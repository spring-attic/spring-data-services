package org.springframework.data.services.repository.jpa;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.util.BeanUtils;
import org.springframework.data.services.util.UriUtils;
import org.springframework.util.ClassUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityResourceResolver extends AbstractResourceResolver {

  private JpaRepositoryMetadata metadata;
  private ConversionService conversionService;

  public JpaEntityResourceResolver(URI baseUri, JpaRepositoryMetadata metadata, ConversionService conversionService) {
    super(baseUri);
    this.metadata = metadata;
    this.conversionService = conversionService;
  }

  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    Object o = BeanUtils.tail(resources).target();
    return (o instanceof CrudRepository);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) {
    CrudRepository repo = (CrudRepository) BeanUtils.tail(resources).target();
    EntityInformation entityInfo = metadata.entityInfoFor(repo);
    URI uriTail = UriUtils.tail(baseUri, uri);
    String sId = UriUtils.path(uriTail);

    Serializable serId;
    Class<? extends Serializable> idType = entityInfo.getIdType();
    if (ClassUtils.isAssignable(idType, String.class)) {
      serId = sId;
    } else {
      serId = conversionService.convert(sId, idType);
    }

    return new SimpleResource(uriTail, repo.findOne(serId));
  }

}
