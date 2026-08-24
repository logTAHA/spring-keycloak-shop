package com.shop.controller;

import com.shop.dto.ApiResponse;
import com.shop.dto.PagedResponse;
import com.shop.dto.product.add.AddProductRequest;
import com.shop.dto.product.get.GetProductRequest;
import com.shop.dto.product.get.ProductResponse;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addOrUpdateProduct(
            @Valid @RequestBody AddProductRequest request
    ) {

        return ResponseEntity.ok(ApiResponse.ok());
    }
}