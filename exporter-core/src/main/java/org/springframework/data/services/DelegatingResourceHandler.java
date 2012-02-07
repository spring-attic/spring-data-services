package org.springframework.data.services;

import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class DelegatingResourceHandler implements ResourceHandler {

  private final List<ResourceHandler> handlers;

  public DelegatingResourceHandler(List<ResourceHandler> handlers) {
    this.handlers = handlers;
  }

  @Override public boolean supports(Resource resource, Object... args) {
    for (ResourceHandler handler : handlers) {
      if (handler.supports(resource, args)) {
        return true;
      }
    }
    return false;
  }

  @Override public Resource handle(Resource resource, Object... args) {
    Resource res = null;
    for (ResourceHandler handler : handlers) {
      if (handler.supports(resource, args)) {
        res = handler.handle(resource, args);
        break;
      }
    }
    return res;
  }

}
