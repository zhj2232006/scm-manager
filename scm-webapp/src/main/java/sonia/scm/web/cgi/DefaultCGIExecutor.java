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



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultCGIExecutor extends AbstractCGIExecutor
{

  /** Field description */
  public static final String CGI_VERSION = "CGI/1.1";

  /** Field description */
  public static final int DEFAULT_BUFFER_SIZE = 16264;

  /** Field description */
  public static final String SYSTEM_ROOT_WINDOWS = "C:\\WINDOWS";

  /** Field description */
  private static final String SERVER_SOFTWARE_PREFIX = "scm-manager/";

  /** the logger for DefaultCGIExecutor */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCGIExecutor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param executor to handle error stream processing
   * @param configuration
   * @param context
   * @param request
   * @param response
   */
  public DefaultCGIExecutor(ExecutorService executor,
    ScmConfiguration configuration, ServletContext context,
    HttpServletRequest request, HttpServletResponse response)
  {
    this.executor = executor;
    this.configuration = configuration;
    this.context = context;
    this.request = request;
    this.response = response;

    // set default values
    this.bufferSize = DEFAULT_BUFFER_SIZE;
    this.environment = createEnvironment();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   *
   * @param cmd
   *
   * @throws IOException
   */
  @Override
  public void execute(String cmd) throws IOException
  {
    File command = new File(cmd);
    EnvList env = new EnvList(environment);

    if (passShellEnvironment)
    {
      apendOsEnvironment(env);
    }

    // workaround for mercurial 2.1
    if (isContentLengthWorkaround())
    {
      env.set(ENV_CONTENT_LENGTH, Integer.toString(request.getContentLength()));
    }

    if (workDirectory == null)
    {
      workDirectory = command.getParentFile();
    }

    String path = command.getAbsolutePath();
    String pathTranslated = request.getPathTranslated();

    if (Strings.isNullOrEmpty(pathTranslated))
    {
      pathTranslated = path;
    }
    else
    {
      pathTranslated = HttpUtil.removeMatrixParameter(pathTranslated);
    }

    env.set(ENV_PATH_TRANSLATED, pathTranslated);

    String execCmd = path;

    if ((execCmd.charAt(0) != '"') && (execCmd.indexOf(' ') >= 0))
    {
      execCmd = "\"".concat(execCmd).concat("\"");
    }

    if (interpreter != null)
    {
      execCmd = interpreter.concat(" ").concat(execCmd);
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("execute cgi: {}", execCmd);

      if (logger.isTraceEnabled())
      {
        logger.trace(env.toString());
      }
    }

    Process p = null;

    try
    {
      p = Runtime.getRuntime().exec(execCmd, env.getEnvArray(), workDirectory);
      execute(p);
    }
    catch (IOException ex)
    {
      getExceptionHandler().handleException(request, response, ex);
    }
    finally
    {
      if (p != null)
      {
        p.destroy();
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CGIExceptionHandler getExceptionHandler()
  {
    if (exceptionHandler == null)
    {
      exceptionHandler = new DefaultCGIExceptionHandler();
    }

    return exceptionHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CGIStatusCodeHandler getStatusCodeHandler()
  {
    if (statusCodeHandler == null)
    {
      statusCodeHandler = new DefaultCGIStatusCodeHandler();
    }

    return statusCodeHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isContentLengthWorkaround()
  {
    return contentLengthWorkaround;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param contentLengthWorkaround
   */
  @Override
  public void setContentLengthWorkaround(boolean contentLengthWorkaround)
  {
    this.contentLengthWorkaround = contentLengthWorkaround;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param env
   */
  private void apendOsEnvironment(EnvList env)
  {
    Map<String, String> osEnv = System.getenv();

    if (Util.isNotEmpty(osEnv))
    {
      for (Map.Entry<String, String> e : osEnv.entrySet())
      {
        env.set(e.getKey(), e.getValue());
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private EnvList createEnvironment()
  {

    // remove ;jsessionid
    String pathInfo = HttpUtil.removeMatrixParameter(request.getPathInfo());
    String uri = HttpUtil.removeMatrixParameter(request.getRequestURI());
    String scriptName = uri.substring(0, uri.length() - pathInfo.length());
    String scriptPath = context.getRealPath(scriptName);
    int len = request.getContentLength();
    EnvList env = new EnvList();

    env.set(ENV_AUTH_TYPE, request.getAuthType());

    /**
     * Note CGI spec says CONTENT_LENGTH must be NULL ("") or undefined
     * if there is no content, so we cannot put 0 or -1 in as per the
     * Servlet API spec.
     *
     * see org.apache.catalina.servlets.CGIServlet
     */
    if (len <= 0)
    {
      env.set(ENV_CONTENT_LENGTH, "");
    }
    else
    {
      env.set(ENV_CONTENT_LENGTH, Integer.toString(len));
    }

    /**
     * Decode PATH_INFO
     * https://bitbucket.org/sdorra/scm-manager/issue/79/hgweb-decoding-issue
     */
    if (Util.isNotEmpty(pathInfo))
    {
      pathInfo = HttpUtil.decode(pathInfo);
    }

    env.set(ENV_CONTENT_TYPE, Util.nonNull(request.getContentType()));
    env.set(ENV_GATEWAY_INTERFACE, CGI_VERSION);
    env.set(ENV_PATH_INFO, pathInfo);
    env.set(ENV_QUERY_STRING, request.getQueryString());
    env.set(ENV_REMOTE_ADDR, request.getRemoteAddr());
    env.set(ENV_REMOTE_HOST, request.getRemoteHost());

    // The identity information reported about the connection by a
    // RFC 1413 [11] request to the remote agent, if
    // available. Servers MAY choose not to support this feature, or
    // not to request the data for efficiency reasons.
    // "REMOTE_IDENT" => "NYI"
    env.set(ENV_REMOTE_USER, request.getRemoteUser());
    env.set(ENV_REQUEST_METHOD, request.getMethod());
    env.set(ENV_SCRIPT_NAME, scriptName);
    env.set(ENV_SCRIPT_FILENAME, scriptPath);
    env.set(ENV_SERVER_NAME, Util.nonNull(request.getServerName()));

    int serverPort = HttpUtil.getServerPort(configuration, request);

    env.set(ENV_SERVER_PORT, Integer.toString(serverPort));
    env.set(ENV_SERVER_PROTOCOL, Util.nonNull(request.getProtocol()));
    env.set(ENV_SERVER_SOFTWARE,
      SERVER_SOFTWARE_PREFIX.concat(SCMContext.getContext().getVersion()));

    Enumeration enm = request.getHeaderNames();

    while (enm.hasMoreElements())
    {
      String name = (String) enm.nextElement();
      String value = request.getHeader(name);

      env.set(ENV_HTTP_HEADER_PREFIX + name.toUpperCase().replace('-', '_'),
        value);
    }

    // these extra ones were from printenv on www.dev.nomura.co.uk
    env.set(ENV_HTTPS, (request.isSecure()
      ? ENV_HTTPS_VALUE_ON
      : ENV_HTTPS_VALUE_OFF));

    if (SystemUtil.isWindows())
    {
      env.set(ENV_SYSTEM_ROOT, SYSTEM_ROOT_WINDOWS);
    }

    return env;
  }

  /**
   * Method description
   *
   *
   * @param process
   *
   * @throws IOException
   */
  private void execute(Process process) throws IOException
  {
    InputStream processIS = null;
    ServletOutputStream servletOS = null;

    try
    {
      processErrorStreamAsync(process);
      processServletInput(process);
      processIS = process.getInputStream();
      parseHeaders(processIS);
      servletOS = response.getOutputStream();

      long content = ByteStreams.copy(processIS, servletOS);

      waitForFinish(process, servletOS, content);
    }
    finally
    {
      IOUtil.close(processIS);
      IOUtil.close(servletOS);
    }
  }

  /**
   * Method description
   *
   *
   * @param is
   *
   *
   * @throws IOException
   */
  private void parseHeaders(InputStream is) throws IOException
  {
    String line = null;

    while ((line = getTextLineFromStream(is)).length() > 0)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("  ".concat(line));
      }

      if (!line.startsWith(RESPONSE_HEADER_HTTP_PREFIX))
      {
        int k = line.indexOf(':');

        if (k > 0)
        {
          String key = line.substring(0, k).trim();
          String value = line.substring(k + 1).trim();

          if (RESPONSE_HEADER_LOCATION.equalsIgnoreCase(key))
          {
            response.sendRedirect(response.encodeRedirectURL(value));
          }
          else if (RESPONSE_HEADER_STATUS.equalsIgnoreCase(key))
          {
            String[] token = value.split(" ");
            int status = Integer.parseInt(token[0]);

            if (logger.isDebugEnabled())
            {
              logger.debug("CGI returned with status {}", status);
            }

            if (status < 304)
            {
              response.setStatus(status);
            }
            else
            {
              response.sendError(status);
            }
          }
          else
          {

            // add remaining header items to our response header
            response.addHeader(key, value);
          }
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param in
   *
   * @throws IOException
   */
  private void processErrorStream(InputStream in) throws IOException
  {
    if (logger.isWarnEnabled())
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      IOUtil.copy(in, baos);

      if (baos.size() > 0)
      {
        logger.warn(baos.toString());
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param process
   */
  private void processErrorStreamAsync(final Process process)
  {
    executor.execute(() -> {
      InputStream errorStream = null;

      try
      {
        errorStream = process.getErrorStream();
        processErrorStream(errorStream);
      }
      catch (IOException ex)
      {
        logger.error("could not read errorstream", ex);
      }
      finally
      {
        IOUtil.close(errorStream);
      }
    });
  }

  /**
   * Method description
   *
   *
   * @param process
   */
  private void processServletInput(Process process)
  {
    logger.trace("process servlet input");

    OutputStream processOS = null;
    ServletInputStream servletIS = null;

    try
    {
      processOS = process.getOutputStream();
      servletIS = request.getInputStream();
      IOUtil.copy(servletIS, processOS, bufferSize);
    }
    catch (IOException ex)
    {
      logger.error(
        "could not read from ServletInputStream and write to ProcessOutputStream",
        ex);
    }
    finally
    {
      IOUtil.close(processOS);
      IOUtil.close(servletIS);
    }
  }

  /**
   * Method description
   *
   *
   * @param process
   * @param output
   * @param content
   *
   *
   * @throws IOException
   */
  private void waitForFinish(Process process, ServletOutputStream output,
    long content)
    throws IOException
  {
    try
    {
      int exitCode = process.waitFor();

      if (!ignoreExitCode)
      {
        if (logger.isTraceEnabled())
        {
          logger.trace(
            "handle status code {} with statusCodeHandler, there are {} bytes written to outputstream",
            exitCode, content);
        }

        if (content == 0)
        {
          getStatusCodeHandler().handleStatusCode(request, response, output,
            exitCode);
        }
        else
        {
          getStatusCodeHandler().handleStatusCode(request, exitCode);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("ignore status code {}", exitCode);
      }
    }
    catch (InterruptedException ex)
    {
      getExceptionHandler().handleException(request, response, ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param is
   *
   * @return
   *
   * @throws IOException
   */
  private String getTextLineFromStream(InputStream is) throws IOException
  {
    StringBuilder buffer = new StringBuilder();
    int b;

    while ((b = is.read()) != -1 && (b != (int) '\n'))
    {
      buffer.append((char) b);
    }

    return buffer.toString().trim();
  }

  //~--- fields ---------------------------------------------------------------

  /** executor to handle error stream processing */
  private final ExecutorService executor;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private boolean contentLengthWorkaround = false;

  /** Field description */
  private ServletContext context;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private HttpServletResponse response;
}
