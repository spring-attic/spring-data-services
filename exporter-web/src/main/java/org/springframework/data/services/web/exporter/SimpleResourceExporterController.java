package org.springframework.data.services.web.exporter;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.services.DelegatingResolver;
import org.springframework.data.services.Handler;
import org.springframework.data.services.Link;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.ResourceHandler;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleResourceExporterController implements ResourceExporterController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private ApplicationContext applicationContext;
  private String host;
  private URI baseUri;
  private Resolver<Link> linkResolver;
  private ResourceHandler resourceHandler;
  private ResourceHandler readHandler;
  private ResourceHandler writeHandler;

  public String getHost() {
    return host;
  }

  public SimpleResourceExporterController setHost(String host) {
    this.host = host;
    return this;
  }

  public URI getBaseUri() {
    return baseUri;
  }

  public SimpleResourceExporterController setBaseUri(URI baseUri) {
    this.baseUri = baseUri;
    return this;
  }

  public ResourceHandler getResourceHandler() {
    return resourceHandler;
  }

  public SimpleResourceExporterController setResourceHandler(ResourceHandler resourceHandler) {
    this.resourceHandler = resourceHandler;
    return this;
  }

  public ResourceHandler getReadHandler() {
    return readHandler;
  }

  public SimpleResourceExporterController setReadHandler(ResourceHandler readHandler) {
    this.readHandler = readHandler;
    return this;
  }

  public ResourceHandler getWriteHandler() {
    return writeHandler;
  }

  public SimpleResourceExporterController setWriteHandler(ResourceHandler writeHandler) {
    this.writeHandler = writeHandler;
    return this;
  }

  public Resolver<Link> getLinkResolver() {
    return linkResolver;
  }

  public SimpleResourceExporterController setLinkResolver(Resolver<Link> linkResolver) {
    this.linkResolver = linkResolver;
    return this;
  }

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override public void afterPropertiesSet() throws Exception {

  }

  @SuppressWarnings({"unchecked"})
  @Override public void get(HttpServletRequest request, Model model) {
    URI requestUri = requestUri(request);
    Object resource = resolveResource(requestUri);
    if (null != resource && null != readHandler) {
      Object obj = readHandler.handle(requestUri, resource);
      if (log.isDebugEnabled()) {
        log.debug("Resolved for GET: " + obj);
      }

      model.addAttribute("status", HttpStatus.OK);
      model.addAttribute("resource", resource);
    } else {
      model.addAttribute("status", HttpStatus.NOT_FOUND);
    }
  }

  @SuppressWarnings({"unchecked"})
  public void post(HttpServletRequest request, HttpEntity<byte[]> entity, Model model) {
    URI requestUri = requestUri(request);
    Object resource = resolveResource(requestUri);
    if (null != resource && null != writeHandler) {
      writeHandler.handle(requestUri, resource, entity);
      if (log.isDebugEnabled()) {
        log.debug("Resolved for POST: " + resource);
        log.debug("Body: " + new String(entity.getBody()).trim());
      }

      model.addAttribute("status", HttpStatus.CREATED);
    } else {
      model.addAttribute("status", HttpStatus.NOT_FOUND);
    }
  }

  private URI requestUri(HttpServletRequest request) {
    try {
      return new URI(request.getRequestURL().toString());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Cannot create URI.");
    }
  }

  @SuppressWarnings({"unchecked"})
  private Object resolveResource(URI uri) {
    if (log.isDebugEnabled()) {
      log.debug("Resolving URI " + uri);
    }
    return UriUtils.foreach(baseUri, uri, new Handler<URI, Object>() {
      Object o = null;

      @Override public Object handle(URI u) {
        o = resourceHandler.handle(u, o);
        if (log.isDebugEnabled()) {
          log.debug("  ...resolved o=" + o + " from " + u);
        }
        return o;
      }
    });
  }

}
