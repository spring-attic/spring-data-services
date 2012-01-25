package org.springframework.data.services.context;

import java.net.URI;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ApplicationContextResolver implements Resolver<Class<?>>, ApplicationContextAware {

  private ApplicationContext applicationContext;
  private URI baseUri;

  public ApplicationContextResolver(URI baseUri) {
    this.baseUri = baseUri;
  }

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override public boolean supports(URI uri, Object target) {
    if (null == target) {
      return false;
    }

    if (uri.isAbsolute()) {
      List<URI> uris = UriUtils.explode(baseUri, uri);
      if (uris.size() > 0) {
        uri = uris.get(uris.size() - 1);
      }
    }

    return (null != uri.getPath() && target instanceof Class);
  }

  @Override public Object resolve(URI uri, Class<?> target) {
    return applicationContext.getBean(uri.getPath(), target);
  }

}
