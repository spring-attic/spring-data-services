package org.springframework.data.services;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Handler<T> {
  void handle(T obj);
}
