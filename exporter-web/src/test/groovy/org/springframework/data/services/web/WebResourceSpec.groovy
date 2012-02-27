package org.springframework.data.services.web

import spock.lang.Specification

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
class WebResourceSpec extends Specification {

  def "GETs list of resources available at top level"() {

    given:
    def rest = new RestBuilder()
    rest.errorHandler = {resp ->
      println "error: $resp.body"
    }

    when:
    def resp = rest {
      accept "application/json"
      responseType Map
      get "http://localhost:8080/data/"
    }

    then:
    null != resp.body

    when:
    def personHref
    resp.body._links.each {
      if (it.rel == "person") {
        personHref = it.href
      }
    }

    then:
    personHref == "http://localhost:8080/data/person"

  }

}
