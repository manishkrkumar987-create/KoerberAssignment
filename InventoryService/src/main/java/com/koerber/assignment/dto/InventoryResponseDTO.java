package com.koerber.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDTO {
    private Long productId;
    private String productName;
    private List<BatchDTO> batches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchDTO {
        private Long batchId;
        private Integer quantity;
        private String expiryDate; // Formatted as String for JSON
    }
}
