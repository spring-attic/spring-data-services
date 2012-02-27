package org.springframework.data.services.web.exporter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
public interface ResourceExporterController extends InitializingBean {

  @Transactional(readOnly = true)
  @RequestMapping(method = RequestMethod.GET)
  public void get(ServerHttpRequest request, Model model);

  @Transactional
  @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
  public void createOrUpdate(ServerHttpRequest request, Model model);

  @Transactional
  @RequestMapping(method = RequestMethod.DELETE)
  public void delete(ServerHttpRequest request, Model model);

}
