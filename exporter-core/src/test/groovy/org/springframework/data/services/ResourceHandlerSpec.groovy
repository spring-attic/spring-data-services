package org.springframework.data.services

import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
class ResourceHandlerSpec extends Specification {

  def "resolves properties based on the last segment of the URI"() {

    given:
    def baseUri = new URI("http://localhost:8080/base")
    def handler = new DelegatingResourceHandler([
                                                    new ListResourceHandler(baseUri),
                                                    new MapResourceHandler(baseUri),
                                                    new BeanPropertyResourceHandler(baseUri)
                                                ])
    def list = ["first", "second", "third"]
    def map = ["first": "first", "second": "second", "third": "third"]
    def bean = new SimpleBean("third")

    when:
    def first = handler.handle(new SimpleResource(new URI("simpleBean/0")), list)
    def second = handler.handle(new SimpleResource(new URI("simpleBean/second")), map)
    def name = handler.handle(new SimpleResource(new URI("simpleBean/name")), bean)

    then:
    first == "first"
    second == "second"
    name == "third"

  }

}

class SimpleBean {
  String name

  SimpleBean(String name) {
    this.name = name
  }
}