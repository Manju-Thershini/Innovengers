package com.stockshield.dto; import jakarta.validation.constraints.*; public record OrderRequest(@NotNull Long productId,@Min(1) int quantity,@NotBlank String idempotencyKey){}
