package org.springframework.data.services.web;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.services.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class UriListView extends AbstractView {

  public UriListView() {
    setContentType("text/uri-list");
  }

  @SuppressWarnings({"unchecked"})
  @Override
  protected void renderMergedOutputModel(Map<String, Object> model,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {

    response.setContentType(getContentType());

    HttpStatus status = (HttpStatus) model.get("status");
    HttpHeaders headers = (HttpHeaders) model.get("headers");
    List<Link> links = (List<Link>) model.get("resource");

    if (null != status) {
      response.setStatus(status.value());
    }

    if (null != headers) {
      for (Map.Entry<String, String> entry : headers.toSingleValueMap().entrySet()) {
        response.setHeader(entry.getKey(), entry.getValue());
      }
    }

    PrintWriter out = response.getWriter();
    if (null != links) {
      for (Link l : links) {
        out.println(l.href().toString());
      }
    }
    out.flush();

  }

}
