package com.example.ledgercore.product.query.repository;

import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.enums.ProductStatus;
import com.example.ledgercore.product.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductQueryRepository
        extends JpaRepository<Product, UUID> {

    List<Product> findAllByStatus(ProductStatus status);

    List<Product> findAllByTypeAndStatus(
            ProductType type,
            ProductStatus status
    );
}