package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Base abstract class defining common attributes and contracts for human entities.
 * Demonstrates Abstraction, Encapsulation, and Polymorphic foundation.
 */
public abstract class Person {
    protected final String id;
    protected String name;
    protected int age;
    protected String gender;
    protected String contactNumber;
    protected final LocalDateTime registeredAt;

    // Parameterized Constructor
    public Person(String id, String name, int age, String gender, String contactNumber) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Person ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Person name cannot be null or empty.");
        }
        if (age < 0 || age > 130) {
            throw new IllegalArgumentException("Age must be within a realistic human lifespan (0-130).");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.age = age;
        this.gender = (gender != null && !gender.trim().isEmpty()) ? gender.trim() : "Unknown";
        this.contactNumber = (contactNumber != null && !contactNumber.trim().isEmpty()) ? contactNumber.trim() : "N/A";
        this.registeredAt = LocalDateTime.now();
    }

    // Overloaded Constructor for minimal details
    public Person(String id, String name, int age, String gender) {
        this(id, name, age, gender, "N/A");
    }

    // Abstract method to be mandatorily overridden by concrete subclasses
    public abstract String getSummary();

    // Common formatted timestamp helper
    public String getFormattedRegistrationTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return this.registeredAt.format(formatter);
    }

    // Getters and Setters with basic sanitization
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age >= 0 && age <= 130) {
            this.age = age;
        }
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        if (gender != null && !gender.trim().isEmpty()) {
            this.gender = gender.trim();
        }
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            this.contactNumber = contactNumber.trim();
        }
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    // Standard Identity contracts
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s, %d y/o %s)", name, id, age, gender);
    }
}