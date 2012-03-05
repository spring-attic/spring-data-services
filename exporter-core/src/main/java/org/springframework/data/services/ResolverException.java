package org.springframework.data.services;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class ResolverException extends Exception {

  private Object target;

  public ResolverException(String s) {
    super(s);
  }

  public ResolverException(String s, Object target) {
    super(s);
    this.target = target;
  }

  public ResolverException(String s, Throwable throwable, Object target) {
    super(s, throwable);
    this.target = target;
  }

  public Object target() {
    return target;
  }

}
