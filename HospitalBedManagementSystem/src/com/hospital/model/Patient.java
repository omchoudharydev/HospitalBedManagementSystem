package com.hospital.model;

import com.hospital.exception.InvalidVitalsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Patient class tracking real-time vitals, triage levels, and treatment history.
 * Incorporates thread-safe telemetry manipulation and clinical threshold validation.
 */
public class Patient extends Person {
    private int spo2;                      // Oxygen Saturation percentage (0-100)
    private int heartRate;                 // Heart beats per minute (20-260)
    private double bodyTemperature;        // In Fahrenheit (e.g., 98.6)
    private boolean trauma;                // Flag for severe injuries / emergency accident
    private int triageScore;               // 1 = Resuscitation/ICU, 2 = Emergent/HDU, 3 = Stable/General
    private String allocatedBedId;         // Assigned Bed Identifier
    private String attendingDoctor;        // Assigned Physician
    private final List<String> clinicalNotes; // Treatment event logs

    // Primary Constructor
    public Patient(String id, String name, int age, String gender, String contactNumber,
                   int spo2, int heartRate, double bodyTemperature, boolean trauma) 
                   throws InvalidVitalsException {
        super(id, name, age, gender, contactNumber);
        validateVitals(spo2, heartRate, bodyTemperature);
        this.spo2 = spo2;
        this.heartRate = heartRate;
        this.bodyTemperature = bodyTemperature;
        this.trauma = trauma;
        this.allocatedBedId = "UNASSIGNED";
        this.attendingDoctor = "Unassigned";
        this.clinicalNotes = new ArrayList<>();
        this.triageScore = computeAutomatedTriage();
        addClinicalNote("Patient registered. Initial Triage computed as P" + this.triageScore);
    }

    // Overloaded Constructor without custom contact or temperature
    public Patient(String id, String name, int age, String gender, int spo2, int heartRate, boolean trauma) 
            throws InvalidVitalsException {
        this(id, name, age, gender, "N/A", spo2, heartRate, 98.6, trauma);
    }

    /**
     * Strict physiological boundary checks to prevent telemetry errors.
     */
    public static void validateVitals(int spo2, int heartRate, double bodyTemperature) throws InvalidVitalsException {
        if (spo2 < 0 || spo2 > 100) {
            throw new InvalidVitalsException("SpO2 level must be within [0, 100]%. Supplied: " + spo2);
        }
        if (heartRate < 20 || heartRate > 260) {
            throw new InvalidVitalsException("Heart rate out of biological limits (20-260 bpm). Supplied: " + heartRate);
        }
        if (bodyTemperature < 70.0 || bodyTemperature > 115.0) {
            throw new InvalidVitalsException("Body temperature out of survival thresholds (70-115 °F). Supplied: " + bodyTemperature);
        }
    }

    /**
     * Clinical rule engine mapping vitals to priority level:
     * P1 (Critical/ICU), P2 (Emergent/HDU), P3 (Stable/General Ward).
     */
    public synchronized int computeAutomatedTriage() {
        if (this.trauma || this.spo2 < 88 || this.heartRate > 135 || this.heartRate < 45 || this.bodyTemperature >= 104.0) {
            this.triageScore = 1;
        } else if (this.spo2 < 93 || this.heartRate > 110 || this.bodyTemperature >= 101.5) {
            this.triageScore = 2;
        } else {
            this.triageScore = 3;
        }
        return this.triageScore;
    }

    /**
     * Updates telemetry safely across concurrent background monitoring threads.
     */
    public synchronized void updateVitals(int newSpo2, int newHeartRate, double newTemp) throws InvalidVitalsException {
        validateVitals(newSpo2, newHeartRate, newTemp);
        this.spo2 = newSpo2;
        this.heartRate = newHeartRate;
        this.bodyTemperature = newTemp;
        int oldScore = this.triageScore;
        computeAutomatedTriage();
        if (oldScore != this.triageScore) {
            addClinicalNote("Vitals changed. Triage level shifted from P" + oldScore + " to P" + this.triageScore);
        }
    }

    public synchronized void addClinicalNote(String note) {
        if (note != null && !note.trim().isEmpty()) {
            this.clinicalNotes.add(LocalDateTime.now().toLocalTime().toString().substring(0, 8) + " - " + note.trim());
        }
    }

    public String getRecommendedWardType() {
        switch (this.triageScore) {
            case 1: return "ICU";
            case 2: return "HDU";
            default: return "General";
        }
    }

    public boolean isCritical() {
        return this.triageScore == 1;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("================ PATIENT ADMISSION CARD ================\n")
          .append(String.format("Patient: %-20s | ID: %s\n", name, id))
          .append(String.format("Age/Gender: %d / %-10s | Contact: %s\n", age, gender, contactNumber))
          .append(String.format("Vitals: SpO2: %d%% | HR: %d bpm | Temp: %.1f °F\n", spo2, heartRate, bodyTemperature))
          .append(String.format("Trauma Flag: %-5b | Triage Score: P%d (%s Ward)\n", trauma, triageScore, getRecommendedWardType()))
          .append(String.format("Allocated Bed: %-10s | Attending: %s\n", allocatedBedId, attendingDoctor))
          .append(String.format("Registered On: %s\n", getFormattedRegistrationTime()))
          .append("---------------- Recent Clinical Notes ----------------\n");

        if (clinicalNotes.isEmpty()) {
            sb.append("No clinical notes recorded.\n");
        } else {
            for (String note : clinicalNotes) {
                sb.append("• ").append(note).append("\n");
            }
        }
        sb.append("========================================================");
        return sb.toString();
    }

    // Thread-safe accessors
    public synchronized int getSpo2() { return spo2; }
    public synchronized void setSpo2(int spo2) { this.spo2 = spo2; computeAutomatedTriage(); }

    public synchronized int getHeartRate() { return heartRate; }
    public synchronized void setHeartRate(int heartRate) { this.heartRate = heartRate; computeAutomatedTriage(); }

    public synchronized double getBodyTemperature() { return bodyTemperature; }
    public synchronized void setBodyTemperature(double temp) { this.bodyTemperature = temp; computeAutomatedTriage(); }

    public synchronized boolean isTrauma() { return trauma; }
    public synchronized void setTrauma(boolean trauma) { this.trauma = trauma; computeAutomatedTriage(); }

    public synchronized int getTriageScore() { return triageScore; }
    public synchronized void setTriageScore(int triageScore) { this.triageScore = triageScore; }

    public synchronized String getAllocatedBedId() { return allocatedBedId; }
    public synchronized void setAllocatedBedId(String allocatedBedId) { this.allocatedBedId = allocatedBedId; }

    public synchronized String getAttendingDoctor() { return attendingDoctor; }
    public synchronized void setAttendingDoctor(String attendingDoctor) { this.attendingDoctor = attendingDoctor; }

    public synchronized List<String> getClinicalNotes() {
        return Collections.unmodifiableList(new ArrayList<>(clinicalNotes));
    }
}