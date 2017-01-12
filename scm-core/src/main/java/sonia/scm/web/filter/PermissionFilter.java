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



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Splitter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.Role;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.AuthorizationException;

/**
 * Abstract http filter to check repository permissions.
 *
 * @author Sebastian Sdorra
 */
public abstract class PermissionFilter extends HttpFilter
{

  /** the logger for PermissionFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new permission filter
   *
   * @param configuration global scm-manager configuration
   *
   * @since 1.21
   */
  public PermissionFilter(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the requested repository.
   *
   *
   * @param request current http request
   *
   * @return requested repository
   */
  protected abstract Repository getRepository(HttpServletRequest request);

  /**
   * Returns true if the current request is a write request.
   *
   *
   * @param request
   *
   * @return returns true if the current request is a write request
   */
  protected abstract boolean isWriteRequest(HttpServletRequest request);

  //~--- methods --------------------------------------------------------------

  /**
   * Checks the permission for the requested repository. If the user has enough
   * permission, then the filter chain is called.
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
    Subject subject = SecurityUtils.getSubject();

    try
    {
      Repository repository = getRepository(request);

      if (repository != null)
      {
        boolean writeRequest = isWriteRequest(request);

        if (hasPermission(repository, writeRequest))
        {
          logger.trace("{} access to repository {} for user {} granted",
            getActionAsString(writeRequest), repository.getName(),
            getUserName(subject));

          chain.doFilter(request, response);
        }
        else
        {
          logger.info("{} access to repository {} for user {} denied",
            getActionAsString(writeRequest), repository.getName(),
            getUserName(subject));
          
          sendAccessDenied(request, response, subject);
        }
      }
      else
      {
        logger.debug("repository not found");

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    }
    catch (ArgumentIsInvalidException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace(
          "wrong request at ".concat(request.getRequestURI()).concat(
            " send redirect"), ex);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("wrong request at {} send redirect",
          request.getRequestURI());
      }

      response.sendRedirect(getRepositoryRootHelpUrl(request));
    }
    catch (ScmSecurityException | AuthorizationException ex)
    {
      logger.warn("user " + subject.getPrincipal() +  " has not enough permissions", ex);
      sendAccessDenied(request, response, subject);
    }

  }

  /**
   * Sends a "not enough privileges" error back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * Sends an unauthorized error back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendUnauthorizedError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    HttpUtil.sendUnauthorized(response, configuration.getRealmDescription());
  }

  /**
   * Extracts the type of the repositroy from url.
   *
   *
   * @param request http request
   *
   * @return type of repository
   */
  private String extractType(HttpServletRequest request)
  {
    Iterator<String> it = Splitter.on(
                            HttpUtil.SEPARATOR_PATH).omitEmptyStrings().split(
                            request.getRequestURI()).iterator();
    String type = it.next();

    if (Util.isNotEmpty(request.getContextPath()))
    {
      type = it.next();
    }

    return type;
  }

  /**
   * Send access denied to the servlet response.
   *
   * @param request current http request object
   * @param response current http response object
   * @param subject user subject
   *
   * @throws IOException
   */
  private void sendAccessDenied(HttpServletRequest request,
    HttpServletResponse response, Subject subject)
    throws IOException
  {
    if (subject.hasRole(Role.USER))
    {
      sendNotEnoughPrivilegesError(request, response);
    }
    else
    {
      sendUnauthorizedError(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns action as string.
   *
   *
   * @param writeRequest true if the action is a write action
   *
   * @return action as string
   */
  private String getActionAsString(boolean writeRequest)
  {
    return writeRequest
      ? "write"
      : "read";
  }

  /**
   * Returns the repository root help url.
   *
   *
   * @param request current http request
   *
   * @return repository root help url
   */
  private String getRepositoryRootHelpUrl(HttpServletRequest request)
  {
    String type = extractType(request);
    String helpUrl = HttpUtil.getCompleteUrl(request,
                       "/api/rest/help/repository-root/");

    helpUrl = helpUrl.concat(type).concat(".html");

    return helpUrl;
  }

  /**
   * Returns the username from the given subject or anonymous.
   *
   *
   * @param subject user subject
   *
   * @return username username from subject or anonymous
   */
  private Object getUserName(Subject subject)
  {
    Object principal = subject.getPrincipal();

    if (principal == null)
    {
      principal = SCMContext.USER_ANONYMOUS;
    }

    return principal;
  }

  /**
   * Returns true if the current user has the required permissions.
   *
   *
   * @param repository repository for the permissions check
   * @param writeRequest true if request is a write request
   *
   * @return true if the current user has the required permissions
   */
  private boolean hasPermission(Repository repository, boolean writeRequest)
  {
    boolean permitted;

    if (writeRequest)
    {
      permitted = RepositoryPermissions.write(repository).isPermitted();
    }
    else
    {
      permitted = RepositoryPermissions.read(repository).isPermitted();
    }

    return permitted;
  }

  //~--- fields ---------------------------------------------------------------

  /** scm-manager global configuration */
  private final ScmConfiguration configuration;
}
