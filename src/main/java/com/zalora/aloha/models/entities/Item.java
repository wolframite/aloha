package com.zalora.aloha.models.entities;

import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public interface Item {

    String getId();

    byte[] getData();

    long getFlags();

}
