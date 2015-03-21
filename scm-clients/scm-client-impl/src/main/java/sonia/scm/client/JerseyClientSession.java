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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ScmState;
import sonia.scm.url.UrlProvider;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientSession implements ScmClientSession
{

  /** the logger for JerseyClientSession */
  private static final Logger logger =
    LoggerFactory.getLogger(JerseyClientSession.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param client
   * @param urlProvider
   * @param state
   */
  public JerseyClientSession(Client client, UrlProvider urlProvider,
    ScmState state)
  {
    this.client = client;
    this.urlProvider = urlProvider;
    this.state = state;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   */
  @Override
  public void close()
  {
    if (logger.isInfoEnabled())
    {
      logger.info("close client session");
    }

    client.destroy();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Client getClient()
  {
    return client;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public GroupClientHandler getGroupHandler()
  {
    return new JerseyGroupClientHandler(this);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryClientHandler getRepositoryHandler()
  {
    return new JerseyRepositoryClientHandler(this);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SecurityClientHandler getSecurityHandler()
  {
    return new JerseySecurityClientHandler(this);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ScmState getState()
  {
    return state;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public UrlProvider getUrlProvider()
  {
    return urlProvider;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public UserClientHandler getUserHandler()
  {
    return new JerseyUserClientHandler(this);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Client client;

  /** Field description */
  private final ScmState state;

  /** Field description */
  private final UrlProvider urlProvider;
}
