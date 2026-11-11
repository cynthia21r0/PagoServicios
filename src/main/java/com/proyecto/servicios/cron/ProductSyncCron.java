package com.proyecto.servicios.cron;

import com.proyecto.servicios.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductSyncCron {
    private static final Logger log = LoggerFactory.getLogger(ProductSyncCron.class);
    private final ProductService productService;

    public ProductSyncCron(ProductService productService) {
        this.productService = productService;
    }

    public void syncDailyAt6Am() {
        log.info("Ejecutando cron de sincronización de productos a Mongo a las 06:00 AM");
    }
}
