package org.springframework.data.services;

import java.net.URI;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Link {

  public static final String LINKS = "_links";

  String rel();

  URI href();

}
