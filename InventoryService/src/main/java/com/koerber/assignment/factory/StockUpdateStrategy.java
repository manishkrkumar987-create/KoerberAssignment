package com.koerber.assignment.factory;

import java.util.List;

public interface StockUpdateStrategy {
    List<Long> updateStock(Long productId, Integer quantity);
}
