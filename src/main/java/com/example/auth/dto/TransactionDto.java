package com.example.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class TransactionDto {
    @NotNull
    private String paidBy;
    private String borrowedBy;
    private String groupId;

    @Builder.Default
    private SplitType splitType = SplitType.EQUAL; // Default to EQUAL if not provided

    @Builder.Default
    private List<Double> splitValues = new ArrayList<>(); // Optional, required only for UNEQUAL and PERCENTAGE

    @NotNull
    private String description;

    @NotNull
    private Double amount;
}
