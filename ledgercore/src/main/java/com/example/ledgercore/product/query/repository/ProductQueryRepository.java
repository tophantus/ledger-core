package com.example.ledgercore.product.query.repository;

import com.example.ledgercore.product.entity.Product;
import com.example.ledgercore.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductQueryRepository
        extends JpaRepository<Product, UUID> {

    List<Product> findByStatusOrderByCodeAsc(
            ProductStatus status
    );
}