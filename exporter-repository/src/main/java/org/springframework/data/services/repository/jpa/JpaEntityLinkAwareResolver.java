package org.springframework.data.services.repository.jpa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.services.Handler;
import org.springframework.data.services.Link;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityLinkAwareResolver implements Resolver {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private URI baseUri;
  private EntityManager entityManager;
  private Metamodel metamodel;
  private JpaRepositoryMetadata repositoryMetadata;
  private Cache<EntityType, JpaEntityMetadata> metadataCache = CacheBuilder.newBuilder().
      build(new CacheLoader<EntityType, JpaEntityMetadata>() {
        @Override public JpaEntityMetadata load(EntityType key) throws Exception {
          return new JpaEntityMetadata(key, repositoryMetadata);
        }
      });

  public JpaEntityLinkAwareResolver(URI baseUri,
                                    JpaRepositoryMetadata repositoryMetadata) {
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

  @SuppressWarnings({"unchecked"})
  @Override public Object resolve(final URI uri, final Object target) {
    final Map<String, Object> model = new HashMap<String, Object>();
    final List<Link> links = new ArrayList<Link>();

    EntityType entityType = metamodel.entity(target.getClass());
    try {
      final JpaEntityMetadata metadata = metadataCache.get(entityType);
      metadata.doWithEmbedded(new Handler<Attribute>() {
        @Override public void handle(Attribute attr) {
          String name = attr.getName();
          Object val = metadata.get(name, target);
          model.put(name, val);
        }
      });
      metadata.doWithLinked(new Handler<Attribute>() {
        @Override public void handle(Attribute attr) {
          final String name = attr.getName();
          try {
            links.add(new SimpleLink(name, UriUtils.merge(uri, new URI(name))));
          } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
          }
        }
      });
    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }

    if (links.size() > 0) {
      model.put(Link.LINKS, links);
    }

    return model;
  }

}
