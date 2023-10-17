package me.dahiorus.project.vending.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration implements CachingConfigurer
{
  @Bean
  CacheResolver vendingCacheResolver(final CacheManager cacheManager)
  {
    return new VendingCacheResolver(cacheManager);
  }
}
