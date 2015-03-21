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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import org.apache.shiro.codec.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.CipherUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgEnvironment
{

  /** Field description */
  public static final String ENV_PYTHON_PATH = "PYTHONPATH";

  /** Field description */
  private static final String ENV_CHALLENGE = "SCM_CHALLENGE";

  /** Field description */
  private static final String ENV_URL = "SCM_URL";

  /** Field description */
  private static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  /**
   *   the logger for HgEnvironment
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgEnvironment.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HgEnvironment() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param environment
   * @param handler
   * @param hookManager
   * @param request
   */
  public static void prepareEnvironment(Map<String, String> environment,
    HgRepositoryHandler handler, HgHookManager hookManager,
    HttpServletRequest request)
  {
    String hookUrl;

    if (request != null)
    {
      hookUrl = hookManager.createUrl(request);
      environment.put(SCM_CREDENTIALS, getCredentials(request));
    }
    else
    {
      hookUrl = hookManager.createUrl();
    }

    environment.put(ENV_PYTHON_PATH, HgUtil.getPythonPath(handler.getConfig()));
    environment.put(ENV_URL, hookUrl);
    environment.put(ENV_CHALLENGE, hookManager.getChallenge());
  }

  /**
   * Method description
   *
   *
   * @param environment
   * @param handler
   * @param hookManager
   */
  public static void prepareEnvironment(Map<String, String> environment,
    HgRepositoryHandler handler, HgHookManager hookManager)
  {
    prepareEnvironment(environment, handler, hookManager, null);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private static String getCredentials(HttpServletRequest request)
  {
    String credentials = null;
    String header = request.getHeader(HttpUtil.HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(header))
    {
      String[] parts = header.split("\\s+");

      if (parts.length > 0)
      {
        CipherUtil cu = CipherUtil.getInstance();

        credentials = cu.encode(Base64.decodeToString(parts[1]));
      }
      else
      {
        logger.warn("invalid basic authentication header");
      }
    }
    else
    {
      logger.warn("could not find authentication header on request");
    }

    return Strings.nullToEmpty(credentials);
  }
}
