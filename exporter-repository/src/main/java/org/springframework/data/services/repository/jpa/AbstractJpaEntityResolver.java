package org.springframework.data.services.repository.jpa;

import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.services.Resolver;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public abstract class AbstractJpaEntityResolver implements Resolver {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected URI baseUri;
  protected EntityManager entityManager;
  protected Metamodel metamodel;
  protected JpaRepositoryMetadata repositoryMetadata;
  protected Cache<EntityType, JpaEntityMetadata> metadataCache = CacheBuilder.newBuilder().
      build(new CacheLoader<EntityType, JpaEntityMetadata>() {
        @Override public JpaEntityMetadata load(EntityType key) throws Exception {
          return new JpaEntityMetadata(key, repositoryMetadata);
        }
      });

  protected AbstractJpaEntityResolver(URI baseUri, JpaRepositoryMetadata repositoryMetadata) {
    this.baseUri = baseUri;
    this.repositoryMetadata = repositoryMetadata;
  }

  @PersistenceContext
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.metamodel = entityManager.getMetamodel();
  }

  @Override public boolean supports(URI uri, Object target) {
    return null != target && null != metamodel.entity(target.getClass());
  }

}
