package org.springframework.data.services;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.services.util.UriUtils;
import org.springframework.util.ClassUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public abstract class UriTailResourceHandler<T, R extends UriTailResourceHandler<T, ? super R>>
    implements ResourceHandler {

  protected final URI baseUri;
  private ConversionService conversionService = new DefaultConversionService();
  private Class<T> targetType;

  {
    for (Method m : getClass().getDeclaredMethods()) {
      if (m.getName().equals("handleTail")) {
        targetType = (Class<T>) m.getParameterTypes()[0];
        break;
      }
    }
  }

  protected UriTailResourceHandler(URI baseUri) {
    this.baseUri = baseUri;
  }

  public ConversionService conversionService() {
    return conversionService;
  }

  @SuppressWarnings({"unchecked"})
  public R conversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
    return (R) this;
  }

  @Override public Object handle(URI uri, Object... args) {
    URI tail = UriUtils.tail(baseUri, uri);
    if (ClassUtils.isAssignable(targetType, String.class)) {
      return handleTail((T) tail.getPath(), args);
    } else {
      T t = conversionService.convert(tail.getPath(), targetType);
      return handleTail(t, args);
    }
  }

  protected abstract Object handleTail(T tail, Object... args);

}
