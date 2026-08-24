package com.shop.controller;

import com.shop.dto.ApiResponse;
import com.shop.dto.PagedResponse;
import com.shop.dto.product.add.UpsertProductRequest;
import com.shop.dto.product.add.AddProductResponse;
import com.shop.dto.product.add.UpsertProductResult;
import com.shop.dto.product.get.GetProductRequest;
import com.shop.dto.product.get.ProductResponse;
import com.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AddProductResponse>> addOrUpdateProduct(
            @Valid @RequestBody UpsertProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = (jwt != null) ? jwt.getSubject() : "anonymous";
        log.info("Processing upsert product '{}' (price: {}, stock: {}) by user ID '{}'",
                request.name(), request.price(), request.stock(), userId
        );

        UpsertProductResult result = productService.addOrUpdateProduct(request);

        log.info("Product '{}' (ID: {}) was successfully {} by user ID '{}'",
                result.product().name(), result.product().id(),
                result.isCreated() ? "created" : "updated", userId
        );

        String message = result.isCreated() ? "Product created successfully" : "Product updated successfully";
        HttpStatus status = result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.ok(message, result.product()));
    }
}