package org.springframework.data.services;

import java.net.URI;
import java.util.Map;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceOperations {

  Resource create(Resource resource, Map<String, Object> model);

  Resource retrieve(URI uri);

  Resource update(Resource resource, Map<String, Object> model);

  Resource delete(URI uri);

}
