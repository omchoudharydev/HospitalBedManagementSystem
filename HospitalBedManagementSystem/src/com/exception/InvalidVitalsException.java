package com.hospital.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Checked exception thrown when incoming biomedical telemetry (SpO2, Heart Rate, Temp)
 * falls outside valid physiological survival limits.
 */
public class InvalidVitalsException extends Exception {
    private final String metricName;
    private final double invalidValue;
    private final String timestamp;

    public InvalidVitalsException(String message) {
        super(message);
        this.metricName = "GENERIC_VITALS";
        this.invalidValue = -1.0;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public InvalidVitalsException(String metricName, double invalidValue, String customMessage) {
        super(String.format("Telemetry Validation Failed for [%s = %.2f]: %s", metricName, invalidValue, customMessage));
        this.metricName = metricName;
        this.invalidValue = invalidValue;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getMetricName() {
        return metricName;
    }

    public double getInvalidValue() {
        return invalidValue;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[InvalidVitalsException at %s] Metric: %s | Value: %.2f | Cause: %s",
                timestamp, metricName, invalidValue, getMessage());
    }
}