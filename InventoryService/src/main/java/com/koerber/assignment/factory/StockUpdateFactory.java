package com.koerber.assignment.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockUpdateFactory {
    @Autowired
    private FEFOUpdateStrategy fefoStrategy;

    public StockUpdateStrategy getStrategy(String type) {
        if ("FEFO".equalsIgnoreCase(type)) return fefoStrategy;
        throw new IllegalArgumentException("Unknown strategy");
    }
}
