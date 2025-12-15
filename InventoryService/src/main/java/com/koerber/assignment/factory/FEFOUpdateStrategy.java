package com.koerber.assignment.factory;

import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FEFOUpdateStrategy implements StockUpdateStrategy{
    @Autowired
    private InventoryRepository repository;
    @Override
    public List<Long> updateStock(Long productId, Integer quantity) {
        List<InventoryBatch> batches = repository.findByProductIdOrderByExpiryDateAsc(productId);
        List<Long> affectedBatchIds = new ArrayList<>();
        int remainingToDeduct = quantity;

        for (InventoryBatch batch : batches) {
            if (remainingToDeduct <= 0) break;

            int currentStock = batch.getQuantity();
            if (currentStock > 0) {
                affectedBatchIds.add(batch.getBatchId()); // Capture the ID
                int deduction = Math.min(currentStock, remainingToDeduct);
                batch.setQuantity(currentStock - deduction);
                remainingToDeduct -= deduction;
            }
        }
        repository.saveAll(batches);
        return affectedBatchIds; // Return the IDs of batches used
    }

}
