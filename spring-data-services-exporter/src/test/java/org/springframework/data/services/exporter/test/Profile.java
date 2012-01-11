package org.springframework.data.services.exporter.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Entity
public class Profile {

  @Id @GeneratedValue(strategy = GenerationType.AUTO) Long id;
  private String name;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
