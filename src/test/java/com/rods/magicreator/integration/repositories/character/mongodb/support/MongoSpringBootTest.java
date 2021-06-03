package com.rods.magicreator.integration.repositories.character.mongodb.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(value= ElementType.TYPE)
@SpringBootTest
@ContextConfiguration(initializers = MongoInitializer.class)
public @interface MongoSpringBootTest {

}
