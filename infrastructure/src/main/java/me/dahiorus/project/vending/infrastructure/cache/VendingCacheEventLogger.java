package me.dahiorus.project.vending.infrastructure.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VendingCacheEventLogger implements CacheEventListener<Object, Object> {

  private static final Logger logger = LoggerFactory.getLogger(VendingCacheEventLogger.class);

  @Override
  public void onEvent(final CacheEvent<?, ?> event) {
    logger.info(
        "Cache event = {}, Key = {},  Old value = {}, New value = {}",
        event.getType(),
        event.getKey(),
        event.getOldValue(),
        event.getNewValue());
  }
}
