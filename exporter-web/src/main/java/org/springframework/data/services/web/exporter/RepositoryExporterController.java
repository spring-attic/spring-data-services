package org.springframework.data.services.web.exporter;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.services.Link;
import org.springframework.data.services.SimpleLink;
import org.springframework.data.services.repository.jpa.JpaRepositoryMetadata;
import org.springframework.data.services.util.UriUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public class RepositoryExporterController {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryExporterController.class);
  private static final MediaType URI_LIST = MediaType.parseMediaType("text/uri-list");

  private URI baseUri;
  @Autowired
  private JpaRepositoryMetadata repositoryMetadata;
  @Autowired
  private ConversionService conversionService;

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

  @SuppressWarnings({"unchecked"})
  @RequestMapping(method = RequestMethod.GET)
  public void get(ServerHttpRequest request, Model model) {
    if (validBaseUri(request.getURI())) {

      URI relativeUri = baseUri.relativize(request.getURI());
      if (StringUtils.hasText(relativeUri.getPath())) {

        List<URI> uris = UriUtils.explode(baseUri, relativeUri);
        if (LOG.isDebugEnabled()) {
          LOG.debug("uris: " + uris);
        }

        String repoName = uris.get(0).getPath();
        CrudRepository repo = repositoryMetadata.repositoryFor(repoName);
        EntityInformation entityInfo = repositoryMetadata.entityInfoFor(repo);
        EntityType entityType = repositoryMetadata.entityTypeFor(entityInfo.getJavaType());

        if (uris.size() == 1) {
          // List the entities
          model.addAttribute("status", HttpStatus.OK);

          List<Link> links = new ArrayList<Link>();
          Iterator iter = repo.findAll().iterator();
          while (iter.hasNext()) {
            Object o = iter.next();
            Serializable id = entityInfo.getId(o);
            links.add(new SimpleLink("resource",
                                     UriComponentsBuilder.fromUri(baseUri)
                                         .pathSegment(repoName)
                                         .pathSegment(id.toString())
                                         .build()
                                         .toUri())
            );
          }

          model.addAttribute("resource", links);
        } else if (uris.size() == 2) {
          // Retrieve an entity
          String sId = uris.get(1).getPath().replaceAll("/", "");
          Class<? extends Serializable> idType = entityInfo.getIdType();

          Serializable serId;
          if (idType == String.class) {
            serId = sId;
          } else {
            serId = conversionService.convert(sId, idType);
          }

          Object entity = repo.findOne(serId);
          if (null == entity) {
            model.addAttribute("status", HttpStatus.NOT_FOUND);
          } else {

          }
        }
      } else {
        // List the repositories
        model.addAttribute("status", HttpStatus.OK);

        List<Link> links = new ArrayList<Link>();
        for (String repo : repositoryMetadata.repositoryNames()) {
          links.add(new SimpleLink("resource",
                                   UriComponentsBuilder.fromUri(baseUri)
                                       .pathSegment(repo)
                                       .build()
                                       .toUri())
          );
        }

        model.addAttribute("resource", links);
      }
    } else {
      model.addAttribute("status", HttpStatus.NOT_FOUND);
    }
  }

  private boolean validBaseUri(URI requestUri) {
    String path = baseUri.relativize(requestUri).getPath();
    return StringUtils.hasText(path) ? path.charAt(0) != '/' : true;
  }

}
