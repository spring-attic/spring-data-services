package org.springframework.data.services;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class MapResourceResolver extends AbstractResourceResolver {

  public MapResourceResolver(URI baseUri) {
    super(baseUri);
  }

  @Override public boolean supports(URI uri, List<Resource<?>> stack) {
    if (null != stack && stack.size() > 0) {
      Resource r = BeanUtils.tail(stack);
      return r.target() instanceof Map;
    }
    return false;
  }

  @Override public Resource<?> resolve(URI uri, List<Resource<?>> stack) {
    URI tail = UriUtils.tail(baseUri, uri);
    Map m = (Map) BeanUtils.tail(stack).target();
    return new SimpleResource<Object>(uri, m.get(UriUtils.path(tail)));
  }

}
