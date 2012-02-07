package org.springframework.data.services;

import java.net.URI;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Resource<T> {

  URI uri();

  Map<String, String> metadata();

  T target();

}
