package org.springframework.data.services.web.exporter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
@Controller
@RequestMapping("/**")
public interface ResourceExporterController extends ApplicationContextAware, InitializingBean {

  @Transactional(readOnly = true)
  @RequestMapping(method = RequestMethod.GET)
  public void get(HttpServletRequest request, Model model);

  @Transactional
  @RequestMapping(method = RequestMethod.POST)
  public void post(HttpServletRequest request, HttpEntity<byte[]> entity, Model model);

}
