package com.example.ledgercore.product.adapter.inbound.rest;

import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.product.query.dto.ProductResponse;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(
        name = "Product",
        description = "Product query APIs"
)
public class ProductController {

    private final GetActiveProductsUseCase getActiveProductsUseCase;

    @GetMapping
    @Operation(
            summary = "Get active products",
            description = "Get all currently active banking products"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        List<ProductResponse> response =
                getActiveProductsUseCase.execute();

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Active products retrieved successfully"
                )
        );
    }
}