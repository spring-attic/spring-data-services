package org.springframework.data.services;

import java.net.URI;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Resolver<T> {

  boolean supports(URI uri, Object target);

  Object resolve(URI uri, T target);

}
