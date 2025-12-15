package com.koerber.assignment.service;

import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.factory.StockUpdateFactory;
import com.koerber.assignment.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {
    @Autowired
    private InventoryRepository repository;
    @Autowired
    private StockUpdateFactory strategyFactory;

    @Autowired
    private InventoryRepository inventoryRepository;


    public List<InventoryBatch> getSortedInventory(Long productId) {
        return inventoryRepository.findByProductIdOrderByExpiryDateAsc(productId);
    }

    @Transactional
    public List<Long> updateInventory(Long productId, Integer quantity) {
        return strategyFactory.getStrategy("FEFO").updateStock(productId, quantity);
    }
}
