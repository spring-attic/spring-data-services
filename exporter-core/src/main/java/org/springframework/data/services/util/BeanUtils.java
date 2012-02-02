package org.springframework.data.services.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class BeanUtils {

  private static final Cache<Class<?>, Field[]> fields = CacheBuilder.newBuilder().build(
      new CacheLoader<Class<?>, Field[]>() {
        @Override public Field[] load(Class<?> clazz) throws Exception {
          return clazz.getDeclaredFields();
        }
      }
  );
  private static final Cache<Object[], Method> getters = CacheBuilder.newBuilder().build(
      new CacheLoader<Object[], Method>() {
        @Override public Method load(Object[] key) throws Exception {
          Class<?> clazz = (Class<?>) key[0];
          String name = "get" + StringUtils.capitalize((String) key[1]);
          return ReflectionUtils.findMethod(clazz, name);
        }
      }
  );

  public static Object findFirst(String property, Object... objs) {
    for (Object obj : objs) {
      Class<?> type = obj.getClass();
      try {
        for (Field f : fields.get(type)) {
          String name = f.getName();
          if (name.equals(property)) {
            if (FluentBeanUtils.isFluentBean(type)) {
              return FluentBeanUtils.get(name, obj);
            } else {
              Method getter = getters.get(new Object[]{type, name});
              try {
                if (null != getter) {
                  return getter.invoke(obj);
                } else {
                  return f.get(obj);
                }
              } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
              } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
              }
            }
          }
        }
      } catch (ExecutionException e) {
        throw new IllegalArgumentException(e);
      }
    }

    return null;
  }

}
