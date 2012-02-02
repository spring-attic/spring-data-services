package org.springframework.data.services.repository.jpa;

import java.net.URI;

import org.springframework.http.HttpEntity;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityLinkAwareWriteResolver extends AbstractJpaEntityResolver {

  public JpaEntityLinkAwareWriteResolver(URI baseUri, JpaRepositoryMetadata repositoryMetadata) {
    super(baseUri, repositoryMetadata);
  }

  @Override public boolean supports(URI uri, Object target) {
    if (null == target) {
      return false;
    }
    return target instanceof HttpEntity;
  }

  @SuppressWarnings({"unchecked"})
  @Override public Object resolve(URI uri, Object target) {
    HttpEntity<byte[]> entity = (HttpEntity<byte[]>) target;

    if (uri.isAbsolute()) {

    }

    return null;
  }

}
