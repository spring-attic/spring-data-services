package org.springframework.data.services.util;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.springframework.data.services.SimpleLink;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@SuppressWarnings({"unchecked"})
public class SimpleLinkSerializer extends SerializerBase<SimpleLink> {

  public SimpleLinkSerializer(Class t) {
    super(t);
  }

  @Override
  public void serialize(SimpleLink link, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
    jgen.writeStartObject();
    jgen.writeStringField("rel", link.rel());
    jgen.writeStringField("href", link.href().toASCIIString());
    jgen.writeEndObject();
  }

}
