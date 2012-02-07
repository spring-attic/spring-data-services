package org.springframework.data.services.repository.jpa;

import java.net.URI;

import org.springframework.data.services.Resource;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityLinkAwareWriteResourceHandler extends AbstractJpaEntityResourceHandler {

  public JpaEntityLinkAwareWriteResourceHandler(URI baseUri, JpaRepositoryMetadata repositoryMetadata) {
    super(baseUri, repositoryMetadata);
  }

  @Override public boolean supports(Resource resource, Object... args) {
    return false;
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource handle(Resource resource, Object... args) {
    return null;
  }

}
