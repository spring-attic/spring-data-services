package org.springframework.data.services.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaRepositoryMetadata implements InitializingBean, ApplicationContextAware {

  private ApplicationContext applicationContext;
  private Map<Class<?>, RepositoryCacheEntry> repositories = new HashMap<Class<?>, RepositoryCacheEntry>();

  public CrudRepository repositoryFor(Class<?> domainClass) {
    RepositoryCacheEntry entry = repositories.get(domainClass);
    if (null != entry) {
      return entry.repository;
    }
    return null;
  }

  public EntityInformation entityInfoFor(Class<?> domainClass) {
    RepositoryCacheEntry entry = repositories.get(domainClass);
    if (null != entry) {
      return entry.entityInfo;
    }
    return null;
  }

  public EntityInformation entityInfoFor(CrudRepository repository) {
    for (Map.Entry<Class<?>, RepositoryCacheEntry> entry : repositories.entrySet()) {
      if (entry.getValue().repository == repository) {
        return entry.getValue().entityInfo;
      }
    }
    return null;
  }

  public String repositoryNameFor(Class<?> domainClass) {
    RepositoryCacheEntry entry = repositories.get(domainClass);
    if (null != entry) {
      return entry.name;
    }
    return null;
  }

  public String repositoryNameFor(CrudRepository repository) {
    for (Map.Entry<Class<?>, RepositoryCacheEntry> entry : repositories.entrySet()) {
      if (entry.getValue().repository == repository) {
        return entry.getValue().name;
      }
    }
    return null;
  }

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setRepositories(Collection<CrudRepository> repositories) {
    for (CrudRepository repository : repositories) {
      Class<?> repoClass = AopUtils.getTargetClass(repository);
      Field infoField = ReflectionUtils.findField(repoClass, "entityInformation");
      ReflectionUtils.makeAccessible(infoField);
      Method m = ReflectionUtils.findMethod(repository.getClass(), "getTargetSource");
      ReflectionUtils.makeAccessible(m);
      try {
        SingletonTargetSource targetRepo = (SingletonTargetSource) m.invoke(repository);
        EntityInformation entityInfo = (EntityInformation) infoField.get(targetRepo.getTarget());
        String name = StringUtils.uncapitalize(repoClass.getSimpleName().replaceAll("Repository", ""));
        this.repositories.put(entityInfo.getJavaType(), new RepositoryCacheEntry(name, repository, entityInfo));
      } catch (Throwable t) {
        throw new IllegalStateException(t);
      }
    }
  }

  @Override public void afterPropertiesSet() throws Exception {
    if (this.repositories.isEmpty()) {
      setRepositories(applicationContext.getBeansOfType(CrudRepository.class).values());
    }
  }

  private class RepositoryCacheEntry {
    String name;
    CrudRepository repository;
    EntityInformation entityInfo;

    private RepositoryCacheEntry(String name, CrudRepository repository, EntityInformation entityInfo) {
      this.name = name;
      this.repository = repository;
      this.entityInfo = entityInfo;
    }
  }

}
