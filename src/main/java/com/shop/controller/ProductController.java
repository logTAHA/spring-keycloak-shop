package com.shop.controller;

import com.shop.dto.ApiResponse;
import com.shop.dto.PagedResponse;
import com.shop.dto.product.GetProductRequest;
import com.shop.dto.product.ProductResponse;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @Valid @ModelAttribute GetProductRequest request
    ) {
        PagedResponse<ProductResponse> response = productService.getProducts(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}