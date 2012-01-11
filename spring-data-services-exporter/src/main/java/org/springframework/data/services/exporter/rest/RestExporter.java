package org.springframework.data.services.exporter.rest;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class RestExporter {

  private String baseUrl = "/";
  private String name;
  private CrudRepository repository;
  private List<HttpMethod> allowedMethods = Collections.emptyList();
  private EntityInformation entityInfo;

  public boolean supports(HttpRequest request) {
    if (!allowedMethods.contains(request.getMethod())) {
      return false;
    }
    String uri = UriComponentsBuilder.newInstance().
        path(baseUrl).
        pathSegment(name).
        build().
        toUriString();

    return (request.getURI().getPath().startsWith(uri));
  }

  public Class<?> targetType() {
    return entityInfo.getJavaType();
  }

  public Class<? extends Serializable> targetIdType() {
    return entityInfo.getIdType();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public RestExporter setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public String getName() {
    return name;
  }

  public RestExporter setName(String name) {
    this.name = name;
    return this;
  }

  public EntityInformation getEntityInfo() {
    return entityInfo;
  }

  public RestExporter setEntityInfo(EntityInformation entityInfo) {
    this.entityInfo = entityInfo;
    return this;
  }

  public CrudRepository getRepository() {
    return repository;
  }

  public RestExporter setRepository(CrudRepository repository) {
    this.repository = repository;
    DefaultRepositoryMetadata repoInfo = new DefaultRepositoryMetadata(repository.getClass());
    Class<?> domainClass = repoInfo.getDomainClass();
    Class<?> repoClass = AopUtils.getTargetClass(repository);
    Field infoField = ReflectionUtils.findField(repoClass, "entityInformation");
    ReflectionUtils.makeAccessible(infoField);
    Method m = ReflectionUtils.findMethod(repository.getClass(), "getTargetSource");
    ReflectionUtils.makeAccessible(m);
    try {
      SingletonTargetSource targetRepo = (SingletonTargetSource) m.invoke(repository);
      entityInfo = (JpaEntityInformation) infoField.get(targetRepo.getTarget());
      name = StringUtils.uncapitalize(domainClass.getSimpleName().replaceAll("Repository", ""));
    } catch (Throwable t) {
      throw new IllegalStateException(t);
    }
    return this;
  }

  public List<HttpMethod> getAllowedMethods() {
    return allowedMethods;
  }

  public RestExporter setAllowedMethods(List<HttpMethod> allowedMethods) {
    this.allowedMethods = allowedMethods;
    return this;
  }

}
