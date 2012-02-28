package org.springframework.data.services;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public interface Resolver<KEY, STACK, VAL> {

  boolean supports(KEY key, STACK stack);

  VAL resolve(KEY key, STACK stack);

}
