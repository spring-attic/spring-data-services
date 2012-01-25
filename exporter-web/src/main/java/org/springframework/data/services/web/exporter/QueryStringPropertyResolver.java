package org.springframework.data.services.web.exporter;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.services.BeanPropertyResolver;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class QueryStringPropertyResolver extends BeanPropertyResolver {

  private String name;

  public QueryStringPropertyResolver(String name) {
    this.name = name;
  }

  @Override public boolean supports(URI uri, Object target) {
    return (null != target && null != uri.getQuery());
  }

  @Override public Object resolve(URI uri, Object target) {
    UriComponents components = UriComponentsBuilder.fromUri(uri).build();
    MultiValueMap<String, String> params = components.getQueryParams();
    List<String> values = params.get(name);
    if (values.size() == 1) {
      return get(values.get(0), target);
    } else if (values.size() > 1) {
      Map<String, Object> returnVals = new HashMap<String, Object>();
      for (String name : values) {
        returnVals.put(name, get(name, target));
      }
      return returnVals;
    }
    return null;
  }

}
