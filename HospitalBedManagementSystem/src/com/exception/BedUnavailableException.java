package com.hospital.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Checked exception thrown when an admission or transfer request fails due to 
 * 100% capacity in the target hospital ward.
 */
public class BedUnavailableException extends Exception {
    private final String requestedWard;
    private final int triagePriority;
    private final String timestamp;

    public BedUnavailableException(String requestedWard) {
        super("Immediate capacity exhausted: No vacant bed found in " + requestedWard + " ward.");
        this.requestedWard = requestedWard;
        this.triagePriority = -1;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public BedUnavailableException(String requestedWard, int triagePriority, String message) {
        super(message);
        this.requestedWard = requestedWard;
        this.triagePriority = triagePriority;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getRequestedWard() {
        return requestedWard;
    }

    public int getTriagePriority() {
        return triagePriority;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[BedUnavailableException at %s] Ward: %s | Patient Priority: P%d | Details: %s",
                timestamp, requestedWard, triagePriority, getMessage());
    }
}