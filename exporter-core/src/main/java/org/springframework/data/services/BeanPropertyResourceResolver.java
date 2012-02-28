package org.springframework.data.services;

import java.net.URI;
import java.util.List;

import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BeanPropertyResourceResolver extends AbstractResourceResolver {

  public BeanPropertyResourceResolver(URI baseUri) {
    super(baseUri);
  }

  @Override public boolean supports(URI uri, List<Resource<?>> stack) {
    URI nameUri = UriUtils.tail(baseUri, uri);
    Resource tail = BeanUtils.tail(stack);
    String propertyName = UriUtils.path(nameUri);
    return BeanUtils.hasProperty(propertyName, tail.target());
  }

  @Override public Resource<?> resolve(URI uri, List<Resource<?>> stack) {
    URI nameUri = UriUtils.tail(baseUri, uri);
    Resource tail = BeanUtils.tail(stack);
    String propertyName = UriUtils.path(nameUri);
    return new SimpleResource(uri, BeanUtils.findFirst(propertyName, tail.target()));
  }

}
