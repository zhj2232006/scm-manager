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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import sonia.scm.ClientMessages;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.ScmSvnErrorCode;
import sonia.scm.repository.SvnUtil;
import sonia.scm.web.filter.ProviderPermissionFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Priority(Filters.PRIORITY_AUTHORIZATION)
@WebElement(value = SvnServletModule.PATTERN_SVN)
public class SvnPermissionFilter extends ProviderPermissionFilter
{

  /** Field description */
  private static final Set<String> WRITEMETHOD_SET =
    ImmutableSet.of("MKACTIVITY", "PROPPATCH", "PUT", "CHECKOUT", "MKCOL",
      "MOVE", "COPY", "DELETE", "LOCK", "UNLOCK", "MERGE");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param configuration
   * @param repository
   */
  @Inject
  public SvnPermissionFilter(ScmConfiguration configuration,
    RepositoryProvider repository)
  {
    super(configuration, repository);
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
   */
  @Override
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    if (SvnUtil.isSvnClient(request))
    {
      //J-
      SvnUtil.sendError(
        request, 
        response, 
        HttpServletResponse.SC_FORBIDDEN,
        ScmSvnErrorCode.authzNotEnoughPrivileges(
          ClientMessages.get(request).notEnoughPrivileges()
        )
      );
      //J+
    }
    else
    {
      super.sendNotEnoughPrivilegesError(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override
  protected boolean isWriteRequest(HttpServletRequest request)
  {
    return WRITEMETHOD_SET.contains(request.getMethod().toUpperCase());
  }
}
