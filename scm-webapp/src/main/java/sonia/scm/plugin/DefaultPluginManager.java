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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.config.ScmConfigurationChangedEvent;
import sonia.scm.io.ZipUnArchiver;
import sonia.scm.net.HttpClient;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;
import sonia.scm.version.Version;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * TODO replace aether stuff
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager implements PluginManager
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.plugins";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** the logger for DefaultPluginManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginManager.class);

  /** enable or disable remote plugins */
  private static final boolean REMOTE_PLUGINS_ENABLED = false;

  /** Field description */
  public static final Predicate<PluginInformation> FILTER_UPDATES =
    new StatePluginPredicate(PluginState.UPDATE_AVAILABLE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param context
   * @param configuration
   * @param pluginLoader
   * @param cacheManager
   * @param clientProvider
   */
  @Inject
  public DefaultPluginManager(SCMContextProvider context,
    ScmConfiguration configuration, PluginLoader pluginLoader,
    CacheManager cacheManager, Provider<HttpClient> clientProvider)
  {
    this.context = context;
    this.configuration = configuration;
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.clientProvider = clientProvider;
    installedPlugins = new HashMap<>();

    for (PluginWrapper wrapper : pluginLoader.getInstalledPlugins())
    {
      Plugin plugin = wrapper.getPlugin();
      PluginInformation info = plugin.getInformation();

      if ((info != null) && info.isValid())
      {
        installedPlugins.put(info.getId(), plugin);
      }
    }

    try
    {
      unmarshaller =
        JAXBContext.newInstance(PluginCenter.class).createUnmarshaller();
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clearCache()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear plugin cache");
    }

    cache.clear();
  }

  /**
   * Method description
   *
   *
   * @param config
   */
  @Subscribe
  public void configChanged(ScmConfigurationChangedEvent config)
  {
    clearCache();
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void install(String id)
  {
    PluginPermissions.manage().check();
    
    PluginCenter center = getPluginCenter();

    // pluginHandler.install(id);

    for (PluginInformation plugin : center.getPlugins())
    {
      String pluginId = plugin.getId();

      if (Util.isNotEmpty(pluginId) && pluginId.equals(id))
      {
        plugin.setState(PluginState.INSTALLED);

        // ugly workaround
        Plugin newPlugin = new Plugin();

        // TODO check
        // newPlugin.setInformation(plugin);
        installedPlugins.put(id, newPlugin);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param packageStream
   *
   * @throws IOException
   */
  @Override
  public void installPackage(InputStream packageStream) throws IOException
  {
    PluginPermissions.manage().check();

    File tempDirectory = Files.createTempDir();

    try
    {
      new ZipUnArchiver().extractArchive(packageStream, tempDirectory);

      Plugin plugin = JAXB.unmarshal(new File(tempDirectory, "plugin.xml"),
                        Plugin.class);

      PluginCondition condition = plugin.getCondition();

      if ((condition != null) &&!condition.isSupported())
      {
        throw new PluginConditionFailedException(condition);
      }

      /*
       * AetherPluginHandler aph = new AetherPluginHandler(this, context,
       *                           configuration);
       * Collection<PluginRepository> repositories =
       * Sets.newHashSet(new PluginRepository("package-repository",
       *   "file://".concat(tempDirectory.getAbsolutePath())));
       *
       * aph.setPluginRepositories(repositories);
       *
       * aph.install(plugin.getInformation().getId());
       */
      plugin.getInformation().setState(PluginState.INSTALLED);
      installedPlugins.put(plugin.getInformation().getId(), plugin);

    }
    finally
    {
      IOUtil.delete(tempDirectory);
    }
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void uninstall(String id)
  {
    PluginPermissions.manage().check();

    Plugin plugin = installedPlugins.get(id);

    if (plugin == null)
    {
      String pluginPrefix = getPluginIdPrefix(id);

      for (String nid : installedPlugins.keySet())
      {
        if (nid.startsWith(pluginPrefix))
        {
          id = nid;
          plugin = installedPlugins.get(nid);

          break;
        }
      }
    }

    if (plugin == null)
    {
      throw new PluginNotInstalledException(id.concat(" is not install"));
    }

    /*
     * if (pluginHandler == null)
     * {
     * getPluginCenter();
     * }
     *
     * pluginHandler.uninstall(id);
     */
    installedPlugins.remove(id);
    preparePlugins(getPluginCenter());
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void update(String id)
  {
    PluginPermissions.manage().check();

    String[] idParts = id.split(":");
    String groupId = idParts[0];
    String artefactId = idParts[1];
    PluginInformation installed = null;

    for (PluginInformation info : getInstalled())
    {
      if (groupId.equals(info.getGroupId())
        && artefactId.equals(info.getArtifactId()))
      {
        installed = info;

        break;
      }
    }

    if (installed == null)
    {
      StringBuilder msg = new StringBuilder(groupId);

      msg.append(":").append(groupId).append(" is not install");

      throw new PluginNotInstalledException(msg.toString());
    }

    uninstall(installed.getId());
    install(id);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public PluginInformation get(String id)
  {
    PluginPermissions.read().check();

    PluginInformation result = null;

    for (PluginInformation info : getPluginCenter().getPlugins())
    {
      if (id.equals(info.getId()))
      {
        result = info;

        break;
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param predicate
   *
   * @return
   */
  @Override
  public Set<PluginInformation> get(Predicate<PluginInformation> predicate)
  {
    AssertUtil.assertIsNotNull(predicate);
    PluginPermissions.read().check();

    Set<PluginInformation> infoSet = new HashSet<>();

    filter(infoSet, getInstalled(), predicate);
    filter(infoSet, getPluginCenter().getPlugins(), predicate);

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAll()
  {
    PluginPermissions.read().check();

    Set<PluginInformation> infoSet = getInstalled();

    infoSet.addAll(getPluginCenter().getPlugins());

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAvailable()
  {
    PluginPermissions.read().check();

    Set<PluginInformation> availablePlugins = new HashSet<>();
    Set<PluginInformation> centerPlugins = getPluginCenter().getPlugins();

    for (PluginInformation info : centerPlugins)
    {
      if (!installedPlugins.containsKey(info.getId()))
      {
        availablePlugins.add(info);
      }
    }

    return availablePlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<PluginInformation> getAvailableUpdates()
  {
    PluginPermissions.read().check();

    return get(FILTER_UPDATES);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<PluginInformation> getInstalled()
  {
    PluginPermissions.read().check();

    Set<PluginInformation> infoSet = new LinkedHashSet<>();

    for (Plugin plugin : installedPlugins.values())
    {
      infoSet.add(plugin.getInformation());
    }

    return infoSet;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param url
   * @return
   */
  private String buildPluginUrl(String url)
  {
    String os = SystemUtil.getOS();
    String arch = SystemUtil.getArch();

    try
    {
      os = URLEncoder.encode(os, ENCODING);
    }
    catch (UnsupportedEncodingException ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return url.replace("{version}", context.getVersion()).replace("{os}",
      os).replace("{arch}", arch);
  }

  /**
   * Method description
   *
   *
   * @param target
   * @param source
   * @param predicate
   */
  private void filter(Set<PluginInformation> target,
    Collection<PluginInformation> source,
    Predicate<PluginInformation> predicate)
  {
    for (PluginInformation info : source)
    {
      if (predicate.apply(info))
      {
        target.add(info);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param available
   */
  private void preparePlugin(PluginInformation available)
  {
    PluginState state = PluginState.AVAILABLE;

    for (PluginInformation installed : getInstalled())
    {
      if (isSamePlugin(available, installed))
      {
        if (installed.getVersion().equals(available.getVersion()))
        {
          state = PluginState.INSTALLED;
        }
        else if (isNewer(available, installed))
        {
          state = PluginState.UPDATE_AVAILABLE;
        }
        else
        {
          state = PluginState.NEWER_VERSION_INSTALLED;
        }

        break;
      }
    }

    available.setState(state);
  }

  /**
   * Method description
   *
   *
   * @param pc
   */
  private void preparePlugins(PluginCenter pc)
  {
    Set<PluginInformation> infoSet = pc.getPlugins();

    if (infoSet != null)
    {
      Iterator<PluginInformation> pit = infoSet.iterator();

      while (pit.hasNext())
      {
        PluginInformation available = pit.next();

        if (isCorePluging(available))
        {
          pit.remove();
        }
        else
        {
          preparePlugin(available);
        }
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
  private PluginCenter getPluginCenter()
  {
    PluginCenter center = cache.get(PluginCenter.class.getName());

    if (center == null)
    {
      synchronized (DefaultPluginManager.class)
      {
        String pluginUrl = configuration.getPluginUrl();

        pluginUrl = buildPluginUrl(pluginUrl);

        if (logger.isInfoEnabled())
        {
          logger.info("fetch plugin informations from {}", pluginUrl);
        }

        /**
         * remote plugins are disabled for early 2.0.0-SNAPSHOTS
         * TODO enable remote plugins later
         */
        if (REMOTE_PLUGINS_ENABLED && Util.isNotEmpty(pluginUrl))
        {
          InputStream input = null;

          try
          {
            input = clientProvider.get().get(pluginUrl).getContent();

            /*
             *  TODO: add gzip support
             *
             * if (gzip)
             * {
             * input = new GZIPInputStream(input);
             * }
             */
            center = (PluginCenter) unmarshaller.unmarshal(input);
            preparePlugins(center);
            cache.put(PluginCenter.class.getName(), center);

            /*
             * if (pluginHandler == null)
             * {
             * pluginHandler = new AetherPluginHandler(this,
             *   SCMContext.getContext(), configuration,
             *   advancedPluginConfiguration);
             * }
             *
             * pluginHandler.setPluginRepositories(center.getRepositories());
             */
          }
          catch (IOException | JAXBException ex)
          {
            logger.error("could not load plugins from plugin center", ex);
          }
          finally
          {
            IOUtil.close(input);
          }
        }

        if (center == null)
        {
          center = new PluginCenter();
        }
      }
    }

    return center;
  }

  /**
   * Method description
   *
   *
   * @param pluginId
   *
   * @return
   */
  private String getPluginIdPrefix(String pluginId)
  {
    return pluginId.substring(0, pluginId.lastIndexOf(':'));
  }

  /**
   * Method description
   *
   *
   * @param available
   *
   * @return
   */
  private boolean isCorePluging(PluginInformation available)
  {
    boolean core = false;

    for (Plugin installedPlugin : installedPlugins.values())
    {
      PluginInformation installed = installedPlugin.getInformation();

      if (isSamePlugin(available, installed)
        && (installed.getState() == PluginState.CORE))
      {
        core = true;

        break;
      }
    }

    return core;
  }

  /**
   * Method description
   *
   *
   * @param available
   * @param installed
   *
   * @return
   */
  private boolean isNewer(PluginInformation available,
    PluginInformation installed)
  {
    boolean result = false;
    Version version = Version.parse(available.getVersion());

    if (version != null)
    {
      result = version.isNewer(installed.getVersion());
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param p1
   * @param p2
   *
   * @return
   */
  private boolean isSamePlugin(PluginInformation p1, PluginInformation p2)
  {
    return p1.getGroupId().equals(p2.getGroupId())
      && p1.getArtifactId().equals(p2.getArtifactId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Cache<String, PluginCenter> cache;

  /** Field description */
  private final Provider<HttpClient> clientProvider;

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final SCMContextProvider context;

  /** Field description */
  private final Map<String, Plugin> installedPlugins;

  /** Field description */
  private Unmarshaller unmarshaller;
}
