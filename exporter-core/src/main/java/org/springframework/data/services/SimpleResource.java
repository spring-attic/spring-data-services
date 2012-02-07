package org.springframework.data.services;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleResource implements Resource {

  private final URI uri;
  private final Map<String, String> metadata = new HashMap<String, String>();
  private final Object entity;

  public SimpleResource(URI uri) {
    this.uri = uri;
    this.entity=null;
  }

  public SimpleResource(URI uri, Object entity) {
    this.uri = uri;
    this.entity = entity;
  }

  @Override public URI uri() {
    return uri;
  }

  @Override public Map<String, String> metadata() {
    return metadata;
  }

  @Override public Object target() {
    return entity;
  }

}
