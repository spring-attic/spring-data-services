package org.springframework.data.services.web.exporter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.services.DelegatingResolver;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
@RequestMapping("/**")
public class ResourceExporterController implements ApplicationContextAware, InitializingBean {

  private ApplicationContext applicationContext;
  private String host;
  private URI baseUri;
  private DelegatingResolver readResolvers = new DelegatingResolver();

  public String getHost() {
    return host;
  }

  public ResourceExporterController setHost(String host) {
    this.host = host;
    return this;
  }

  public URI getBaseUri() {
    return baseUri;
  }

  public ResourceExporterController setBaseUri(URI baseUri) {
    this.baseUri = baseUri;
    return this;
  }

  public List<Resolver> getReadResolvers() {
    return readResolvers.resolvers();
  }

  public ResourceExporterController setReadResolvers(List<Resolver> readResolvers) {
    Assert.notNull(readResolvers, "Read resolvers cannot be null.");
    this.readResolvers.resolvers().addAll(readResolvers);
    return this;
  }


  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override public void afterPropertiesSet() throws Exception {

  }

  @RequestMapping(method = RequestMethod.HEAD)
  public void head(HttpServletRequest request, Model model) {
    try {
      URI requestUri = new URI(request.getRequestURL().toString());
      System.out.println("URI: " + requestUri);

      Object o = null;
      for (URI uri : UriUtils.explode(baseUri, requestUri)) {
        o = readResolvers.resolve(uri, o);
        System.out.println("o: " + o);
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

  @Transactional(readOnly = true)
  @RequestMapping(method = RequestMethod.GET)
  public void get(HttpServletRequest request, Model model) {
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

  @RequestMapping(method = RequestMethod.POST)
  public void post(HttpServletRequest request, HttpEntity<byte[]> input, Model model) {

  }

  @RequestMapping(method = RequestMethod.PUT)
  public void put(HttpServletRequest request, HttpEntity<byte[]> input, Model model) {

  }

  @RequestMapping(method = RequestMethod.DELETE)
  public void delete(HttpServletRequest request, Model model) {

  }


}
