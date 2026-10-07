package com.hospital.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Bed class representing a physical ward bed, its real-time allocation state,
 * maintenance/sanitation lifecycle, and occupant association.
 */
public class Bed {
    public enum BedStatus {
        AVAILABLE,
        OCCUPIED,
        UNDER_MAINTENANCE,
        SANITIZING
    }

    private final String bedId;
    private final String wardType;         // "ICU", "HDU", "General"
    private final int floorNumber;
    private final boolean hasVentilator;   // Critical for ICU evaluation
    private BedStatus status;
    private Patient currentPatient;
    private LocalDateTime occupiedSince;
    private double dailyCharge;

    // Full Parameterized Constructor
    public Bed(String bedId, String wardType, int floorNumber, boolean hasVentilator, double dailyCharge) {
        if (bedId == null || bedId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bed ID cannot be null or empty.");
        }
        if (wardType == null || wardType.trim().isEmpty()) {
            throw new IllegalArgumentException("Ward Type must be specified.");
        }

        this.bedId = bedId.trim().toUpperCase();
        this.wardType = wardType.trim();
        this.floorNumber = floorNumber;
        this.hasVentilator = hasVentilator;
        this.dailyCharge = Math.max(0.0, dailyCharge);
        this.status = BedStatus.AVAILABLE;
        this.currentPatient = null;
        this.occupiedSince = null;
    }

    // Standard constructor defaulting charge and floor
    public Bed(String bedId, String wardType, boolean hasVentilator) {
        this(bedId, wardType, 1, hasVentilator, wardType.equalsIgnoreCase("ICU") ? 5000.0 : (wardType.equalsIgnoreCase("HDU") ? 2500.0 : 1000.0));
    }

    // Minimal constructor
    public Bed(String bedId, String wardType) {
        this(bedId, wardType, false);
    }

    /**
     * Atomically allocates a patient to this bed if it is currently available.
     * Synchronized to prevent double allocation race conditions across multiple threads.
     */
    public synchronized boolean allocate(Patient patient) {
        if (patient == null) {
            return false;
        }
        if (this.status == BedStatus.AVAILABLE) {
            this.currentPatient = patient;
            this.status = BedStatus.OCCUPIED;
            this.occupiedSince = LocalDateTime.now();
            patient.setAllocatedBedId(this.bedId);
            patient.addClinicalNote("Admitted to " + this.bedId + " (" + this.wardType + " Ward, Floor " + this.floorNumber + ")");
            return true;
        }
        return false;
    }

    /**
     * Releases the occupant and transitions bed into SANITIZING status for hospital hygiene safety.
     */
    public synchronized Patient discharge() {
        if (this.status != BedStatus.OCCUPIED || this.currentPatient == null) {
            return null;
        }
        Patient dischargedPatient = this.currentPatient;
        dischargedPatient.setAllocatedBedId("UNASSIGNED");
        dischargedPatient.addClinicalNote("Discharged from " + this.bedId + ". Total stay: " + getStayDurationString());

        this.currentPatient = null;
        this.occupiedSince = null;
        this.status = BedStatus.SANITIZING;
        return dischargedPatient;
    }

    /**
     * Marks sanitation complete, returning bed to active pool.
     */
    public synchronized void markSanitized() {
        if (this.status == BedStatus.SANITIZING || this.status == BedStatus.UNDER_MAINTENANCE) {
            this.status = BedStatus.AVAILABLE;
        }
    }

    public synchronized void setMaintenance(boolean underMaintenance) {
        if (this.status == BedStatus.OCCUPIED) {
            throw new IllegalStateException("Cannot set an occupied bed to maintenance. Discharge patient first.");
        }
        this.status = underMaintenance ? BedStatus.UNDER_MAINTENANCE : BedStatus.AVAILABLE;
    }

    public synchronized long getStayDurationInMinutes() {
        if (this.occupiedSince == null || this.status != BedStatus.OCCUPIED) {
            return 0;
        }
        return Duration.between(this.occupiedSince, LocalDateTime.now()).toMinutes();
    }

    public synchronized String getStayDurationString() {
        long minutes = getStayDurationInMinutes();
        long hours = minutes / 60;
        long remainingMins = minutes % 60;
        return hours + " hr " + remainingMins + " min";
    }

    public synchronized double calculateAccruedBill() {
        long minutes = getStayDurationInMinutes();
        double hours = minutes / 60.0;
        double days = Math.max(1.0, Math.ceil(hours / 24.0)); // minimum 1-day billing
        return days * this.dailyCharge;
    }

    public synchronized String getStatusDetails() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = (occupiedSince != null) ? occupiedSince.format(dtf) : "N/A";
        String occupantStr = (currentPatient != null) ? currentPatient.getName() + " [P" + currentPatient.getTriageScore() + "]" : "None";

        return String.format("Bed [%-7s] | Ward: %-7s | Vent: %-5b | Status: %-11s | Occupant: %-15s | Since: %s",
                bedId, wardType, hasVentilator, status, occupantStr, timeStr);
    }

    // Getters and Setters
    public String getBedId() { return bedId; }
    public String getWardType() { return wardType; }
    public int getFloorNumber() { return floorNumber; }
    public boolean isHasVentilator() { return hasVentilator; }
    public double getDailyCharge() { return dailyCharge; }
    public void setDailyCharge(double charge) { this.dailyCharge = Math.max(0.0, charge); }

    public synchronized BedStatus getStatus() { return status; }
    public synchronized boolean isAvailable() { return this.status == BedStatus.AVAILABLE; }
    public synchronized boolean isOccupied() { return this.status == BedStatus.OCCUPIED; }
    public synchronized Patient getCurrentPatient() { return currentPatient; }
    public synchronized LocalDateTime getOccupiedSince() { return occupiedSince; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bed)) return false;
        Bed bed = (Bed) o;
        return Objects.equals(bedId, bed.bedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bedId);
    }

    @Override
    public String toString() {
        return String.format("%s (%s Ward, Floor %d) - %s", bedId, wardType, floorNumber, status);
    }
}