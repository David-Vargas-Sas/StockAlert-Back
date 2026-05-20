package com.stockalert.sales.repository;

import java.math.BigDecimal;

public interface TopProductProjection {

    Long getProductId();

    String getProductName();

    Long getQuantitySold();

    BigDecimal getTotalSold();
}
