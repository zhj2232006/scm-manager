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



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;

/**
 * The main class for retrieving the home and the version of the SCM-Manager.
 * This class is a singleton which can be retrieved via injection
 * or with the static {@link SCMContext#getContext()} method.
 *
 * @author Sebastian Sdorra
 */
public interface SCMContextProvider extends Closeable
{

  /**
   * Initializes the {@link SCMContextProvider}.
   * This method is called when the SCM manager is started.
   *
   */
  public void init();

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the base directory of the SCM-Manager.
   *
   *
   * @return base directory of the SCM-Manager
   */
  public File getBaseDirectory();

  /**
   * Returns the current stage of SCM-Manager.
   *
   *
   * @return stage of SCM-Manager
   * @since 1.12
   */
  public Stage getStage();

  /**
   * Returns a exception which is occurred on context startup.
   * The method returns null if the start was successful.
   *
   *
   * @return startup exception of null
   * @since 1.14
   */
  public Throwable getStartupError();

  /**
   * Returns the version of the SCM-Manager.
   *
   *
   * @return version of the SCM-Manager
   */
  public String getVersion();
}
