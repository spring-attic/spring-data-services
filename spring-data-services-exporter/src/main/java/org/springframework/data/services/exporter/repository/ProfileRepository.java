package org.springframework.data.services.exporter.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.services.exporter.test.Profile;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface ProfileRepository extends CrudRepository<Profile, Long> {
}
