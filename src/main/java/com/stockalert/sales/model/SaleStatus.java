package com.stockalert.sales.model;

public enum SaleStatus {
    ACTIVE("Activa"),
    CANCELLED("Anulada");

    private final String label;

    SaleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
