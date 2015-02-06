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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class BasicAuthenticationFilter extends HttpFilter
{

  /** Field description */
  public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC";

  /** Field description */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  public static final String HEADER_AUTHORIZATION = "Authorization";

  /** marker for failed authentication */
  private static final String ATTRIBUTE_FAILED_AUTH = "sonia.scm.auth.failed";

  /** the logger for BasicAuthenticationFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicAuthenticationFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new basic authenticaton filter.
   *
   * @param configuration scm-manager global configuration
   *
   * @since 1.21
   */
  @Inject
  public BasicAuthenticationFilter(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
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
    User user = null;

    String authentication = request.getHeader(HEADER_AUTHORIZATION);

    if (Util.startWithIgnoreCase(authentication, AUTHORIZATION_BASIC_PREFIX))
    {
      logger.trace("found basic authorization header, start authentication");

      user = authenticate(request, response, subject, authentication);

      if (logger.isTraceEnabled())
      {
        if (user != null)
        {
          logger.trace("user {} successfully authenticated", user.getName());
        }
        else
        {
          logger.trace("authentcation failed, user object is null");
        }
      }
    }
    else if (subject.isAuthenticated())
    {
      logger.trace("user is allready authenticated");
      user = subject.getPrincipals().oneByType(User.class);
    }
    else if ((configuration != null)
      && configuration.isAnonymousAccessEnabled())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("anonymous access granted");
      }

      user = SCMContext.ANONYMOUS;
    }

    if (user == null)
    {
      logger.trace("could not find user send unauthorized");

      handleUnauthorized(request, response, chain);
    }
    else
    {
      chain.doFilter(new SecurityHttpServletRequestWrapper(request, user),
        response);
    }
  }

  /**
   * Sends status code 403 back to client, if the authentication has failed.
   * In all other cases the method will send status code 403 back to client.
   *
   * @param request servlet request
   * @param response servlet response
   * @param chain filter chain
   *
   * @throws IOException
   * @throws ServletException
   *
   * @since 1.8
   */
  protected void handleUnauthorized(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {

    // send only forbidden, if the authentication has failed.
    // see https://bitbucket.org/sdorra/scm-manager/issue/545/git-clone-with-username-in-url-does-not
    if (Boolean.TRUE.equals(request.getAttribute(ATTRIBUTE_FAILED_AUTH)))
    {
      sendFailedAuthenticationError(request, response);
    }
    else
    {
      sendUnauthorizedError(request, response);
    }
  }

  /**
   * Sends an error for a failed authentication back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendFailedAuthenticationError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    HttpUtil.sendUnauthorized(request, response,
      configuration.getRealmDescription());
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
    HttpUtil.sendUnauthorized(request, response,
      configuration.getRealmDescription());
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param securityContext
   * @param subject
   * @param authentication
   *
   * @return
   */
  private User authenticate(HttpServletRequest request,
    HttpServletResponse response, Subject subject, String authentication)
  {
    String token = authentication.substring(6);

    token = new String(Base64.decode(token.getBytes()));

    int index = token.indexOf(CREDENTIAL_SEPARATOR);
    User user = null;

    if ((index > 0) && (index < token.length()))
    {
      String username = token.substring(0, index);
      String password = token.substring(index + 1);

      if (Util.isNotEmpty(username) && Util.isNotEmpty(password))
      {
        logger.trace("try to authenticate user {}", username);

        try
        {

          subject.login(new UsernamePasswordToken(username, password,
            request.getRemoteAddr()));
          user = subject.getPrincipals().oneByType(User.class);
        }
        catch (AuthenticationException ex)
        {

          // add a marker to the request that the authentication has failed
          request.setAttribute(ATTRIBUTE_FAILED_AUTH, Boolean.TRUE);

          if (logger.isTraceEnabled())
          {
            logger.trace("authentication failed for user ".concat(username),
              ex);
          }
          else if (logger.isWarnEnabled())
          {
            logger.warn("authentication failed for user {}", username);
          }
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("username or password is null/empty");
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("failed to read basic auth credentials");
    }

    return user;
  }

  //~--- fields ---------------------------------------------------------------

  /** scm main configuration */
  protected ScmConfiguration configuration;
}
