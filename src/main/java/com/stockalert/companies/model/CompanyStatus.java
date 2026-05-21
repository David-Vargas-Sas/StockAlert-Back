package com.stockalert.companies.model;

public enum CompanyStatus {
    ACTIVE("Activa"),
    INACTIVE("Inactiva"),
    SUSPENDED("Suspendida");

    private final String label;

    CompanyStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
