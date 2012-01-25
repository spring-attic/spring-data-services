package org.springframework.data.services.web.exporter.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Entity
public class Profile {

  @Id
  @GeneratedValue
  private Long id;
  private String type;
  private String url;

  public Profile(String type, String url) {
    this.type = type;
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
