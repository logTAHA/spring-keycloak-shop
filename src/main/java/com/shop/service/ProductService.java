package com.shop.service;

import com.shop.dto.PagedResponse;
import com.shop.dto.product.add.UpsertProductRequest;
import com.shop.dto.product.add.AddProductResponse;
import com.shop.dto.product.add.UpsertProductResult;
import com.shop.dto.product.get.GetProductRequest;
import com.shop.dto.product.get.ProductDetailResponse;
import com.shop.dto.product.get.ProductResponse;
import com.shop.dto.product.get.ProductSortBy;
import com.shop.entity.product.Product;
import com.shop.handler.BusinessException;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public PagedResponse<ProductResponse> getProducts(GetProductRequest request) {
        Sort sort = resolveSort(request.sortBy());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Specification<Product> spec = Specification.allOf();
        if (Boolean.TRUE.equals(request.inStockOnly())) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stock"), 0));
        }

        Page<ProductResponse> productPage = productRepository
                .findAll(spec, pageable)
                .map(ProductResponse::from);

        return PagedResponse.from(productPage);
    }

    @Transactional
    public UpsertProductResult addOrUpdateProduct(UpsertProductRequest request) {
        Optional<Product> existingProduct = productRepository.findByName(request.name());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            log.info("Updating existing product '{}' (ID: {}): price -> {}, stock -> {}",
                    product.getName(), product.getId(), request.price(), request.stock());

            product.setPrice(request.price());
            product.setStock(request.stock());
            Product updated = productRepository.save(product);

            AddProductResponse response = AddProductResponse.builder()
                    .id(updated.getId())
                    .name(updated.getName())
                    .build();

            return new UpsertProductResult(response, false);
        } else {
            log.info("Creating new product '{}' with price={} and stock={}",
                    request.name(), request.price(), request.stock());

            Product product = Product.builder()
                    .name(request.name())
                    .price(request.price())
                    .stock(request.stock())
                    .build();
            Product saved = productRepository.save(product);

            AddProductResponse response = AddProductResponse.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .build();

            return new UpsertProductResult(response, true);
        }
    }

    private Sort resolveSort(ProductSortBy sortBy) {

        // default
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (sortBy) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
        };
    }

    public ProductDetailResponse getProductByIdentifier(String identifier, boolean isAdmin) {
        Optional<Product> productOpt;
        if (identifier.matches("^\\d+$")) {
            Long id = Long.parseLong(identifier);
            productOpt = productRepository.findById(id).or(() -> productRepository.findByName(identifier));
        } else {
            productOpt = productRepository.findByName(identifier);
        }

        Product product = productOpt.orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "Product not found")
        );

        log.debug("Fetched product '{}' (ID: {}) with isAdmin={}", product.getName(), product.getId(), isAdmin);
        return ProductDetailResponse.from(product, isAdmin);
    }
}
