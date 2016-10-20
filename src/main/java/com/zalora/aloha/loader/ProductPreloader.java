package com.zalora.aloha.loader;

import com.zalora.aloha.models.entities.Item;
import com.zalora.aloha.models.entities.Product;
import com.zalora.aloha.models.repositories.ProductRepository;
import org.infinispan.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class ProductPreloader implements Preloader {

    ProductRepository productRepository;

    @Autowired
    public ProductPreloader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void preLoad(boolean preload, Cache<String, Item> cache) {
        if (preload) {
            Iterable<Product> items = productRepository.findAll();
            for (Product product : items) {
                cache.putAsync(product.getId(), product);
            }
        }
    }

}
