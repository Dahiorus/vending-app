package me.dahiorus.project.vending.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.BasicOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

@ExtendWith(MockitoExtension.class)
class VendingCacheResolverTest
{
  @Mock
  CacheManager cacheManager;

  @Mock
  CacheOperationInvocationContext<?> context;

  VendingCacheResolver cacheResolver;

  @BeforeEach
  void setUp()
  {
    cacheResolver = new VendingCacheResolver(cacheManager);
  }

  @Test
  void getClassCache()
  {
    when(context.getOperation()).then(invoc -> {
      BasicOperation op = mock(BasicOperation.class);
      when(op.getCacheNames()).thenReturn(Set.of());
      return op;
    });
    when(context.getTarget()).thenReturn(new AnnotatedClass());

    Cache classCache = mock(Cache.class);
    when(cacheManager.getCache("testCache")).thenReturn(classCache);

    Collection<? extends Cache> caches = cacheResolver.resolveCaches(context);

    assertThat(caches)
      .singleElement()
      .isEqualTo(classCache);
  }

  @Test
  void getOperationCache() {
    when(context.getOperation()).then(invoc -> {
      BasicOperation op = mock(BasicOperation.class);
      when(op.getCacheNames()).thenReturn(Set.of("operationCache"));
      return op;
    });

    Cache operationCache = mock(Cache.class);
    when(cacheManager.getCache("operationCache")).thenReturn(operationCache);

    Collection<? extends Cache> caches = cacheResolver.resolveCaches(context);

    assertThat(caches)
      .singleElement()
      .isEqualTo(operationCache);
  }

  @CacheConfig(cacheNames = "testCache")
  static class AnnotatedClass
  {
  }
}
