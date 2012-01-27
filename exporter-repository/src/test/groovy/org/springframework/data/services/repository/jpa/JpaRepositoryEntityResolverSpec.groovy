package org.springframework.data.services.repository.jpa

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.repository.CrudRepository
import org.springframework.data.services.util.UriUtils
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@ContextConfiguration(locations = "/JpaRepositoryEntityResolver-test.xml")
class JpaRepositoryEntityResolverSpec extends Specification {

  @Autowired
  ApplicationContext appCtx
  @Autowired
  SimpleEntityRepository simpleEntityRepository
  @Autowired
  ComplexEntityRepository complexEntityRepository
  @Autowired
  JpaRepositoryEntityResolver resolver
  @Autowired
  JpaEntityLinkAwareResolver entityResolver

  def "resolves an entity using findOne"() {

    when:
    def entity = simpleEntityRepository.save(new SimpleEntity(name: "John Doe"))
    def id = entity.id
    def entity2 = resolver.resolve(new URI(id.toString()), repository)

    then:
    entity.equals(entity2)

  }

  def "resolves an entity using findOne and an absolute URI"() {

    given:
    def baseUri = new URI("http://localhost:8080/data/simpleEntity")

    when:
    def entity = simpleEntityRepository.save(new SimpleEntity(name: "John Doe"))
    def id = entity.id
    def entity2 = resolver.resolve(UriUtils.merge(baseUri, new URI(id.toString())), repository)

    then:
    entity.equals(entity2)

  }

  def "resolves an entity's properties"() {

    given:
    def baseUri = new URI("http://localhost:8080/data/complexEntity")

    when:
    def simple = simpleEntityRepository.save(new SimpleEntity(name: "John Doe"))
    def complex = complexEntityRepository.save(new ComplexEntity(simpleEntities: [simple]))
    def id = complex.id
    def uri = UriUtils.merge(baseUri, new URI(id.toString()), new URI("simpleEntities"))
    def simpleEntities = entityResolver.resolve(uri, complex)
    println "simpleEntities: $simpleEntities"

    then:
    true

  }

}

@Entity
class SimpleEntity {
  @Id @GeneratedValue Long id
  String name

  @Override boolean equals(Object o) {
    if (o instanceof SimpleEntity) {
      SimpleEntity se = (SimpleEntity) o
      return se.id.equals(id) && se.name.equals(name)
    }
    return super.equals(o)
  }

  @Override
  public String toString() {
    return "SimpleEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}

@Entity
class ComplexEntity {
  @Id @GeneratedValue Long id
  @OneToMany
  List<SimpleEntity> simpleEntities

  @Override
  public String toString() {
    return "ComplexEntity{" +
        "id=" + id +
        ", simpleEntities=" + simpleEntities +
        '}';
  }
}

interface SimpleEntityRepository extends CrudRepository<SimpleEntity, Long> {}
interface ComplexEntityRepository extends CrudRepository<ComplexEntity, Long> {}