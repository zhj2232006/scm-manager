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


package sonia.scm.repository.client.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.client.api.RepositoryClientException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryClientFactoryProvider
  implements RepositoryClientFactoryProvider
{

  /**
   * Method description
   *
   *
   * @param main
   * @param workingCopy
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public RepositoryClientProvider create(File main, File workingCopy)
    throws IOException
  {
    Git git = null;

    try
    {
      git = Git.cloneRepository().setURI(main.toURI().toString()).setDirectory(
        workingCopy).call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not clone repository", ex);
    }

    return new GitRepositoryClientProvider(git);
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param username
   * @param password
   * @param workingCopy
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public RepositoryClientProvider create(String url, String username,
    String password, File workingCopy)
    throws IOException
  {
    Git git = null;

    CredentialsProvider credentialsProvider = null;
    if ( username != null && password != null ) {
      credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
    }

    try
    {
      git = Git.cloneRepository()
        .setURI(url)
        .setDirectory(workingCopy)
        .setCredentialsProvider(credentialsProvider)
        .call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not clone repository", ex);
    }

    return new GitRepositoryClientProvider(git, credentialsProvider);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getType()
  {
    return GitRepositoryHandler.TYPE_NAME;
  }
}
