package me.dahiorus.project.vending.infrastructure.cache;

import java.util.Collection;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

public class VendingCacheResolver extends AbstractCacheResolver {
  private static final Logger logger = LoggerFactory.getLogger(VendingCacheResolver.class);

  public VendingCacheResolver(final CacheManager cacheManager) {
    super(cacheManager);
  }

  @Override
  protected Collection<String> getCacheNames(final CacheOperationInvocationContext<?> context) {
    Set<String> cacheNames = context.getOperation().getCacheNames();
    if (!cacheNames.isEmpty()) {
      logger.trace("Returning operation [{}] declared caches: {}", context.getMethod(), cacheNames);

      return cacheNames;
    }

    Class<?> targetClass = context.getTarget().getClass();
    CacheConfig cacheConfigAnnotation = targetClass.getAnnotation(CacheConfig.class);
    Collection<String> targetCacheNames = Set.of(cacheConfigAnnotation.cacheNames());

    logger.trace("Returning target [{}] declared caches: {}", targetClass, targetCacheNames);

    return targetCacheNames;
  }
}
