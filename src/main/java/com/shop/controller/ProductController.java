package com.shop.controller;

import com.shop.dto.ApiResponse;
import com.shop.dto.product.GetProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getAllProducts(@Valid @ModelAttribute GetProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok());
    }
}