package com.stockalert.alerts.model;

public enum AlertStatus {
    ACTIVE("Activa"),
    RESOLVED("Resuelta");

    private final String label;

    AlertStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
