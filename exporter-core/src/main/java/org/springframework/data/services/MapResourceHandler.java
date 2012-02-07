package org.springframework.data.services;

import java.net.URI;
import java.util.Map;

import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class MapResourceHandler extends AbstractResourceHandler {

  public MapResourceHandler(URI baseUri) {
    super(baseUri);
  }

  @Override public boolean supports(Resource resource, Object... args) {
    return BeanUtils.containsType(Map.class, args);
  }

  @Override public Resource handle(Resource resource, Object... args) {
    URI tail = UriUtils.tail(baseUri, resource.uri());
    return new SimpleResource(tail, ((Map) resource.target()).get(tail.getPath()));
  }

}
