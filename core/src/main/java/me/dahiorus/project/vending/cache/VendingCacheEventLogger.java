package me.dahiorus.project.vending.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class VendingCacheEventLogger implements CacheEventListener<Object, Object>
{

  @Override
  public void onEvent(final CacheEvent<?, ?> event)
  {
    log.info("Cache event = {}, Key = {},  Old value = {}, New value = {}", event.getType(),
      event.getKey(), event.getOldValue(), event.getNewValue());
  }

}
