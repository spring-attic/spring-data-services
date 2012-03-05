package org.springframework.data.services;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleResource<T> implements Resource<T> {

  private final URI uri;
  private final Map<String, String> metadata = new HashMap<String, String>();
  private final T entity;

  public SimpleResource(URI uri) {
    this.uri = uri;
    this.entity = null;
  }

  public SimpleResource(URI uri, T entity) {
    this.uri = uri;
    this.entity = entity;
  }

  @Override public URI uri() {
    return uri;
  }

  @Override public Map<String, String> metadata() {
    return metadata;
  }

  @Override public T target() {
    return entity;
  }

}
