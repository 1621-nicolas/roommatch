package com.roommatch.dto;
import jakarta.validation.constraints.*;
public record DecisionModeracionRequest(
    @NotBlank(message = "Explica el motivo de esta decisión") @Size(max = 500) String motivo
) {}
