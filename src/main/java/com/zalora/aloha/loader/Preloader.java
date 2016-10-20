package com.zalora.aloha.loader;

import com.zalora.aloha.models.entities.Item;
import org.infinispan.Cache;

/**
 * Preload read through items manually, the JPA store cannot handle amounts > 5000
 * See https://developer.jboss.org/thread/272481 for more details
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public interface Preloader {

    void preLoad(boolean preload, Cache<String, Item> cache);

}
