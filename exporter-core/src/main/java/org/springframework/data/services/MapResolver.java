package org.springframework.data.services;

import java.net.URI;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class MapResolver implements Resolver<Map> {

  @Override public boolean supports(URI uri, Object target) {
    return (null != uri.getFragment() && target instanceof Map);
  }

  @Override public Object resolve(URI uri, Map target) {
    return target.get(uri.getFragment());
  }

}
