package com.rods.magicreator.integration.repositories.character.mongodb.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

public class MongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        MongoContainerSingleton mongoContainerSingleton = new MongoContainerSingleton();
        mongoContainerSingleton.instance.start();
        Map<String, String> addedProperties = Map.of("spring.data.mongodb.uri",  mongoContainerSingleton.instance.getReplicaSetUrl());

        TestPropertyValues.of(addedProperties).applyTo(applicationContext);
    }
}

