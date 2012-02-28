package org.springframework.data.services;

import java.net.URI;
import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class DelegatingResourceResolver implements ResourceResolver {

  private final List<ResourceResolver> resolvers;

  public DelegatingResourceResolver(List<ResourceResolver> resolvers) {
    this.resolvers = resolvers;
  }

  @Override public boolean supports(URI uri, List<Resource<?>> stack) {
    for (ResourceResolver resolver : resolvers) {
      if (resolver.supports(uri, stack)) {
        return true;
      }
    }
    return false;
  }

  @Override public Resource<?> resolve(URI uri, List<Resource<?>> stack) {
    for (ResourceResolver resolver : resolvers) {
      if (resolver.supports(uri, stack)) {
        return resolver.resolve(uri, stack);
      }
    }
    return null;
  }

  @Override public String toString() {
    return "DelegatingResourceResolver{" +
        "resolvers=" + resolvers +
        '}';
  }

}
