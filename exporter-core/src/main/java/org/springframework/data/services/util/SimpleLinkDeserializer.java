package org.springframework.data.services.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.springframework.data.services.SimpleLink;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class SimpleLinkDeserializer extends StdDeserializer {

  @SuppressWarnings({"unchecked"})
  public SimpleLinkDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public SimpleLink deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
      throw ctxt.mappingException(SimpleLink.class);
    }

    String rel = null;
    String href = null;
    while (jp.nextToken() != JsonToken.END_OBJECT) {
      String name = jp.getCurrentName();
      if ("rel".equals(name)) {
        rel = jp.nextTextValue();
      } else if ("href".equals(name)) {
        href = jp.nextTextValue();
      }
    }

    try {
      return new SimpleLink(rel, new URI(href));
    } catch (URISyntaxException e) {
      throw ctxt.mappingException(SimpleLink.class);
    }
  }

}
