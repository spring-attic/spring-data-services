package org.springframework.data.services;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceHandler {

  boolean supports(Resource resource, Object... args);

  Resource handle(Resource resource, Object... args);

}
