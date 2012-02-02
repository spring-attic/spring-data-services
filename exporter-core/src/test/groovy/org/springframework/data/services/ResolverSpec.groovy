package org.springframework.data.services

import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.data.services.context.ApplicationContextResolver
import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
class ResolverSpec extends Specification {

  def "resolves simple properties based on the fragment"() {

    given:
    def resolver = new DelegatingResolver()
    resolver.resolvers().add(new ListResolver())
    resolver.resolvers().add(new MapResolver())
    resolver.resolvers().add(new BeanPropertyResolver())
    def list = ["first", "second", "third"]
    def map = ["first": "first", "second": "second", "third": "third"]
    def bean = new SimpleBean("third")

    when:
    def first = resolver.resolve(new URI("#0"), list)
    def second = resolver.resolve(new URI("#second"), map)
    def name = resolver.resolve(new URI("#name"), bean)

    then:
    first == "first"
    second == "second"
    name == "third"

  }

  def "resolves beans from the ApplicationContext"() {

    given:
    def baseUri = new URI("http://localhost:8080/data")
    def appCtx = new ClassPathXmlApplicationContext("Resolver-test.xml")
    def resolver = new ApplicationContextResolver(baseUri)
    resolver.setApplicationContext(appCtx)

    when:
    SimpleBean bean = resolver.resolve(new URI("simpleBean"), SimpleBean)

    then:
    bean.name == "John Doe"

  }

}
