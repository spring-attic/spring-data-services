package org.springframework.data.services;

import java.net.URI;
import java.util.List;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ResourceResolver extends Resolver<URI, List<Resource<?>>, Resource<?>> {
}
