package org.springframework.data.services;

import java.net.URI;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceHandler {
  Object handle(URI uri, Object... args);
}
