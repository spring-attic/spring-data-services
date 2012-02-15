package org.springframework.data.services.web.exporter;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.Handler;
import org.springframework.data.services.Link;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.repository.jpa.JpaEntityMetadata;
import org.springframework.data.services.repository.jpa.JpaRepositoryMetadata;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public class RepositoryExporterController implements InitializingBean {

  public static final String STATUS = "status";
  public static final String HEADERS = "headers";
  public static final String RESOURCE = "resource";
  public static final String LINKS = "_links";
  public static final int LIST_REPOS = 0;
  public static final int LIST_ENTITIES = 1;
  public static final int HAS_ID = 2;
  public static final int LIST_LINKS = 3;
  public static final int HAS_CHILD_ID = 4;

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryExporterController.class);
  private static final MediaType URI_LIST = MediaType.parseMediaType("text/uri-list");

  @Autowired
  private URI baseUri;
  @Autowired
  private JpaRepositoryMetadata repositoryMetadata;
  @Autowired
  private ConversionService conversionService;
  @Autowired
  private TransactionTemplate transactionTemplate;

  public URI getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(URI baseUri) {
    this.baseUri = baseUri;
  }

  public URI baseUri() {
    return baseUri;
  }

  public RepositoryExporterController baseUri(URI baseUri) {
    this.baseUri = baseUri;
    return this;
  }

  public JpaRepositoryMetadata getRepositoryMetadata() {
    return repositoryMetadata;
  }

  public void setRepositoryMetadata(JpaRepositoryMetadata repositoryMetadata) {
    this.repositoryMetadata = repositoryMetadata;
  }

  public JpaRepositoryMetadata repositoryMetadata() {
    return repositoryMetadata;
  }

  public RepositoryExporterController repositoryMetadata(JpaRepositoryMetadata repositoryMetadata) {
    this.repositoryMetadata = repositoryMetadata;
    return this;
  }

  public ConversionService getConversionService() {
    return conversionService;
  }

  public void setConversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public ConversionService conversionService() {
    return conversionService;
  }

  public RepositoryExporterController conversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
    return this;
  }

  public TransactionTemplate getTransactionTemplate() {
    return transactionTemplate;
  }

  public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
  }

  public TransactionTemplate transactionTemplate() {
    return transactionTemplate;
  }

  public RepositoryExporterController transactionTemplate(TransactionTemplate transactionTemplate) {
    this.transactionTemplate = transactionTemplate;
    return this;
  }

  @Override public void afterPropertiesSet() throws Exception {
    if (null == conversionService) {
      conversionService = new DefaultConversionService();
    }
    Assert.notNull(transactionTemplate, "TransactionTemplate cannot be null");
  }

  @SuppressWarnings({"unchecked"})
  @RequestMapping(method = RequestMethod.GET)
  public void get(ServerHttpRequest request, final Model model) {
    if (validBaseUri(request.getURI())) {

      URI relativeUri = baseUri.relativize(request.getURI());
      final List<URI> uris = UriUtils.explode(baseUri, relativeUri);
      if (LOG.isDebugEnabled()) {
        LOG.debug("uris: " + uris);
      }

      final int uriCnt = uris.size();
      if (uris.size() > 0) {
        final String repoName = uris.get(0).getPath();
        final CrudRepository repo = repositoryMetadata.repositoryFor(repoName);
        final EntityInformation entityInfo = repositoryMetadata.entityInfoFor(repo);
        final Class<?> domainClass = entityInfo.getJavaType();
        final Class<? extends Serializable> idType = entityInfo.getIdType();
        final EntityType entityType = repositoryMetadata.entityTypeFor(domainClass);
        final JpaEntityMetadata entityMetadata = repositoryMetadata.entityMetadataFor(domainClass);

        switch (uriCnt) {

          // List the entities
          case LIST_ENTITIES: {
            Map<String, List<Link>> resource = new HashMap<String, List<Link>>();
            List<Link> links = new ArrayList<Link>();
            Iterator iter = repo.findAll().iterator();
            while (iter.hasNext()) {
              Object o = iter.next();
              Serializable id = entityInfo.getId(o);
              links.add(new SimpleLink(RESOURCE,
                                       UriComponentsBuilder.fromUri(baseUri)
                                           .pathSegment(repoName, id.toString())
                                           .build()
                                           .toUri())
              );
            }
            resource.put(LINKS, links);

            model.addAttribute(STATUS, HttpStatus.OK);
            model.addAttribute(RESOURCE, resource);
            return;
          }

          // Retrieve an entity
          case HAS_ID: {
            final String sId = UriUtils.path(uris.get(1));
            Serializable serId;
            if (idType == String.class) {
              serId = sId;
            } else {
              serId = conversionService.convert(sId, idType);
            }

            final Object entity = repo.findOne(serId);
            if (null == entity) {
              model.addAttribute(STATUS, HttpStatus.NOT_FOUND);
            } else {
              Map<String, Object> entityDto = transferPropertiesLinkAware(entity, entityMetadata, repoName, sId);
              if (LOG.isDebugEnabled()) {
                LOG.debug("entityDto: " + entityDto);
              }
              model.addAttribute(STATUS, HttpStatus.OK);
              model.addAttribute(RESOURCE, entityDto);
            }
            return;
          }

          // Retrieve the linked entities
          case LIST_LINKS:
            // Retrieve a child entity
          case HAS_CHILD_ID: {
            final String sId = UriUtils.path(uris.get(1));
            final Serializable serId;
            if (idType == String.class) {
              serId = sId;
            } else {
              serId = conversionService.convert(sId, idType);
            }

            transactionTemplate.execute(new TransactionCallback<Object>() {
              @Override public Object doInTransaction(TransactionStatus status) {
                final Object entity = repo.findOne(serId);
                if (null == entity) {
                  model.addAttribute(STATUS, HttpStatus.NOT_FOUND);
                } else {
                  model.addAttribute(STATUS, HttpStatus.OK);
                  final String attrName = UriUtils.path(uris.get(2));
                  Attribute attr = entityType.getAttribute(attrName);
                  if (null != attr) {
                    Class<?> childType;
                    if (attr instanceof PluralAttribute) {
                      childType = ((PluralAttribute) attr).getElementType().getJavaType();
                    } else {
                      childType = attr.getJavaType();
                    }
                    CrudRepository childRepo = repositoryMetadata.repositoryFor(childType);
                    if (null == childRepo) {
                      model.addAttribute(STATUS, HttpStatus.NOT_FOUND);
                      return null;
                    }
                    EntityInformation childEntityInfo = repositoryMetadata.entityInfoFor(childRepo);
                    JpaEntityMetadata childEntityMetadata = repositoryMetadata.entityMetadataFor(childEntityInfo.getJavaType());

                    Object child = entityMetadata.get(attrName, entity);
                    if (uriCnt == 3) {
                      Map<String, List<Link>> resource = new HashMap<String, List<Link>>();
                      List<Link> links = new ArrayList<Link>();
                      if (null != child) {
                        if (child instanceof Collection) {
                          for (Object o : (Collection) child) {
                            String childId = childEntityInfo.getId(o).toString();
                            URI uri = UriComponentsBuilder.fromUri(baseUri)
                                .pathSegment(repoName, sId, attrName, childId)
                                .build()
                                .toUri();
                            links.add(new SimpleLink(attrName, uri));
                          }
                          resource.put(LINKS, links);
                          model.addAttribute(RESOURCE, resource);
                        } else {
                          model.addAttribute(RESOURCE, child);
                        }
                      }
                    } else if (uriCnt == 4) {
                      String childId = UriUtils.path(uris.get(3));
                      Class<? extends Serializable> childIdType = childEntityInfo.getIdType();
                      final Serializable childSerId;
                      if (idType == String.class) {
                        childSerId = childId;
                      } else {
                        childSerId = conversionService.convert(childId, childIdType);
                      }

                      Object o = childRepo.findOne(childSerId);
                      if (null != o) {
                        Map<String, Object> entityDto = transferPropertiesLinkAware(o,
                                                                                    childEntityMetadata,
                                                                                    repoName,
                                                                                    sId,
                                                                                    attrName);
                        model.addAttribute(RESOURCE, entityDto);
                      } else {
                        model.addAttribute(STATUS, HttpStatus.NOT_FOUND);
                      }
                    }
                  }
                }
                return null;
              }
            });
            return;
          }

          // List the repositories
          default:
        }
      }
    } else {
      model.addAttribute(STATUS, HttpStatus.NOT_FOUND);
      return;
    }

    model.addAttribute(STATUS, HttpStatus.OK);

    Map<String, List<Link>> resource = new HashMap<String, List<Link>>();
    List<Link> links = new ArrayList<Link>();
    for (String name : repositoryMetadata.repositoryNames()) {
      links.add(new SimpleLink(RESOURCE,
                               UriComponentsBuilder.fromUri(baseUri)
                                   .pathSegment(name)
                                   .build()
                                   .toUri())
      );
    }
    resource.put(LINKS, links);

    model.addAttribute(RESOURCE, resource);
  }

  private boolean validBaseUri(URI requestUri) {
    String path = baseUri.relativize(requestUri).getPath();
    return !StringUtils.hasText(path) || path.charAt(0) != '/';
  }

  @SuppressWarnings({"unchecked"})
  private Map<String, Object> transferPropertiesLinkAware(final Object entity,
                                                          final JpaEntityMetadata entityMetadata,
                                                          final String... pathSegs) {
    final Map<String, Object> entityDto = new HashMap<String, Object>();

    entityMetadata.doWithEmbedded(new Handler<Attribute, Void>() {
      @Override public Void handle(Attribute attr) {
        String name = attr.getName();
        Object val = entityMetadata.get(name, entity);
        if (null != val) {
          entityDto.put(name, val);
        }
        return null;
      }
    });

    entityMetadata.doWithLinked(new Handler<Attribute, Void>() {
      @Override public Void handle(Attribute attr) {
        String name = attr.getName();
        URI uri = UriComponentsBuilder.fromUri(baseUri)
            .pathSegment(pathSegs)
            .pathSegment(name)
            .build()
            .toUri();
        Link l = new SimpleLink(name, uri);
        List<Link> links = (List<Link>) entityDto.get(LINKS);
        if (null == links) {
          links = new ArrayList<Link>();
          entityDto.put(LINKS, links);
        }
        links.add(l);
        return null;
      }
    });

    return entityDto;
  }

}
