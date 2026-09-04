package me.dahiorus.project.vending.infrastructure.cache.config;

import me.dahiorus.project.vending.infrastructure.cache.VendingCacheResolver;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
  @Bean
  CacheResolver vendingCacheResolver(final CacheManager cacheManager) {
    return new VendingCacheResolver(cacheManager);
  }
}
