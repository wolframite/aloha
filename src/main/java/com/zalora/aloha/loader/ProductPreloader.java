package com.zalora.aloha.loader;

import com.zalora.aloha.models.entities.Item;
import com.zalora.aloha.models.entities.Product;
import com.zalora.aloha.models.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class ProductPreloader implements Preloader {

    @Value("${infinispan.cache.readthrough.preloadPageSize}")
    private int pageSize;

    private ProductRepository productRepository;
    private EntityManager entityManager;

    @Autowired
    public ProductPreloader(ProductRepository productRepository, EntityManager entityManager) {
        Assert.notNull(productRepository, "Product Repository could not be autoloaded");
        Assert.notNull(entityManager, "Entity Manager could not be autoloaded");
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void preLoad(boolean preload, Cache<String, Item> cache) {
        if (!preload) {
            return;
        }

        int page = 0;
        Slice<Product> productPage;

        log.info("Starting pre-load with page size {}", pageSize);

        do {
            productPage = productRepository.findAll(new PageRequest(page, pageSize));
            for (Product product : productPage) {
                cache.putAsync(product.getId(), product);
            }

            log.info("Imported chunk {}", productPage.getNumber() + 1);
            entityManager.clear();
            page++;
        } while (productPage.hasNext());

        // Try to reclaim some space
        entityManager.clear();
        System.gc();
    }

}
