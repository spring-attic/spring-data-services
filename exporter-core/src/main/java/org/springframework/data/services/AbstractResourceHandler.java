package org.springframework.data.services;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractResourceHandler
    implements ResourceHandler {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final URI baseUri;

  protected AbstractResourceHandler(URI baseUri) {
    this.baseUri = baseUri;
  }

}
