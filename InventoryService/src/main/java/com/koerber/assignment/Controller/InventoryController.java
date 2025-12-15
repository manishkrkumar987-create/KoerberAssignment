package com.koerber.assignment.Controller;


import com.koerber.assignment.dto.InventoryResponseDTO;
import com.koerber.assignment.entity.InventoryBatch;
import com.koerber.assignment.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponseDTO> getInventory(@PathVariable Long productId) {
        List<InventoryBatch> batches = inventoryService.getSortedInventory(productId);

        if (batches.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Mapping Entity to DTO
        List<InventoryResponseDTO.BatchDTO> batchDtos = batches.stream()
                .map(b -> new InventoryResponseDTO.BatchDTO(
                        b.getBatchId(),
                        b.getQuantity(),
                        b.getExpiryDate().toString()))
                .collect(Collectors.toList());

        InventoryResponseDTO response = InventoryResponseDTO.builder()
                .productId(productId)
                .productName(batches.get(0).getProductName())
                .batches(batchDtos)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<List<Long>> updateInventory(@RequestParam Long productId, @RequestParam Integer quantity) {
        List<Long> batchIds = inventoryService.updateInventory(productId, quantity);
        return ResponseEntity.ok(batchIds); // Sending [2, 5, 7] back to Order Service
    }
}
