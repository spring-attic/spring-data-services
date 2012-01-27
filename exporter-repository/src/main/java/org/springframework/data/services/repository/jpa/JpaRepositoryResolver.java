package org.springframework.data.services.repository.jpa;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaRepositoryResolver implements Resolver {

  private URI baseUri;
  private List<JpaRepositoryMetadata> metadata = new ArrayList<JpaRepositoryMetadata>();

  public JpaRepositoryResolver(URI baseUri) {
    this.baseUri = baseUri;
  }

  public JpaRepositoryResolver(URI baseUri, List<JpaRepositoryMetadata> metadata) {
    this.baseUri = baseUri;
    this.metadata = metadata;
  }

  public List<JpaRepositoryMetadata> getMetadata() {
    return metadata;
  }

  public JpaRepositoryResolver setMetadata(List<JpaRepositoryMetadata> metadata) {
    this.metadata = metadata;
    return this;
  }

  @Override public boolean supports(URI uri, Object target) {
    if (null == target) {
      return null != findRepositoryFor(getRelativeUri(uri));
    }
    return false;
  }

  @Override public Object resolve(URI uri, Object target) {
    if (uri.isAbsolute()) {
      uri = getRelativeUri(uri);
    }
    return findRepositoryFor(uri);
  }

  private URI getRelativeUri(URI uri) {
    if (uri.isAbsolute()) {
      List<URI> uris = UriUtils.explode(baseUri, uri);
      if (uris.size() > 0) {
        return uris.get(0);
      }
    }
    return uri;
  }

  private CrudRepository findRepositoryFor(URI uri) {
    for (JpaRepositoryMetadata metadata : this.metadata) {
      CrudRepository repository = metadata.repositoryFor(uri.getPath());
      if (null != repository) {
        return repository;
      }
    }
    return null;
  }

}
