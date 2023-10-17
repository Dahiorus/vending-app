package me.dahiorus.project.vending.cache;

import java.util.Collection;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class VendingCacheResolver extends AbstractCacheResolver
{
  public VendingCacheResolver(final CacheManager cacheManager)
  {
    super(cacheManager);
  }

  @Override
  protected Collection<String> getCacheNames(final CacheOperationInvocationContext<?> context)
  {
    Set<String> cacheNames = context.getOperation().getCacheNames();
    if (!cacheNames.isEmpty())
    {
      log.trace("Returning operation [{}] declared caches: {}", context.getMethod(), cacheNames);

      return cacheNames;
    }

    Class<?> targetClass = context.getTarget().getClass();
    CacheConfig cacheConfigAnnotation = targetClass.getAnnotation(CacheConfig.class);
    Collection<String> targetCacheNames = Set.of(cacheConfigAnnotation.cacheNames());

    log.trace("Returning target [{}] declared caches: {}", targetCacheNames);

    return targetCacheNames;
  }
}
