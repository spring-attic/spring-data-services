package org.springframework.data.services.repository.jpa;

import java.net.URI;
import java.util.List;

import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaRepositoryResourceResolver extends AbstractResourceResolver {

  private JpaRepositoryMetadata metadata;

  public JpaRepositoryResourceResolver(URI baseUri, JpaRepositoryMetadata metadata) {
    super(baseUri);
    this.metadata = metadata;
  }

  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    URI uriTail = UriUtils.tail(baseUri, uri);
    return null != metadata.repositoryFor(UriUtils.path(uriTail));
  }

  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) {
    URI uriTail = UriUtils.tail(baseUri, uri);
    return new SimpleResource(uri, metadata.repositoryFor(UriUtils.path(uriTail)));
  }

}
