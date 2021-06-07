package com.rods.magicreator;

import com.rods.magicreator.controller.CharactersController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
public class MagicreatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicreatorApplication.class, args);
    }

}
