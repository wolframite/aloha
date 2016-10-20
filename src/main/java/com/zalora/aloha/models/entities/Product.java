package com.zalora.aloha.models.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Data
@Entity
@Table(name = "catalog_product_cache")
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Item {

    @Id
    @Getter @Setter
    @Column(name = "id_catalog_product_cache")
    private String id;

    @Getter @Setter
    @Column(name = "data", columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] data;

    @Getter @Setter
    @Column(name = "flags", columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    private long flags;

    @Override
    public String toString() {
        return new String(data);
    }

}
