package org.springframework.data.services.web.exporter.test;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
