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
  private AddressRepository addressRepository;

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

  public AddressRepository getAddressRepository() {
    return addressRepository;
  }

  public void setAddressRepository(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override public void afterPropertiesSet() throws Exception {
    Address pers1addr = addressRepository.save(new Address(new String[]{"1234 W. 1st St."}, "Univille", "ST", "12345"));
    List<Profile> pers1profiles = new ArrayList<Profile>();
    pers1profiles.add(profileRepository.save(new Profile("twitter", "#!/johndoe")));
    pers1profiles.add(profileRepository.save(new Profile("facebook", "/johndoe")));
    personRepository.save(new Person("John Doe", pers1addr, pers1profiles));

    Address pers2addr = addressRepository.save(new Address(new String[]{"1234 E. 2nd St."}, "Univille", "ST", "12345"));
    List<Profile> pers2profiles = new ArrayList<Profile>();
    pers2profiles.add(profileRepository.save(new Profile("facebook", "/janedoe")));
    personRepository.save(new Person("Jane Doe", pers2addr, pers2profiles));
  }

}
