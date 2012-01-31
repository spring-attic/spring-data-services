package org.springframework.data.services.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.springframework.util.ReflectionUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class FluentBeanDeserializer extends StdDeserializer {

  private Map<String, Method> setters = new HashMap<String, Method>();

  @SuppressWarnings({"unchecked"})
  public FluentBeanDeserializer(final Class<?> valueClass) {
    super(valueClass);
    ReflectionUtils.doWithFields(valueClass, new ReflectionUtils.FieldCallback() {
      @Override public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        final String fname = field.getName();
        ReflectionUtils.doWithMethods(valueClass, new ReflectionUtils.MethodCallback() {
          @Override public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
            String mname = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            if (mname.equals(fname)) {
              if (paramTypes.length == 1) {
                setters.put(fname, method);
              }
            }
          }
        });
      }
    });
  }

  @Override
  public Object deserialize(JsonParser jp,
                            DeserializationContext ctxt)
      throws IOException,
             JsonProcessingException {
    if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
      throw ctxt.mappingException(_valueClass);
    }

    return null;
  }

}
