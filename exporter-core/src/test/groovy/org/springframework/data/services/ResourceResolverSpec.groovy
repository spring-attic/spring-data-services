package org.springframework.data.services

import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
class ResourceResolverSpec extends Specification {

  def "resolves properties based on the last segment of the URI"() {

    given:
    def baseUri = new URI("http://localhost:8080/base")
    def resolver = new DelegatingResourceResolver([
                                                      new ListResourceResolver(baseUri),
                                                      new MapResourceResolver(baseUri),
                                                      new BeanPropertyResourceResolver(baseUri)
                                                  ])
    def list = ["first", "second", "third"]
    def map = ["first": "first", "second": "second", "third": "third"]
    def bean = new SimpleBean("third")

    when:
    def first = resolver.resolve(new URI("simpleBean/0"), [new SimpleResource(new URI("simpleBean"), list)])
    def second = resolver.resolve(new URI("simpleBean/second"), [new SimpleResource(new URI("simpleBean"), map)])
    def name = resolver.resolve(new URI("simpleBean/name"), [new SimpleResource(new URI("simpleBean"), bean)])

    then:
    first?.target() == "first"
    second?.target() == "second"
    name?.target() == "third"

  }

}

class SimpleBean {
  String name

  SimpleBean(String name) {
    this.name = name
  }
}