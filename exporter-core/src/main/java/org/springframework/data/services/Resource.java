package org.springframework.data.services;

import java.net.URI;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Resource<T> {

  URI uri();

  T target();

}
