package org.springframework.data.services.web.exporter.test;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Entity
public class Person {

  @Id
  @GeneratedValue
  private Long id;
  private String name;
  @OneToMany
  private List<Profile> profiles;

  public Person(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Profile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<Profile> profiles) {
    this.profiles = profiles;
  }

}
