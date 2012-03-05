package org.springframework.data.services.web.exporter.jpa;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.metamodel.Attribute;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.AbstractResourceResolver;
import org.springframework.data.services.Link;
import org.springframework.data.services.ResolverException;
import org.springframework.data.services.Resource;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.SimpleResource;
import org.springframework.data.services.repository.jpa.JpaEntityMetadata;
import org.springframework.data.services.repository.jpa.JpaRepositoryMetadata;
import org.springframework.data.services.util.ResourceUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class LinkAwareJpaResourceResolver extends AbstractResourceResolver {

  private JpaRepositoryMetadata metadata;
  private ConversionService conversionService;

  public LinkAwareJpaResourceResolver(URI baseUri,
                                      JpaRepositoryMetadata metadata,
                                      ConversionService conversionService) {
    super(baseUri);
    this.metadata = metadata;
    this.conversionService = conversionService;
  }

  @Override public boolean supports(URI uri, List<Resource<?>> resources) {
    int size = resources.size();
    if (size > 1) {
      if (resources.get(size - 2).target() instanceof CrudRepository) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings({"unchecked"})
  @Override public Resource<?> resolve(URI uri, List<Resource<?>> resources) throws ResolverException {
    ServerHttpRequest request = ResourceUtils.find(ServerHttpRequest.class, resources);
    int size = resources.size();
    Object entity = resources.get(size - 1).target();
    CrudRepository repo = (CrudRepository) resources.get(size - 2).target();
    EntityInformation entityInfo = metadata.entityInfoFor(repo);
    Class<?> domainClass = entityInfo.getJavaType();
    String sId = conversionService.convert(entityInfo.getId(entity), String.class);
    JpaEntityMetadata entityMeta = metadata.entityMetadataFor(domainClass);
    HttpHeaders headers = new HttpHeaders();

    Object vers = entityMeta.version(entity);
    if (null != vers) {
      Class<?> versType = vers.getClass();
      List<String> l = request.getHeaders().getIfNoneMatch();
      for (String s : l) {
        Object etag = conversionService.convert(s.substring(1, s.length() - 1), versType);
        if (etag == vers || etag.equals(vers)) {
          return new SimpleResource(uri, new ResponseEntity(HttpStatus.NOT_MODIFIED));
        }
      }
      String etag = conversionService.convert(vers, String.class);
      headers.setETag("\"" + etag + "\"");
    }

    Map model = new HashMap();
    for (Attribute attr : entityMeta.embeddedAttributes().values()) {
      String name = attr.getName();
      Object val = entityMeta.get(name, entity);
      model.put(name, val);
    }

    List<Link> links = new ArrayList<Link>();
    for (Attribute attr : entityMeta.linkedAttributes().values()) {
      String name = attr.getName();
      URI link = UriComponentsBuilder.fromUri(baseUri).
          pathSegment(metadata.repositoryNameFor(repo), sId, name).
          build().
          toUri();
      links.add(new SimpleLink(name, link));
    }
    if (links.size() > 0) {
      model.put("_links", links);
    }

    return new SimpleResource(uri, new ResponseEntity(model, headers, HttpStatus.OK));
  }

  @SuppressWarnings({"unchecked"})
  private URI createLink(Object entity) {
    if (null != entity) {
      Class<?> domainClass = entity.getClass();
      CrudRepository repo = metadata.repositoryFor(domainClass);
      String repoName = metadata.repositoryNameFor(repo);
      EntityInformation entityInfo = metadata.entityInfoFor(domainClass);
      Object id = entityInfo.getId(entity);
      String sId = conversionService.convert(id, String.class);
      return UriComponentsBuilder.fromUri(baseUri).
          pathSegment(repoName, sId).
          build().
          toUri();
    }
    return null;
  }

}
