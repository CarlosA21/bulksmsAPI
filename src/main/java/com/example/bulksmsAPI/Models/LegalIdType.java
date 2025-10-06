package com.example.bulksmsAPI.Models;

public enum LegalIdType {
    DRIVER_LICENSE("Driver's License"),
    PASSPORT("Passport"),
    NATIONAL_ID("National ID"),
    SSN("Social Security Number"),
    TAX_ID("Tax ID"),
    VOTER_ID("Voter ID"),
    OTHER("Other");

    private final String displayName;

    LegalIdType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

