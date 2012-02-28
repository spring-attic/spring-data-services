package org.springframework.data.services;

import java.net.URI;
import java.util.List;

import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;
import org.springframework.util.NumberUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ListResourceResolver extends AbstractResourceResolver {

  public ListResourceResolver(URI baseUri) {
    super(baseUri);
  }

  @Override public boolean supports(URI uri, List<Resource<?>> stack) {
    Resource r = BeanUtils.tail(stack);
    return (r.target() instanceof List) || r.target().getClass().isArray();
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> stack) {
    URI tail = UriUtils.tail(baseUri, uri);
    int idx = NumberUtils.parseNumber(UriUtils.path(tail), Integer.class);

    Resource r = BeanUtils.tail(stack);
    Object o = r.target();
    if (o instanceof List) {
      List<?> l = (List<?>) o;
      if (idx < l.size()) {
        return new SimpleResource(uri, l.get(idx));
      }
    } else {
      Object[] objs = (Object[]) o;
      if (idx < objs.length) {
        return new SimpleResource(uri, objs[idx]);
      }
    }

    return null;
  }

}
