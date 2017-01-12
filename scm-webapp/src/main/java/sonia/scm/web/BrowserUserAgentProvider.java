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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import sonia.scm.plugin.Extension;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 */
@Extension
public class BrowserUserAgentProvider implements UserAgentProvider
{

  /** Field description */
  @VisibleForTesting
  static final UserAgent CHROME = UserAgent.builder(
                                    "Chrome").basicAuthenticationCharset(
                                    Charsets.UTF_8).build();

  /** Field description */
  private static final String CHROME_PATTERN = "chrome";

  /** Field description */
  @VisibleForTesting
  static final UserAgent FIREFOX = UserAgent.builder("Firefox").build();

  /** Field description */
  private static final String FIREFOX_PATTERN = "firefox";

  /** Field description */
  @VisibleForTesting
  static final UserAgent MSIE = UserAgent.builder("Internet Explorer").build();

  /** Field description */
  private static final String MSIE_PATTERN = "msie";

  /** Field description */
  @VisibleForTesting    // todo check charset
  static final UserAgent SAFARI = UserAgent.builder("Safari").build();

  /** Field description */
  private static final String OPERA_PATTERN = "opera";

  /** Field description */
  private static final String SAFARI_PATTERN = "safari";

  /** Field description */
  @VisibleForTesting    // todo check charset
  static final UserAgent OPERA = UserAgent.builder(
                                   "Opera").basicAuthenticationCharset(
                                   Charsets.UTF_8).build();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param userAgentString
   *
   * @return
   */
  @Override
  public UserAgent parseUserAgent(String userAgentString)
  {
    UserAgent ua = null;

    if (userAgentString.contains(CHROME_PATTERN))
    {
      ua = CHROME;
    }
    else if (userAgentString.contains(FIREFOX_PATTERN))
    {
      ua = FIREFOX;
    }
    else if (userAgentString.contains(OPERA_PATTERN))
    {
      ua = OPERA;
    }
    else if (userAgentString.contains(MSIE_PATTERN))
    {
      ua = MSIE;
    }
    else if (userAgentString.contains(SAFARI_PATTERN))
    {
      ua = SAFARI;
    }

    return ua;
  }
}
