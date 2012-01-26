package org.springframework.data.services.repository

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.data.services.util.UriUtils
import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
class JpaRepositoryEntityResolverSpec extends Specification {

  ApplicationContext appCtx
  SimpleEntityRepository repository
  JpaRepositoryEntityResolver resolver

  def setup() {
    appCtx = new ClassPathXmlApplicationContext("JpaRepositoryEntityResolver-test.xml")
    repository = appCtx.getBean(SimpleEntityRepository)
    resolver = appCtx.getBean(JpaRepositoryEntityResolver)
  }

  def "resolves an entity using findOne"() {

    when:
    def entity = new SimpleEntity(name: "John Doe")
    repository.save(entity)
    def id = entity.id
    def entity2 = resolver.resolve(new URI(id.toString()), repository)

    then:
    entity.equals(entity2)

  }

  def "resolves an entity using findOne and an absolute URI"() {

    given:
    def baseUri = new URI("http://localhost:8080/data/simpleEntity")

    when:
    def entity = new SimpleEntity(name: "John Doe")
    repository.save(entity)
    def id = entity.id
    def entity2 = resolver.resolve(UriUtils.merge(baseUri, new URI(id.toString())), repository)

    then:
    entity.equals(entity2)

  }

}

@Entity
class SimpleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id
  String name

  @Override boolean equals(Object o) {
    if (o instanceof SimpleEntity) {
      SimpleEntity se = (SimpleEntity) o
      return se.id.equals(id) && se.name.equals(name)
    }
    return super.equals(o)
  }


}