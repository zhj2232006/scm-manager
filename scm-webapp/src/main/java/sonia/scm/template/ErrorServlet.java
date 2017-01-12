/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ErrorServlet extends HttpServlet
{

  /** Field description */
  private static final String TEMPALTE = "/error.mustache";

  /** Field description */
  private static final long serialVersionUID = -3289076078469757874L;

  /**
   * the logger for ErrorServlet
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ErrorServlet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param templateEngineFactory
   */
  @Inject
  public ErrorServlet(SCMContextProvider context,
    TemplateEngineFactory templateEngineFactory)
  {
    this.context = context;
    this.templateEngineFactory = templateEngineFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doPost(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void processRequest(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
  {
    PrintWriter writer = null;

    try
    {
      writer = response.getWriter();

      Map<String, Object> env = new HashMap<String, Object>();
      String error = Util.EMPTY_STRING;

      if (context.getStartupError() != null)
      {
        error = Throwables.getStackTraceAsString(context.getStartupError());
      }

      env.put("error", error);

      TemplateEngine engine = templateEngineFactory.getDefaultEngine();
      Template template = engine.getTemplate(TEMPALTE);

      if (template != null)
      {
        template.execute(writer, env);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find template {}", TEMPALTE);
      }
    }
    finally
    {
      IOUtil.close(writer);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SCMContextProvider context;

  /** Field description */
  private final TemplateEngineFactory templateEngineFactory;
}
