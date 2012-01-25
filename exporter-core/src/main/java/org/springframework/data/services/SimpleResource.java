package org.springframework.data.services;

import java.net.URI;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleResource implements Resource {

  private URI uri;
  private Object entity;

  private SimpleResource(URI uri, Object entity) {
    this.uri = uri;
    this.entity = entity;
  }

  @Override public URI uri() {
    return uri;
  }

  @Override public Object target() {
    return entity;
  }
}
