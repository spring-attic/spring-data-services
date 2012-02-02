package org.springframework.data.services;

import java.net.URI;
import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class DelegatingResourceHandler implements ResourceHandler {

  private final List<ResourceHandler> handlers;

  public DelegatingResourceHandler(List<ResourceHandler> handlers) {
    this.handlers = handlers;
  }

  @Override public Object handle(URI uri, Object... args) {
    Object obj = null;
    for (ResourceHandler handler : handlers) {
      if (null != (obj = handler.handle(uri, args))) {
        break;
      }
    }
    return obj;
  }

}
