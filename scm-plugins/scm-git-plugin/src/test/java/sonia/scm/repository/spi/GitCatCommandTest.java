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


package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.repository.RepositoryException;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import sonia.scm.repository.GitConstants;

/**
 * Unit tests for {@link GitCatCommand}.
 * 
 * TODO add not found test
 *
 * @author Sebastian Sdorra
 */
public class GitCatCommandTest extends AbstractGitCommandTestBase
{
  
  /**
   * Tests cat command with default branch.
   * 
   * @throws IOException
   * @throws RepositoryException 
   */
  @Test
  public void testDefaultBranch() throws IOException, RepositoryException {
    // without default branch, the repository head should be used
    CatCommandRequest request = new CatCommandRequest();
    request.setPath("a.txt");
    
    assertEquals("a\nline for blame", execute(request));
    
    // set default branch for repository and check again
    repository.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "test-branch");
    assertEquals("a and b", execute(request));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testCat() throws IOException, RepositoryException
  {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("a.txt");
    request.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertEquals("a and b", execute(request));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testSimpleCat() throws IOException, RepositoryException
  {
    CatCommandRequest request = new CatCommandRequest();

    request.setPath("b.txt");
    assertEquals("b", execute(request));
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private String execute(CatCommandRequest request)
          throws IOException, RepositoryException
  {
    String content = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      new GitCatCommand(createContext(), repository).getCatResult(request,
                        baos);
    }
    finally
    {
      content = baos.toString().trim();
    }

    return content;
  }
}
