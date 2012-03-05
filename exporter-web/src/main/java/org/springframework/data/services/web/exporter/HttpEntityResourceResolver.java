package org.springframework.data.services.web.exporter;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.ResolverException;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.util.ResourceUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpEntityResourceResolver extends AbstractResourceResolver {

  @Autowired
  private List<HttpMessageConverter<?>> httpMessageConverters;

  public HttpEntityResourceResolver(URI baseUri) {
    super(baseUri);
  }

  public HttpEntityResourceResolver(URI baseUri, List<HttpMessageConverter<?>> httpMessageConverters) {
    super(baseUri);
    this.httpMessageConverters = httpMessageConverters;
  }

  public List<HttpMessageConverter<?>> getHttpMessageConverters() {
    return httpMessageConverters;
  }

  public void setHttpMessageConverters(List<HttpMessageConverter<?>> httpMessageConverters) {
    this.httpMessageConverters = httpMessageConverters;
  }

  public List<HttpMessageConverter<?>> httpMessageConverters() {
    return httpMessageConverters;
  }

  public HttpEntityResourceResolver httpMessageConverters(List<HttpMessageConverter<?>> httpMessageConverters) {
    this.httpMessageConverters = httpMessageConverters;
    return this;
  }

  @SuppressWarnings({"unchecked"})
  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    if(resources.size() == 1) {
      ServerHttpRequest request = (ServerHttpRequest) resources.get(resources.size() - 1).target();
      if (uri.isAbsolute() && (request.getMethod() == HttpMethod.PUT || request.getMethod() == HttpMethod.POST)) {
        MediaType contentType = request.getHeaders().getContentType();
        for (HttpMessageConverter converter : httpMessageConverters) {
          if (converter.canRead(Map.class, contentType)) {
            return true;
          } else if (converter.canRead(List.class, contentType)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) throws ResolverException {
    ServerHttpRequest request = ResourceUtils.find(ServerHttpRequest.class, resources);
    MediaType contentType = request.getHeaders().getContentType();
    for (HttpMessageConverter converter : httpMessageConverters) {
      try {
        if (converter.canRead(Map.class, contentType)) {
          Map model = (Map) converter.read(Map.class, request);
          return new SimpleResource<HttpEntity<Map>>(uri, new HttpEntity<Map>(model, request.getHeaders()));
        } else if (converter.canRead(List.class, contentType)) {
          List uris = (List) converter.read(List.class, request);
          return new SimpleResource<HttpEntity<List<?>>>(uri, new HttpEntity<List<?>>(uris, request.getHeaders()));
        }
      } catch (IOException e) {
        throw new ResolverException(e.getMessage(), e, uri);
      }
    }
    return null;
  }

}
