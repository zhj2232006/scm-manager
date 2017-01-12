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

import sonia.scm.repository.Branch;
import sonia.scm.repository.client.api.RepositoryClientException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import org.eclipse.jgit.lib.Ref;
import sonia.scm.repository.GitUtil;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitBranchCommand implements BranchCommand
{

  /**
   * Constructs ...
   *
   *
   * @param git
   */
  GitBranchCommand(Git git)
  {
    this.git = git;
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public Branch branch(String name) throws IOException
  {
    try
    {
      Ref ref = git.branchCreate().setName(name).call();
      return new Branch(name, GitUtil.getId(ref.getObjectId()));
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not create branch", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Git git;
}
