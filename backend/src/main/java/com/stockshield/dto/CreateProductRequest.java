package com.stockshield.dto; import jakarta.validation.constraints.*; public record CreateProductRequest(@NotBlank String sku,@NotBlank String name,@Min(0) int inventory){}
