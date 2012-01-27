package org.springframework.data.services.repository.jpa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.Handler;
import org.springframework.data.services.Link;
import org.springframework.data.services.Resolver;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.util.UriUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityLinkAwareResolver implements Resolver {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private URI baseUri;
  private TransactionTemplate transactionTemplate;
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
                                    JpaRepositoryMetadata repositoryMetadata,
                                    TransactionTemplate transactionTemplate) {
    this.baseUri = baseUri;
    this.repositoryMetadata = repositoryMetadata;
    this.transactionTemplate = transactionTemplate;
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
          final Object val = metadata.get(name, target);
          if (attr.isCollection()) {
            transactionTemplate.execute(new TransactionCallback<Object>() {
              @Override public Object doInTransaction(TransactionStatus status) {
                try {
                  for (Object o : (Collection) val) {
                    EntityInformation entityInfo = repositoryMetadata.entityInfoFor(o.getClass());
                    Link l = new SimpleLink(name, UriUtils.merge(uri, new URI(entityInfo.getId(o).toString())));
                    links.add(l);
                  }
                } catch (URISyntaxException e) {
                  throw new IllegalStateException(e);
                }
                return null;
              }
            });
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

  @SuppressWarnings({"unchecked"})
  private List<Link> links(Map<String, Object> model) {
    List<Link> links = (List<Link>) model.get(Link.LINKS);
    if (null == links) {
      links = new ArrayList<Link>();
      model.put(Link.LINKS, links);
    }
    return links;
  }

}
