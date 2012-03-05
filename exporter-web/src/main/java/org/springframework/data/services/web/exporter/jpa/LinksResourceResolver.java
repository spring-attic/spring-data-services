package org.springframework.data.services.web.exporter.jpa;

import java.net.URI;
import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.ResolverException;
import org.springframework.data.services.Resource;
import org.springframework.data.services.repository.jpa.JpaEntityMetadata;
import org.springframework.data.services.repository.jpa.JpaRepositoryMetadata;
import org.springframework.data.services.util.ResourceUtils;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class LinksResourceResolver extends AbstractResourceResolver {

  private JpaRepositoryMetadata metadata;
  private ConversionService conversionService;

  public LinksResourceResolver(URI baseUri,
                               JpaRepositoryMetadata metadata,
                               ConversionService conversionService) {
    super(baseUri);
    this.metadata = metadata;
    this.conversionService = conversionService;
  }

  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    String name = UriUtils.path(uri);
    Object entity = ResourceUtils.tail(resources);
    JpaEntityMetadata entityMeta = metadata.entityMetadataFor(entity.getClass());
    if (null != entityMeta) {
      return entityMeta.linkedAttributes().containsKey(name);
    }
    return false;
  }

  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) throws ResolverException {
    String name = UriUtils.path(uri);
    Object entity = ResourceUtils.tail(resources);
    JpaEntityMetadata entityMeta = metadata.entityMetadataFor(entity.getClass());
    return null;
  }

}
