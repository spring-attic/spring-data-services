package org.springframework.data.services.exporter.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.services.exporter.test.Person;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
