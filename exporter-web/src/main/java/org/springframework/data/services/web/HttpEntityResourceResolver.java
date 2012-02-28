package org.springframework.data.services.web;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.util.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpEntityResourceResolver extends AbstractResourceResolver {

  private static final Class<?> BYTES = byte[].class;

  @Autowired
  private List<HttpMessageConverter<?>> httpMessageConverters;

  public HttpEntityResourceResolver(URI baseUri) {
    super(baseUri);
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

  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    Resource<?> r = BeanUtils.tail(resources);
    return uri.isAbsolute() && (r.target() instanceof ServerHttpRequest);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) {
    ServerHttpRequest request = (ServerHttpRequest) BeanUtils.tail(resources).target();

    for (HttpMessageConverter converter : httpMessageConverters) {
      if (converter.canRead(BYTES, request.getHeaders().getContentType())) {
        try {
          byte[] bytes = (byte[]) converter.read(BYTES, request);
          return new SimpleResource(uri, new HttpEntity(bytes, request.getHeaders()));
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    return null;
  }

}
