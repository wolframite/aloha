package com.zalora.aloha.controller;

import com.zalora.aloha.manager.CacheManager;
import org.infinispan.AdvancedCache;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Dump all keys, in memory keys and fetch single keys
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@RestController
public class ItemController {

    private AdvancedCache<String, byte[]> cache;

    @Autowired
    public ItemController(CacheManager cacheManager) {
        cache = cacheManager.getPrimaryCache();
    }

    @RequestMapping("/keys/local")
    public Set getLocalKeys() {
        return cache.getDataContainer().keySet();
    }

    @RequestMapping("/keys")
    public Set getKeys() {
        return cache.keySet();
    }

    @RequestMapping(value = "/item/{key}", produces = "application/json")
    public String getProduct(@PathVariable String key) {
        String item = cache.containsKey(key) ? new String(cache.get(key)) : null;

        try {
            JSONValue.parseWithException(item);
        } catch (ParseException | NullPointerException e) {
            item = JSONValue.toJSONString(item);
        }

        return item;
    }

}
