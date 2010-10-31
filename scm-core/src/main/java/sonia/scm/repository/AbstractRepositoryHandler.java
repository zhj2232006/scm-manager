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

import sonia.scm.ConfigChangedListener;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <C>
 */
public abstract class AbstractRepositoryHandler<C extends BasicRepositoryConfig>
        implements RepositoryHandler
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Class<C> getConfigClass();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void addListener(ConfigChangedListener listener)
  {
    listenerSet.add(listener);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    String name = getType().getName();

    configFile =
      new File(context.getBaseDirectory(),
               "config".concat(File.separator).concat(name).concat(".xml"));
    loadConfig();
  }

  /**
   * Method description
   *
   */
  public void loadConfig()
  {
    if (configFile.exists())
    {
      config = JAXB.unmarshal(configFile, getConfigClass());
    }
  }

  /**
   * Method description
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(ConfigChangedListener listener)
  {
    listenerSet.remove(listener);
  }

  /**
   * Method description
   *
   */
  public void storeConfig()
  {
    if (config != null)
    {
      JAXB.marshal(config, configFile);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public C getConfig()
  {
    return config;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isConfigured()
  {
    return config != null;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  public void setConfig(C config)
  {
    this.config = config;
    fireConfigChanged();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  protected String buildUrl(Repository repository)
  {
    String url = config.getBaseUrl();

    if (Util.isNotEmpty(url))
    {
      if (!url.endsWith("/"))
      {
        url = url.concat("/");
      }

      url = url.concat(repository.getName());
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  protected void initNewRepository(Repository repository)
  {
    repository.setId(UUID.randomUUID().toString());
    repository.setUrl(buildUrl(repository));
    repository.setCreationDate(new Date());
  }

  /**
   * Method description
   *
   */
  private void fireConfigChanged()
  {
    for (ConfigChangedListener listener : listenerSet)
    {
      listener.configChanged(config);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected C config;

  /** Field description */
  protected File configFile;

  /** Field description */
  private Set<ConfigChangedListener> listenerSet =
    new HashSet<ConfigChangedListener>();
}
