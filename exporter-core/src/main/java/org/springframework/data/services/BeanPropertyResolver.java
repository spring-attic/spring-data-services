package org.springframework.data.services;

import java.lang.reflect.Field;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class BeanPropertyResolver implements Resolver {

  @Override public boolean supports(URI uri, Object target) {
    return (null != uri.getFragment() && null != target);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Object resolve(URI uri, Object target) {
    String name = uri.getFragment();
    if (null != name) {
      return get(name, target);
    }
    return null;
  }

  protected Object get(String name, Object target) {
    Field field = ReflectionUtils.findField(target.getClass(), name);
    ReflectionUtils.makeAccessible(field);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

}
