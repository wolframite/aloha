package com.zalora.aloha.models.repositories;

import com.zalora.aloha.models.entities.Product;
import javax.transaction.Transactional;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
@Transactional(rollbackOn = Exception.class)
public interface ProductRepository extends PagingAndSortingRepository<Product, String> {}
