package org.springframework.data.services;

import java.net.URI;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceOperations {

  <T> Resource<T> create(Resource<Map<String, Object>> resource);

  <T> Resource<T> retrieve(URI uri);

  <T> Resource<T> update(Resource<Map<String, Object>> resource);

  <T> Resource<T> delete(URI uri);

}
