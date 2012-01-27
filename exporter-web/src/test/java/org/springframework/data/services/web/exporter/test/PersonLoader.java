package org.springframework.data.services.web.exporter.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class PersonLoader implements ApplicationContextAware, InitializingBean {

  private ApplicationContext applicationContext;
  private PersonRepository personRepository;
  private ProfileRepository profileRepository;

  @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public PersonRepository getPersonRepository() {
    return personRepository;
  }

  public void setPersonRepository(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  public ProfileRepository getProfileRepository() {
    return profileRepository;
  }

  public void setProfileRepository(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  @Override public void afterPropertiesSet() throws Exception {
    Person pers1 = new Person("John Doe");
    List<Profile> pers1profiles = new ArrayList<Profile>();
    pers1profiles.add(profileRepository.save(new Profile("twitter", "#!/johndoe")));
    pers1profiles.add(profileRepository.save(new Profile("facebook", "/johndoe")));
    pers1.setProfiles(pers1profiles);
    personRepository.save(pers1);

    Person pers2 = new Person("Jane Doe");
    List<Profile> pers2profiles = new ArrayList<Profile>();
    pers2profiles.add(profileRepository.save(new Profile("facebook", "/janedoe")));
    pers2.setProfiles(pers2profiles);
    personRepository.save(pers2);
  }

}
