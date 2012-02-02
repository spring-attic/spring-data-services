package org.springframework.data.services;

import java.net.URI;

import org.springframework.data.services.util.BeanUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BeanPropertyResourceHandler extends UriTailResourceHandler<String, BeanPropertyResourceHandler> {

  public BeanPropertyResourceHandler(URI baseUri) {
    super(baseUri);
  }

  @Override protected Object handleTail(String s, Object... args) {
    return BeanUtils.findFirst(s, args);
  }

}
