package org.springframework.data.services.web.exporter.jpa;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.persistence.metamodel.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.ResolverException;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.repository.jpa.JpaEntityMetadata;
import org.springframework.data.services.repository.jpa.JpaRepositoryMetadata;
import org.springframework.data.services.util.ResourceUtils;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.ClassUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class HttpEntityJpaRepositoryResourceResolver extends AbstractResourceResolver {

  private static final Logger LOG = LoggerFactory.getLogger(HttpEntityJpaRepositoryResourceResolver.class);

  private ConversionService conversionService;
  private JpaRepositoryMetadata metadata;

  public HttpEntityJpaRepositoryResourceResolver(URI baseUri,
                                                 JpaRepositoryMetadata metadata,
                                                 ConversionService conversionService) {
    super(baseUri);
    this.metadata = metadata;
    this.conversionService = conversionService;
  }

  @SuppressWarnings({"unchecked"})
  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    ServerHttpRequest request = ResourceUtils.find(ServerHttpRequest.class, resources);
    HttpEntity entity = ResourceUtils.find(HttpEntity.class, resources);
    return (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT)
        && (null != entity)
        && (entity.getBody() instanceof Map)
        && (null != ResourceUtils.find(CrudRepository.class, resources));
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) throws ResolverException {
    ServerHttpRequest request = ResourceUtils.find(ServerHttpRequest.class, resources);
    CrudRepository repo = ResourceUtils.find(CrudRepository.class, resources);
    EntityInformation entityInfo = metadata.entityInfoFor(repo);
    Class<? extends Serializable> idType = entityInfo.getIdType();
    Class<?> domainClass = entityInfo.getJavaType();
    JpaEntityMetadata entityMeta = metadata.entityMetadataFor(domainClass);
    HttpEntity httpEntity = ResourceUtils.find(HttpEntity.class, resources);

    Serializable id;
    Object entity = null;
    if (request.getMethod() == HttpMethod.POST) {
      try {
        entity = domainClass.newInstance();
      } catch (InstantiationException e) {
        throw new ResolverException(e.getMessage(), e, httpEntity);
      } catch (IllegalAccessException e) {
        throw new ResolverException(e.getMessage(), e, httpEntity);
      }

      if (!uri.isAbsolute()) {
        id = path(uri, idType);
        entityMeta.id(id, entity);
      }
    } else if (request.getMethod() == HttpMethod.PUT) {
      id = path(uri, idType);
      entity = repo.findOne(id);
    }

    Map model = (Map) httpEntity.getBody();
    for (Attribute attr : entityMeta.embeddedAttributes().values()) {
      LOG.debug("attr: " + attr);
      String name = attr.getName();
      if (model.containsKey(name)) {
        Object val = model.get(name);
        Class<?> type = attr.getJavaType();
        if (!ClassUtils.isAssignable(val.getClass(), type) && conversionService.canConvert(val.getClass(), type)) {
          val = conversionService.convert(val, type);
        }
        entityMeta.set(name, val, entity);
      }
    }

    Object savedObj = repo.save(entity);
    resources.remove(httpEntity);

    return new SimpleResource<Object>(uri, savedObj);
  }

  @SuppressWarnings({"unchecked"})
  private <T> T path(URI uri, Class<T> targetType) {
    String s = UriUtils.path(uri);
    if (targetType == String.class) {
      return (T) s;
    } else if (conversionService.canConvert(String.class, targetType)) {
      return conversionService.convert(s, targetType);
    } else {
      throw new IllegalArgumentException("No converter found for String -> " + targetType.getSimpleName() + " conversion.");
    }
  }

}
