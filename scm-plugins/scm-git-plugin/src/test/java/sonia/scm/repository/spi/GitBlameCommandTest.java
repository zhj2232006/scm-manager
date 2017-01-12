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

import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.RepositoryException;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import sonia.scm.repository.GitConstants;

/**
 * Unit tests for {@link GitBlameCommand}.
 * 
 * @author Sebastian Sdorra
 */
public class GitBlameCommandTest extends AbstractGitCommandTestBase
{

  /**
   * Tests blame command with default branch.
   * 
   * @throws IOException
   * @throws RepositoryException 
   */
  @Test
  public void testDefaultBranch() throws IOException, RepositoryException {
    // without default branch, the repository head should be used
    BlameCommandRequest request = new BlameCommandRequest();
    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);
    assertNotNull(result);
    assertEquals(2, result.getTotal()); 
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getLine(0).getRevision());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getLine(1).getRevision());
    
    // set default branch and test again
    repository.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "test-branch");
    result = createCommand().getBlameResult(request);
    assertNotNull(result);
    assertEquals(1, result.getTotal()); 
    assertEquals("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", result.getLine(0).getRevision());
  }
  
  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetBlameResult() throws IOException, RepositoryException
  {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
    line = result.getLine(1);
    assertEquals(2, line.getLineNumber());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
                 line.getRevision());
    checkDate(line.getWhen());
    assertEquals("line for blame", line.getCode());
    assertEquals("added new line for blame", line.getDescription());
    assertEquals("Zaphod Beeblebrox", line.getAuthor().getName());
    assertEquals("zaphod.beeblebrox@hitchhiker.com",
                 line.getAuthor().getMail());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void testGetBlameResultWithRevision()
          throws IOException, RepositoryException
  {
    BlameCommandRequest request = new BlameCommandRequest();

    request.setPath("a.txt");
    request.setRevision("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");

    BlameResult result = createCommand().getBlameResult(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());

    BlameLine line = result.getLine(0);

    checkFirstLine(line);
  }

  /**
   * Method description
   *
   *
   * @param line
   */
  private void checkFirstLine(BlameLine line)
  {
    assertEquals(1, line.getLineNumber());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca",
                 line.getRevision());
    checkDate(line.getWhen());
    assertEquals("a", line.getCode());
    assertEquals("added a and b files", line.getDescription());
    assertEquals("Douglas Adams", line.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", line.getAuthor().getMail());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private GitBlameCommand createCommand()
  {
    return new GitBlameCommand(createContext(), repository);
  }
}
