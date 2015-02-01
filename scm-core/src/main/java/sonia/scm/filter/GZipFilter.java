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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Priority;
import sonia.scm.util.WebUtil;
import sonia.scm.web.filter.HttpFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for gzip encoding.
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
@Priority(Filters.PRIORITY_PRE_BASEURL)
@WebElement(value = Filters.PATTERN_RESOURCE_REGEX, regex = true)
public class GZipFilter extends HttpFilter
{

  /**
   * the logger for GZipFilter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GZipFilter.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the configuration for the gzip filter.
   *
   *
   * @return gzip filter configuration
   */
  public GZipFilterConfig getConfig()
  {
    return config;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Encodes the response, if the request has support for gzip encoding.
   *
   *
   * @param request http request
   * @param response http response
   * @param chain filter chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    if (WebUtil.isGzipSupported(request))
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("compress output with gzip");
      }

      GZipResponseWrapper wrappedResponse = new GZipResponseWrapper(response,
                                              config);

      chain.doFilter(request, wrappedResponse);
      wrappedResponse.finishResponse();
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** gzip filter configuration */
  private GZipFilterConfig config = new GZipFilterConfig();
}
