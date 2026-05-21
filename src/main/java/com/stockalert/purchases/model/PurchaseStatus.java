package com.stockalert.purchases.model;

public enum PurchaseStatus {
    RECEIVED("Recibida"),
    CANCELLED("Anulada");

    private final String label;

    PurchaseStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
