package com.shop.service;

import com.shop.dto.PagedResponse;
import com.shop.dto.product.get.GetProductRequest;
import com.shop.dto.product.get.ProductResponse;
import com.shop.dto.product.get.ProductSortBy;
import com.shop.entity.Product;
import com.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
