package com.rods.magicreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MagicreatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicreatorApplication.class, args);
    }

}
