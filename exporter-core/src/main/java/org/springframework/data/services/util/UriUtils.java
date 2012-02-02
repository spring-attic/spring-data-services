package org.springframework.data.services.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.services.Handler;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class UriUtils {

  public static <V> V foreach(URI baseUri, URI uri, Handler<URI, V> handler) {
    List<URI> uris = explode(baseUri, uri);
    V v = null;
    for (URI u : uris) {
      v = handler.handle(u);
    }
    return v;
  }

  public static List<URI> explode(URI baseUri, URI uri) {
    List<URI> uris = new ArrayList<URI>();
    URI relativeUri = baseUri.relativize(uri);
    try {
      for (String part : relativeUri.getPath().split("/")) {
        uris.add(new URI(part));
      }
      if (StringUtils.hasText(relativeUri.getFragment())) {
        uris.add(new URI("#" + relativeUri.getFragment()));
      }
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
    return uris;
  }

  public static URI merge(URI baseUri, URI... uris) {
    StringBuilder query = new StringBuilder();

    UriComponentsBuilder ub = UriComponentsBuilder.fromUri(baseUri);
    for (URI uri : uris) {
      String s = uri.getScheme();
      if (null != s) {
        ub.scheme(s);
      }

      s = uri.getUserInfo();
      if (null != s) {
        ub.userInfo(s);
      }

      s = uri.getHost();
      if (null != s) {
        ub.host(s);
      }

      int i = uri.getPort();
      if (i > 0) {
        ub.port(i);
      }

      s = uri.getPath();
      if (null != s) {
        if (!uri.isAbsolute() && StringUtils.hasText(s)) {
          ub.pathSegment(s);
        } else {
          ub.path(s);
        }
      }

      s = uri.getQuery();
      if (null != s) {
        if (query.length() > 0) {
          query.append("&");
        }
        query.append(s);
      }

      s = uri.getFragment();
      if (null != s) {
        ub.fragment(s);
      }
    }

    if (query.length() > 0) {
      ub.query(query.toString());
    }

    return ub.build().toUri();
  }

  public static int fragmentToInt(URI uri) {
    String s = uri.getFragment();
    if (null != s) {
      return Integer.parseInt(s);
    } else {
      return -1;
    }
  }

  public static URI tail(URI baseUri, URI uri) {
    List<URI> uris = explode(baseUri, uri);
    return uris.get(Math.max(uris.size() - 1, 0));
  }

}
