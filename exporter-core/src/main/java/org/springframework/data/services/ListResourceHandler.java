package org.springframework.data.services;

import java.net.URI;
import java.util.List;

import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;
import org.springframework.util.NumberUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ListResourceHandler extends AbstractResourceHandler {

  public ListResourceHandler(URI baseUri) {
    super(baseUri);
  }

  @Override public boolean supports(Resource resource, Object... args) {
    return BeanUtils.containsType(List.class, args) || BeanUtils.containsType(Object[].class, args);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource handle(Resource resource, Object... args) {
    URI tail = UriUtils.tail(baseUri, resource.uri());
    int idx = NumberUtils.parseNumber(tail.getPath(), Integer.class);
    if (args.length == 1) {
      if (args[0] instanceof Object[]) {
        Object[] os = (Object[]) args[0];
        if (idx < os.length) {
          Object o = os[idx];
          return new SimpleResource(tail, o);
        }
      } else if (args[0] instanceof List) {
        List<Object> l = (List<Object>) args[0];
        if (idx < l.size()) {
          Object o = l.get(idx);
          return new SimpleResource(tail, o);
        }
      }
    } else {
      if (idx < args.length) {
        Object o = args[idx];
        return new SimpleResource(tail, o);
      }
    }
    return null;
  }

}
