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

import org.junit.Assume;

import sonia.scm.SCMContext;
import sonia.scm.io.FileSystem;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgTestUtil
{

  /**
   * Constructs ...
   *
   */
  private HgTestUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   */
  public static void checkForSkip(HgRepositoryHandler handler)
  {

    // skip tests if hg not in path
    if (!handler.isConfigured())
    {
      System.out.println("WARNING could not find hg, skipping test");
      Assume.assumeTrue(false);
    }

    if (Boolean.getBoolean("sonia.scm.test.skip.hg"))
    {
      System.out.println("WARNING mercurial test are disabled");
      Assume.assumeTrue(false);
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  public static HgRepositoryHandler createHandler(File directory)
  {
    TempSCMContextProvider context =
      (TempSCMContextProvider) SCMContext.getContext();

    context.setBaseDirectory(directory);

    FileSystem fileSystem = mock(FileSystem.class);

    HgRepositoryHandler handler =
      new HgRepositoryHandler(new InMemoryConfigurationStoreFactory(), fileSystem,
        new HgContextProvider());

    handler.init(context);

    return handler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static HgHookManager createHookManager()
  {
    HgHookManager hookManager = mock(HgHookManager.class);

    when(hookManager.getChallenge()).thenReturn("challenge");
    when(hookManager.createUrl()).thenReturn(
      "http://localhost:8081/scm/hook/hg/");
    when(hookManager.createUrl(any(HttpServletRequest.class))).thenReturn(
      "http://localhost:8081/scm/hook/hg/");

    return hookManager;
  }
}
