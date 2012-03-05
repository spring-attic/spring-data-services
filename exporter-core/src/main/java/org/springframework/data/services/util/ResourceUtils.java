package org.springframework.data.services.util;

import java.util.List;

import org.springframework.data.services.Resource;
import org.springframework.util.ClassUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class ResourceUtils {

  private ResourceUtils() {
  }

  @SuppressWarnings({"unchecked"})
  public static <T> T find(Class<? extends T> targetType, final List<Resource<?>> stack) {
    for (int i = stack.size() - 1; i >= 0; i--) {
      Resource<?> r = stack.get(i);
      if (ClassUtils.isAssignable(targetType, r.target().getClass())) {
        return (T) r.target();
      }
    }
    return null;
  }

  public static Object tail(List<Resource<?>> resources) {
    int size = resources.size();
    if (size > 0) {
      return resources.get(size - 1).target();
    }
    return null;
  }

}
