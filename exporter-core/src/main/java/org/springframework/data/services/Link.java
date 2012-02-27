package org.springframework.data.services;

import java.net.URI;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Link {

  String rel();

  URI href();

}
