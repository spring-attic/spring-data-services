package org.springframework.data.services.exporter.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public class RestExporterController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private List<RestExporter> exporters = Collections.emptyList();
  @Autowired(required = false)
  private List<HttpMessageConverter> messageConverters = new ArrayList<HttpMessageConverter>();
  @Autowired(required = false)
  private ConfigurableConversionService conversionService = new DefaultConversionService();

  {
    messageConverters.add(new MappingJacksonHttpMessageConverter());
    messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
    messageConverters.add(new AtomFeedHttpMessageConverter());
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(new ByteArrayHttpMessageConverter());
  }

  public List<RestExporter> getExporters() {
    return exporters;
  }

  public RestExporterController setExporters(List<RestExporter> exporters) {
    this.exporters = exporters;
    return this;
  }

  public List<HttpMessageConverter> getMessageConverters() {
    return messageConverters;
  }

  public RestExporterController setMessageConverters(List<HttpMessageConverter> messageConverters) {
    this.messageConverters = messageConverters;
    return this;
  }

  public ConfigurableConversionService getConversionService() {
    return conversionService;
  }

  public RestExporterController setConversionService(ConfigurableConversionService conversionService) {
    this.conversionService = conversionService;
    return this;
  }

  @SuppressWarnings({"unchecked"})
  @RequestMapping(value = "/**", method = RequestMethod.GET)
  public void get(HttpServletRequest servletRequest, Model model) {
    HttpRequest request = new ServletServerHttpRequest(servletRequest);

    for (RestExporter exporter : exporters) {
      if (exporter.supports(request)) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().
            scheme(request.getURI().getScheme()).
            host(request.getHeaders().getFirst("Host")).
            path(exporter.getBaseUrl());
        String uri = servletRequest.getRequestURI();
        List<String> uriParts = parseUrl(exporter.getBaseUrl(), uri);
        if (uriParts.size() == 1) {
          // GET for all entities
          String name = uriParts.get(0);
          uriBuilder.pathSegment(name);
          Iterable entities = exporter.getRepository().findAll();
          List<Link> links = new ArrayList<Link>();
          if (null != entities) {
            String urlSoFar = uriBuilder.build().toUriString();
            Iterator iter = entities.iterator();
            while (iter.hasNext()) {
              Object o = iter.next();
              Object id = exporter.getEntityInfo().getId(o);
              String sid = conversionService.convert(id, String.class);
              String location = UriComponentsBuilder.fromHttpUrl(urlSoFar).pathSegment(sid).build().toUriString();
              links.add(new Link("self", location));
            }
          }
          Map linksObj = new HashMap();
          linksObj.put("links", links);
          model.addAttribute("links", linksObj);
          return;
        } else if (uriParts.size() == 2) {
          // GET for a specific entity
          String id = uriParts.get(1);

          Object oid = (
              ClassUtils.isAssignable(exporter.targetIdType(), Serializable.class) ?
                  id :
                  conversionService.convert(id, exporter.targetIdType())
          );
          Object domainObj = exporter.getRepository().findOne((Serializable) oid);
          if (null != domainObj) {
            model.addAttribute("body", domainObj);
            return;
          }
        }
      }
    }

    addBaseLinks(request, model);
  }

  @SuppressWarnings({"unchecked"})
  @RequestMapping(value = "/**", method = {RequestMethod.POST, RequestMethod.PUT})
  public void post(HttpServletRequest servletRequest, final HttpEntity<byte[]> entity, Model model) {
    HttpRequest request = new ServletServerHttpRequest(servletRequest);

    for (RestExporter exporter : exporters) {
      if (exporter.supports(request)) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().
            scheme(request.getURI().getScheme()).
            host(request.getHeaders().getFirst("Host")).
            path(exporter.getBaseUrl());
        String uri = servletRequest.getRequestURI();
        List<String> uriParts = parseUrl(exporter.getBaseUrl(), uri);
        uriBuilder.pathSegment(uriParts.get(0));

        Class<?> targetType = exporter.targetType();
        for (HttpMessageConverter converter : messageConverters) {
          if (converter.canRead(targetType, request.getHeaders().getContentType())) {
            try {
              Object o = converter.read(targetType, new HttpInputMessage() {
                @Override public InputStream getBody() throws IOException {
                  return new ByteArrayInputStream(entity.getBody());
                }

                @Override public HttpHeaders getHeaders() {
                  return entity.getHeaders();
                }
              });
              Object domainObj = exporter.getRepository().save(o);
              Object id = exporter.getEntityInfo().getId(domainObj);
              String sid = conversionService.convert(id, String.class);
              uriBuilder.pathSegment(sid);

              String location = uriBuilder.build().toUriString();
              if (request.getMethod().equals(HttpMethod.POST)) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Location", location);
                model.addAttribute("status", HttpStatus.CREATED);
                model.addAttribute("headers", headers);
              } else {
                model.addAttribute("status", HttpStatus.NO_CONTENT);
              }
              return;
            } catch (IOException e) {
              log.error(e.getMessage(), e);
              model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR);
              model.addAttribute("body", e.getMessage());
            }
          }
        }
      }
    }

    addBaseLinks(request, model);
  }

  @SuppressWarnings({"unchecked"})
  @RequestMapping(value = "/**", method = RequestMethod.DELETE)
  public void delete(HttpServletRequest servletRequest, Model model) {
    HttpRequest request = new ServletServerHttpRequest(servletRequest);

    for (RestExporter exporter : exporters) {
      if (exporter.supports(request)) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().
            scheme(request.getURI().getScheme()).
            host(request.getHeaders().getFirst("Host")).
            path(exporter.getBaseUrl());
        String uri = servletRequest.getRequestURI();
        List<String> uriParts = parseUrl(exporter.getBaseUrl(), uri);
        if (uriParts.size() == 1) {
          // DELETE for all entities
          String name = uriParts.get(0);
          uriBuilder.pathSegment(name);
          Iterable entities = exporter.getRepository().findAll();
          exporter.getRepository().delete(entities);
          model.addAttribute("status", HttpStatus.NO_CONTENT);
          return;
        } else if (uriParts.size() == 2) {
          // DELETE for a specific entity
          String id = uriParts.get(1);

          Object oid = (
              ClassUtils.isAssignable(exporter.targetIdType(), Serializable.class) ?
                  id :
                  conversionService.convert(id, exporter.targetIdType())
          );
          exporter.getRepository().delete(oid);
          model.addAttribute("status", HttpStatus.NO_CONTENT);
          return;
        }
      }
    }

    addBaseLinks(request, model);
  }

  @SuppressWarnings({"unchecked"})
  private void addBaseLinks(HttpRequest request, Model model) {
    List<Link> links = new ArrayList<Link>();
    for (RestExporter exp : exporters) {
      String location = UriComponentsBuilder.newInstance().
          scheme(request.getURI().getScheme()).
          host(request.getHeaders().getFirst("Host")).
          path(exp.getBaseUrl()).
          pathSegment(exp.getName()).
          build().
          toUriString();
      links.add(new Link(exp.getName(), location));
    }
    Map linksObj = new HashMap();
    linksObj.put("links", links);
    model.addAttribute("status", HttpStatus.OK);
    model.addAttribute("links", linksObj);
  }

  private List<String> parseUrl(String baseUri, String uri) {
    String resourceUri = uri.replace(baseUri, "");
    String[] parts = resourceUri.split("/");
    List<String> partsl = new ArrayList<String>();
    for (String part : parts) {
      if (!"".equals(part.trim())) {
        partsl.add(part);
      }
    }
    return partsl;
  }

}
