package org.springframework.data.services.web;

import java.io.IOException;
import java.net.URI;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.services.util.UriUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BaseUriFilter implements Filter {

  private URI baseUri;

  public BaseUriFilter() {
  }

  @Override public void init(FilterConfig filterConfig) throws ServletException {
    String baseUri = filterConfig.getInitParameter("baseUri");
    this.baseUri = UriUtils.parseUri(baseUri);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    ServerHttpRequest httpRequest = new ServletServerHttpRequest((HttpServletRequest) request);
    String path = UriUtils.path(baseUri.relativize(httpRequest.getURI()));
    boolean validUri = !StringUtils.hasText(path) || path.charAt(0) != '/';
    if (validUri) {
      chain.doFilter(request, response);
    } else {
      HttpServletResponse servletResponse = (HttpServletResponse) response;
      servletResponse.setStatus(404);
    }
  }

  @Override public void destroy() {
  }

}
