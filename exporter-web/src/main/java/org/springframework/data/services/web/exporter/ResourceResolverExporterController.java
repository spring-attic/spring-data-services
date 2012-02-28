package org.springframework.data.services.web.exporter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.services.DelegatingResourceResolver;
import org.springframework.data.services.Resource;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public class ResourceResolverExporterController implements InitializingBean {

  public static final String STATUS = "status";
  public static final String HEADERS = "headers";
  public static final String RESOURCE = "resource";

  private static final Logger LOG = LoggerFactory.getLogger(ResourceResolverExporterController.class);

  private URI baseUri;
  @Autowired
  private Map<HttpMethod, DelegatingResourceResolver> resourceResolvers;

  public URI getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(URI baseUri) {
    this.baseUri = baseUri;
  }

  public URI baseUri() {
    return baseUri;
  }

  public ResourceResolverExporterController baseUri(URI baseUri) {
    this.baseUri = baseUri;
    return this;
  }

  public Map<HttpMethod, DelegatingResourceResolver> getResourceResolvers() {
    return resourceResolvers;
  }

  @SuppressWarnings({"unchecked"})
  public void setResourceResolvers(Map<Object, DelegatingResourceResolver> resourceResolvers) {
    this.resourceResolvers = new HashMap<HttpMethod, DelegatingResourceResolver>(resourceResolvers.size());
    for (Map.Entry<Object, DelegatingResourceResolver> entry : resourceResolvers.entrySet()) {
      if (entry.getKey() instanceof HttpMethod) {
        this.resourceResolvers.put((HttpMethod) entry.getKey(), entry.getValue());
      } else {
        this.resourceResolvers.put(HttpMethod.valueOf(entry.getKey().toString()), entry.getValue());
      }
    }
  }

  public Map<HttpMethod, DelegatingResourceResolver> resourceResolvers() {
    return resourceResolvers;
  }

  public ResourceResolverExporterController resourceResolvers(Map<Object, DelegatingResourceResolver> resourceResolvers) {
    setResourceResolvers(resourceResolvers);
    return this;
  }

  @Override public void afterPropertiesSet() throws Exception {

  }

  @RequestMapping(method = {
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.DELETE
  })
  public void handle(ServerHttpRequest request, Model model) {
    DelegatingResourceResolver resolver = resourceResolvers.get(request.getMethod());
    if (null != resolver) {
      List<Resource<?>> stack = new ArrayList<Resource<?>>();
      Resource<?> resource = null;
      for (URI uri : UriUtils.explode(baseUri, request.getURI())) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("%s: resolving %s using %s", request.getMethod(), uri, resolver));
        }
        try {
          resource = resolver.resolve(uri, stack);
          if (null != resource) {
            stack.add(resource);
          }
        } catch (Exception e) {
          LOG.error(e.getMessage(), e);
          model.addAttribute(STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
          model.addAttribute(RESOURCE, e);
          return;
        }
      }
      if (null != resource) {
        model.addAttribute(STATUS, HttpStatus.OK);
        model.addAttribute(RESOURCE, resource);
      } else {
        model.addAttribute(STATUS, HttpStatus.NO_CONTENT);
      }
    } else {
      model.addAttribute(STATUS, HttpStatus.NOT_IMPLEMENTED);
    }
  }

}
