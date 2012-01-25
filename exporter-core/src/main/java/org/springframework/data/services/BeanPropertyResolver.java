package org.springframework.data.services;

import java.lang.reflect.Field;
import java.net.URI;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.util.ReflectionUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BeanPropertyResolver implements Resolver {

  protected ConfigurableConversionService conversionService;

  public BeanPropertyResolver(ConfigurableConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override public boolean supports(URI uri, Object target) {
    return (null != uri.getFragment() && null != target);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Object resolve(URI uri, Object target) {
    String name = uri.getFragment();
    if (null != name) {
      Field field = ReflectionUtils.findField(target.getClass(), name);
      ReflectionUtils.makeAccessible(field);
      try {
        return field.get(target);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
    return null;
  }

}
