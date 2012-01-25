package org.springframework.data.services;

import java.net.URI;
import java.util.List;

import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ListResolver implements Resolver<List> {

  @Override public boolean supports(URI uri, Object target) {
    return (null != uri.getFragment() && target instanceof List);
  }

  @Override public Object resolve(URI uri, List target) {
    int i = UriUtils.fragmentToInt(uri);
    if (i > -1) {
      return target.get(i);
    } else {
      return null;
    }
  }

}
