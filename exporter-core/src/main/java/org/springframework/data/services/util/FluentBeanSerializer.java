package org.springframework.data.services.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class FluentBeanSerializer extends SerializerBase {

  private Map<String, Method> getters = new HashMap<String, Method>();

  @SuppressWarnings({"unchecked"})
  public FluentBeanSerializer(final Class<?> t) {
    super(t);
    ReflectionUtils.doWithFields(t, new ReflectionUtils.FieldCallback() {
      @Override public void doWith(Field f) throws IllegalArgumentException, IllegalAccessException {
        final String fname = f.getName();
        ReflectionUtils.doWithMethods(t, new ReflectionUtils.MethodCallback() {
          @Override public void doWith(Method m) throws IllegalArgumentException, IllegalAccessException {
            String mname = m.getName();
            Class<?>[] paramTypes = m.getParameterTypes();
            if (mname.equals(fname)) {
              if (paramTypes.length == 0) {
                getters.put(fname, m);
              }
            }
          }
        });
      }
    });
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public void serialize(final Object value,
                        final JsonGenerator jgen,
                        final SerializerProvider provider)
      throws IOException,
             JsonGenerationException {
    if (null == value) {
      provider.defaultSerializeNull(jgen);
    } else {
      Class<?> type = value.getClass();
      if (ClassUtils.isAssignable(type, Collection.class)) {
        jgen.writeStartArray();
        for (Object o : (Collection) value) {
          write(o, jgen, provider);
        }
        jgen.writeEndArray();
      } else if (ClassUtils.isAssignable(type, Map.class)) {
        jgen.writeStartObject();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
          jgen.writeFieldName(entry.getKey());
          write(entry.getValue(), jgen, provider);
        }
        jgen.writeEndObject();
      } else {
        write(value, jgen, provider);
      }
    }
  }

  private void write(final Object value,
                     final JsonGenerator jgen,
                     final SerializerProvider provider) throws IOException {
    Class<?> type = value.getClass();
    if (ClassUtils.isAssignable(type, _handledType)) {
      jgen.writeStartObject();
      ReflectionUtils.doWithFields(
          type,
          new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
              String name = field.getName();
              try {
                jgen.writeFieldName(name);
              } catch (IOException e) {
                throw new IllegalStateException(e);
              }
              Method getter = getters.get(name);
              Object o;
              if (null != getter) {
                try {
                  o = getter.invoke(value);
                } catch (InvocationTargetException e) {
                  throw new IllegalStateException(e);
                }
              } else {
                o = field.get(value);
              }

              try {
                if (null == o) {
                  provider.defaultSerializeNull(jgen);
                } else {
                  jgen.writeObject(o);
                }
              } catch (IOException e) {
                throw new IllegalStateException(e);
              }
            }
          },
          new ReflectionUtils.FieldFilter() {
            @Override public boolean matches(Field field) {
              if (!field.isAnnotationPresent(JsonIgnore.class)) {
                String name = field.getName();
                if (!name.startsWith("_")) {
                  if (null != ReflectionUtils.findMethod(field.getDeclaringClass(), name)) {
                    return true;
                  } else {
                    // This field doesn't have a getter and isn't publicly-accessible.
                  }
                }
              }
              return false;
            }
          }
      );
      jgen.writeEndObject();
    } else {
      jgen.writeObject(value);
    }
  }

}
