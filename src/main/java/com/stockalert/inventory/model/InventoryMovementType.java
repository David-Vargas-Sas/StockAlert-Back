package com.stockalert.inventory.model;

public enum InventoryMovementType {
    INITIAL_STOCK("Stock inicial"),
    SALE("Venta"),
    SALE_CANCEL("Anulacion de venta"),
    PURCHASE("Compra"),
    PURCHASE_CANCEL("Anulacion de compra"),
    ADJUSTMENT_IN("Ajuste de entrada"),
    ADJUSTMENT_OUT("Ajuste de salida");

    private final String label;

    InventoryMovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
