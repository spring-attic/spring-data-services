package org.springframework.data.services.exporter.rest;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JsonView extends AbstractView {

  private ObjectMapper mapper = new ObjectMapper();

  public JsonView() {
    setContentType(MediaType.APPLICATION_JSON.toString());
  }

  @Override
  protected void renderMergedOutputModel(Map<String, Object> model,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {
    HttpStatus status = (HttpStatus) model.get("status");
    if (null != status) {
      response.setStatus(status.value());
    }

    HttpHeaders headers = (HttpHeaders) model.get("headers");
    if (null != headers) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getValue().size() > 1) {
          for (String val : entry.getValue()) {
            response.addHeader(entry.getKey(), val);
          }
        } else {
          response.setHeader(entry.getKey(), entry.getValue().get(0));
        }
      }
    }

    Object body = model.get("body");
    Object links = model.get("links");
    if (null != body) {
      try {
        mapper.writeValue(response.getOutputStream(), body);
      } catch (LazyInitializationException e) {
        // Need to support links for these
      }
    } else if (null != links) {
      mapper.writeValue(response.getOutputStream(), links);
    }
  }

}
