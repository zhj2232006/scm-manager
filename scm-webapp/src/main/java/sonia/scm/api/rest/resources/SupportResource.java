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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.SCMContextProvider;
import sonia.scm.ServletContainerDetector;
import sonia.scm.Type;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.Role;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.SystemUtil;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.view.Viewable;

import java.io.IOException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import sonia.scm.store.ConfigurationStoreFactory;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("support")
public class SupportResource
{

  /** Field description */
  public static final String TEMPLATE = "/templates/support.mustache";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param securityContext
   * @param context
   * @param templateHandler
   * @param configuration
   * @param pluginManager
   * @param storeFactory
   * @param repositoryManager
   * @param request
   */
  @Inject
  public SupportResource(SCMContextProvider context,
    ScmConfiguration configuration, PluginManager pluginManager,
    ConfigurationStoreFactory storeFactory, RepositoryManager repositoryManager,
    HttpServletRequest request)
  {
    this.context = context;
    this.configuration = configuration;
    this.pluginManager = pluginManager;
    this.storeFactoryClass = storeFactory.getClass();
    this.repositoryManager = repositoryManager;
    this.request = request;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Viewable getSupport() throws IOException
  {
    Subject subject = SecurityUtils.getSubject();

    if (!subject.hasRole(Role.ADMIN))
    {
      throw new ScmSecurityException("admin privileges required");
    }

    Map<String, Object> env = Maps.newHashMap();

    env.put("version", new VersionInformation(context, storeFactoryClass));
    env.put("configuration", configuration);
    env.put("pluginManager", pluginManager);
    env.put("runtime", new RuntimeInformation());
    env.put("system", new SystemInformation(request));
    env.put("repositoryHandlers", getRepositoryHandlers());

    return new Viewable(TEMPLATE, env);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private List<RepositoryHandler> getRepositoryHandlers()
  {
    List<RepositoryHandler> handlers = Lists.newArrayList();

    for (Type type : repositoryManager.getConfiguredTypes())
    {
      handlers.add(repositoryManager.getHandler(type.getName()));
    }

    return handlers;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/04/30
   * @author         Enter your name here...
   */
  public static class RuntimeInformation
  {

    /**
     * Constructs ...
     *
     */
    public RuntimeInformation()
    {
      Runtime runtime = Runtime.getRuntime();

      totalMemory = runtime.totalMemory();
      freeMemory = runtime.freeMemory();
      maxMemory = runtime.maxMemory();
      availableProcessors = runtime.availableProcessors();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public int getAvailableProcessors()
    {
      return availableProcessors;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public long getFreeMemory()
    {
      return freeMemory;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public long getMaxMemory()
    {
      return maxMemory;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public long getTotalMemory()
    {
      return totalMemory;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int availableProcessors;

    /** Field description */
    private long freeMemory;

    /** Field description */
    private long maxMemory;

    /** Field description */
    private long totalMemory;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/04/30
   * @author         Enter your name here...
   */
  public static class SystemInformation
  {

    /**
     * Constructs ...
     *
     *
     * @param request
     */
    public SystemInformation(HttpServletRequest request)
    {
      os = SystemUtil.getOS();
      arch = SystemUtil.getArch();
      container = ServletContainerDetector.detect(request).name();
      java = System.getProperty("java.vendor").concat("/").concat(
        System.getProperty("java.version"));
      locale = Locale.getDefault().toString();
      timeZone = TimeZone.getDefault().getID();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getArch()
    {
      return arch;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getContainer()
    {
      return container;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getJava()
    {
      return java;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getLocale()
    {
      return locale;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getOs()
    {
      return os;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getTimeZone()
    {
      return timeZone;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String arch;

    /** Field description */
    private String container;

    /** Field description */
    private String java;

    /** Field description */
    private String locale;

    /** Field description */
    private String os;

    /** Field description */
    private String timeZone;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/04/30
   * @author         Enter your name here...
   */
  public static class VersionInformation
  {

    /**
     * Constructs ...
     *
     *
     * @param context
     * @param storeFactoryClass
     */
    public VersionInformation(SCMContextProvider context,
      Class<?> storeFactoryClass)
    {
      version = context.getVersion();
      stage = context.getStage().name();
      storeFactory = storeFactoryClass.getName();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getStage()
    {
      return stage;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getStoreFactory()
    {
      return storeFactory;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getVersion()
    {
      return version;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String stage;

    /** Field description */
    private String storeFactory;

    /** Field description */
    private String version;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider context;

  /** Field description */
  private PluginManager pluginManager;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private Class<?> storeFactoryClass;
}
