package org.springframework.data.services.repository;

import org.springframework.data.repository.CrudRepository;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface SimpleEntityRepository extends CrudRepository<SimpleEntity, String> {
}
