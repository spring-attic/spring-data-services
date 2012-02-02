package org.springframework.data.services;

import java.net.URI;
import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ListResourceHandler extends UriTailResourceHandler<Integer, ListResourceHandler> {

  public ListResourceHandler(URI baseUri) {
    super(baseUri);
  }

  @Override protected Object handleTail(Integer idx, Object... args) {
    for (Object obj : args) {
      if (obj instanceof List) {
        List l = (List) obj;
        if (idx < l.size()) {
          return l.get(idx);
        }
      }
    }
    return null;
  }

}
