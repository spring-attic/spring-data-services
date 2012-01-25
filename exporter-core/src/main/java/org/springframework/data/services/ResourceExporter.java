package org.springframework.data.services;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceExporter extends Resource {

  ResourceOperations operations(Resource resource);

}
