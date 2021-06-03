package com.rods.magicreator.integration.repositories.character.mongodb.support;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoContainerSingleton {

    public MongoDBContainer instance = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));

    private MongoDBContainer startMongoContainer() {
        return instance.withReuse(true);
    }
}
