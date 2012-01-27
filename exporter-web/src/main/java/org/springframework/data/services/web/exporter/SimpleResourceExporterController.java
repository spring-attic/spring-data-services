package org.springframework.data.services.web.exporter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.services.DelegatingResolver;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.util.Assert;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleResourceExporterController implements ResourceExporterController {

  private ApplicationContext applicationContext;
  private String host;
  private URI baseUri;
  private DelegatingResolver readResolvers = new DelegatingResolver();

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

  public List<Resolver> getReadResolvers() {
    return readResolvers.resolvers();
  }

  public SimpleResourceExporterController setReadResolvers(List<Resolver> readResolvers) {
    Assert.notNull(readResolvers, "Read resolvers cannot be null.");
    this.readResolvers.resolvers().addAll(readResolvers);
    return this;
  }


  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override public void afterPropertiesSet() throws Exception {

  }

  @Override public void get(HttpServletRequest request, Model model) {
    try {
      URI requestUri = new URI(request.getRequestURL().toString());
      System.out.println("URI: " + requestUri);

      Object o = null;
      for (URI uri : UriUtils.explode(baseUri, requestUri)) {
        o = readResolvers.resolve(uri, o);
        System.out.println("o: " + o);
      }
      if (readResolvers.supports(requestUri, o)) {
        o = readResolvers.resolve(requestUri, o);
      }
      if (null != o) {
        model.addAttribute("status", HttpStatus.OK);
        model.addAttribute("resource", o);
      } else {
        model.addAttribute("status", HttpStatus.NOT_FOUND);
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

}
