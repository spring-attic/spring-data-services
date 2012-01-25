package org.springframework.data.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class DelegatingResolver implements Resolver {

  private List<Resolver> resolvers = new ArrayList<Resolver>();

  public DelegatingResolver() {
  }

  public DelegatingResolver(List<Resolver> resolvers) {
    this.resolvers = resolvers;
  }

  public List<Resolver> resolvers() {
    return resolvers;
  }

  public DelegatingResolver resolvers(List<Resolver> resolvers) {
    this.resolvers = resolvers;
    return this;
  }

  @Override public boolean supports(URI uri, Object target) {
    for (Resolver r : resolvers) {
      if (r.supports(uri, target)) {
        return true;
      }
    }
    return false;
  }

  @Override public Object resolve(URI uri, Object target) {
    for (Resolver r : resolvers) {
      if (r.supports(uri, target)) {
        return r.resolve(uri, target);
      }
    }
    return null;
  }

}
