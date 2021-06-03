package com.rods.magicreator.support;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.models.House;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Configuration
@EnableCaching
/*
    I was going to use the basic ConcurrentMap cache implementation, but I felt a little
    difficult getting it to work with a expiry time, while Ehcache was a little more straightforward,
    so that's why I used it.
*/
public class EhCacheCustomizer
        implements JCacheManagerCustomizer {

    @Override
    public void customize(javax.cache.CacheManager cacheManager) {
        javax.cache.configuration.Configuration<String, Character> charactersConfig =
                Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Character.class, ResourcePoolsBuilder.heap(100))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60)))
                        .build());

        javax.cache.configuration.Configuration<String, House> housesConfig =
                Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, House.class, ResourcePoolsBuilder.heap(100))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60)))
                        .build());

        cacheManager.createCache("characters", charactersConfig);
        cacheManager.createCache("houses", housesConfig);
    }
}
