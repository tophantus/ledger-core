package com.example.ledgercore.product.adapter.inbound.rest;

import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeQuery;
import com.example.ledgercore.product.query.dto.GetActiveProductsByTypeResult;
import com.example.ledgercore.product.query.port.inbound.GetActiveProductsByTypeUseCase;
import com.example.ledgercore.product.enums.ProductType;
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

    private final GetActiveProductsByTypeUseCase
            getActiveProductsByTypeUseCase;

    @GetMapping
    @Operation(
            summary = "Get active products by type",
            description = "Get all currently active banking products by product type"
    )
    public ResponseEntity<ApiResponse<GetActiveProductsByTypeResult>> getActiveProductsByType(
            @Parameter(
                    description = "Product type",
                    required = true
            )
            @RequestParam ProductType type
    ) {
        GetActiveProductsByTypeResult response =
                getActiveProductsByTypeUseCase.execute(
                        new GetActiveProductsByTypeQuery(type)
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Active products retrieved successfully"
                )
        );
    }
}