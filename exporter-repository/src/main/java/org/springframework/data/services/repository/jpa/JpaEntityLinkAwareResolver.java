package org.springframework.data.services.repository.jpa;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.springframework.data.services.Handler;
import org.springframework.data.services.Link;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.util.UriUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityLinkAwareResolver extends AbstractJpaEntityResolver {

  public JpaEntityLinkAwareResolver(URI baseUri, JpaRepositoryMetadata repositoryMetadata) {
    super(baseUri, repositoryMetadata);
  }

  @SuppressWarnings({"unchecked"})
  @Override public Object resolve(final URI uri, final Object target) {
    final Map<String, Object> model = new HashMap<String, Object>();
    final List<Link> links = new ArrayList<Link>();

    EntityType entityType = metamodel.entity(target.getClass());
    try {
      final JpaEntityMetadata metadata = metadataCache.get(entityType);
      metadata.
          doWithEmbedded(new Handler<Attribute, JpaEntityMetadata>() {
            @Override public JpaEntityMetadata handle(Attribute attr) {
              String name = attr.getName();
              Object val = metadata.get(name, target);
              model.put(name, val);
              return metadata;
            }
          }).
          doWithLinked(new Handler<Attribute, JpaEntityMetadata>() {
            @Override public JpaEntityMetadata handle(Attribute attr) {
              final String name = attr.getName();
              try {
                links.add(new SimpleLink(name, UriUtils.merge(uri, new URI(name))));
              } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
              }
              return metadata;
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
