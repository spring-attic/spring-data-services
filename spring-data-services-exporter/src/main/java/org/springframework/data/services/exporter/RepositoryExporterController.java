package org.springframework.data.services.exporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public class RepositoryExporterController implements ApplicationContextAware, InitializingBean {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryExporterController.class);

  private static final List<MediaType> PRODUCIBLE_TYPES = new ArrayList<MediaType>() {{
    add(MediaType.APPLICATION_JSON);
    add(MediaType.APPLICATION_OCTET_STREAM);
  }};
  private ApplicationContext applicationContext;
  private Map<String, MetadataEntry> repositories = new HashMap<String, MetadataEntry>();
  private List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
  private ConfigurableConversionService conversionService = new DefaultConversionService();

  public RepositoryExporterController() {
    converters.add(new StringHttpMessageConverter());
    converters.add(new ByteArrayHttpMessageConverter());
    converters.add(new MappingJacksonHttpMessageConverter());
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public Map<String, CrudRepository<Object, Serializable>> getRepositories() {
    Map<String, CrudRepository<Object, Serializable>> repos = new HashMap<String, CrudRepository<Object, Serializable>>();
    for (Map.Entry<String, MetadataEntry> entry : this.repositories.entrySet()) {
      repos.put(entry.getKey(), entry.getValue().repository);
    }
    return repos;
  }

  @SuppressWarnings({"unchecked"})
  public RepositoryExporterController setRepositories(Map<String, CrudRepository> repositories) {
    Assert.notNull(repositories, "Repository Map cannot be null.");
    for (Map.Entry<String, CrudRepository> entry : repositories.entrySet()) {
      DefaultRepositoryMetadata repoInfo = new DefaultRepositoryMetadata(entry.getValue().getClass());
      CrudRepository repo = entry.getValue();
      Class<?> domainClass = repoInfo.getDomainClass();

      // We have to do this to get access to the EntityInformation stored inside the
      // Repository implementation. Unfortunately, there's no other easy way to do this a.t.m.
      Class<?> repoClass = AopUtils.getTargetClass(repo);
      Field infoField = ReflectionUtils.findField(repoClass, "entityInformation");
      ReflectionUtils.makeAccessible(infoField);
      Method m = ReflectionUtils.findMethod(repo.getClass(), "getTargetSource");
      ReflectionUtils.makeAccessible(m);
      try {
        SingletonTargetSource targetRepo = (SingletonTargetSource) m.invoke(repo);
        JpaEntityInformation entityInfo = (JpaEntityInformation) infoField.get(targetRepo.getTarget());
        String key = StringUtils.uncapitalize(domainClass.getSimpleName().replaceAll("Repository", ""));

        this.repositories.put(key, new MetadataEntry(entityInfo, repo));
      } catch (Throwable t) {
        throw new IllegalStateException(t);
      }
    }
    return this;
  }

  @SuppressWarnings({"unchecked"})
  public void afterPropertiesSet() throws Exception {
    if (repositories.size() == 0) {
      setRepositories(BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, CrudRepository.class));
    }
  }

  @SuppressWarnings({"unchecked"})
  @ResponseBody
  @RequestMapping(value = "/{repository}/{id}", method = RequestMethod.HEAD)
  public ResponseEntity head(@PathVariable String repository,
                             @PathVariable String id,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
    MetadataEntry entry = repositories.get(repository);
    if (null != entry) {
      CrudRepository<Object, Serializable> repo = entry.repository;
      Class<? extends Serializable> domainIdClass = entry.metadata.getIdType();
      Serializable domainId;
      if (ClassUtils.isAssignable(domainIdClass, String.class)) {
        domainId = id;
      } else {
        domainId = conversionService.convert(id, domainIdClass);
      }
      Object domainObj = repo.findOne(domainId);
      if (null != domainObj) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity(headers, HttpStatus.OK);
      }
    }

    return new ResponseEntity(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/{repository}", method = RequestMethod.GET)
  public void get(@PathVariable String repository,
                  HttpServletRequest request,
                  HttpServletResponse response) throws Exception {

  }

  @SuppressWarnings({"unchecked"})
  @ResponseBody
  @RequestMapping(value = "/{repository}/{id}", method = RequestMethod.GET)
  public ResponseEntity get(@PathVariable String repository,
                            @PathVariable String id,
                            HttpServletRequest request) throws Exception {

    String accept = request.getHeader("Accept");
    if (null == accept || "*/*".equals(accept)) {
      accept = "application/json";
    }
    List<MediaType> acceptableTypes = MediaType.parseMediaTypes(accept);
    MediaType.sortByQualityValue(acceptableTypes);
    MediaType responseType = null;
    for (MediaType mt : acceptableTypes) {
      for (MediaType mt2 : PRODUCIBLE_TYPES) {
        if (mt.getType().equals(mt2.getType()) && mt.getSubtype().equals(mt2.getSubtype())) {
          responseType = mt2;
          break;
        }
      }
    }
    if (null == responseType && acceptableTypes.contains(MediaType.ALL)) {
      responseType = MediaType.APPLICATION_JSON;
    } else if (null == responseType) {
      return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
    }

    MetadataEntry entry = repositories.get(repository);
    if (null != entry) {
      CrudRepository<Object, Serializable> repo = entry.repository;
      Class<? extends Serializable> domainIdClass = entry.metadata.getIdType();
      Serializable domainId;
      if (ClassUtils.isAssignable(domainIdClass, String.class)) {
        domainId = id;
      } else {
        domainId = conversionService.convert(id, domainIdClass);
      }
      Object domainObj = repo.findOne(domainId);
      if (null != domainObj) {
        Class<?> domainClass = entry.metadata.getJavaType();
        for (HttpMessageConverter converter : converters) {
          if (converter.canWrite(domainClass, responseType)) {
            final HttpHeaders headers = new HttpHeaders();
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            HttpOutputMessage output = new HttpOutputMessage() {
              public OutputStream getBody() throws IOException {
                return bout;
              }

              public HttpHeaders getHeaders() {
                return headers;
              }
            };
            converter.write(domainObj, responseType, output);

            String body = new String(bout.toByteArray());
            if (null != request.getParameter("callback")) {
              body = request.getParameter("callback") + "(" + body + ")";
              headers.setContentType(MediaType.parseMediaType("text/javascript"));
            } else {
              headers.setContentType(responseType);
            }
            return new ResponseEntity(body, headers, HttpStatus.OK);
          }
        }
        return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
      }
    }

    return new ResponseEntity(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/{repository}/{id}", method = RequestMethod.PUT)
  public void put(HttpServletRequest request, HttpServletResponse response) throws Exception {

  }

  @SuppressWarnings({"unchecked"})
  @RequestMapping(value = "/{repository}", method = RequestMethod.POST)
  public ResponseEntity post(@PathVariable String repository, HttpServletRequest request) throws Exception {
    HttpHeaders headers = new HttpHeaders();

    MetadataEntry entry = repositories.get(repository);
    if (null != entry) {
      CrudRepository<Object, Serializable> repo = entry.repository;
      Class<?> domainClass = entry.metadata.getJavaType();
      for (HttpMessageConverter converter : converters) {
        if (converter.canRead(domainClass, MediaType.valueOf(request.getContentType()))) {
          Object domainObj = converter.read(domainClass, new ServletServerHttpRequest(request));
          repo.save(domainObj);
          headers.set("Location", String.format("/%s/%s", repository, entry.metadata.getId(domainObj)));
          return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        }
      }
    }
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }

  @RequestMapping(value = "/{repository}/{id}", method = RequestMethod.DELETE)
  public void delete(HttpServletRequest request, HttpServletResponse response) throws Exception {

  }

  private MediaType negotiateAcceptableMediaType(String accept) {
    if (null == accept || "*/*".equals(accept)) {
      accept = "application/json";
    }
    List<MediaType> acceptableTypes = MediaType.parseMediaTypes(accept);
    MediaType.sortByQualityValue(acceptableTypes);
    MediaType responseType = null;
    for (MediaType mt : acceptableTypes) {
      for (MediaType mt2 : PRODUCIBLE_TYPES) {
        if (mt.getType().equals(mt2.getType()) && mt.getSubtype().equals(mt2.getSubtype())) {
          responseType = mt2;
          break;
        }
      }
    }
    if (null == responseType && acceptableTypes.contains(MediaType.ALL)) {
      responseType = MediaType.APPLICATION_JSON;
    }
    return responseType;
  }

  private static class MetadataEntry {
    EntityInformation<Object, Serializable> metadata;
    CrudRepository<Object, Serializable> repository;

    private MetadataEntry(EntityInformation<Object, Serializable> metadata, CrudRepository<Object, Serializable> repository) {
      this.metadata = metadata;
      this.repository = repository;
    }
  }
}
