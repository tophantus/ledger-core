package com.example.ledgercore.product.adapter.inbound.rest;

import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.product.enums.ProductType;
import com.example.ledgercore.product.query.dto.GetActiveProductsQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(
        name = "Product",
        description = "Product query APIs"
)
public class ProductController {

    private final GetActiveProductsUseCase
            getActiveProductsUseCase;

    @GetMapping
    @Operation(
            summary = "Get active products",
            description = "Get all active banking products, optionally filtered by product type"
    )
    public ResponseEntity<ApiResponse<GetActiveProductsResult>> getActiveProducts(
            @Parameter(description = "Product type")
            @RequestParam(required = false) ProductType type
    ) {
        GetActiveProductsResult response =
                getActiveProductsUseCase.execute(
                        new GetActiveProductsQuery(type)
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Active products retrieved successfully"
                )
        );
    }
}