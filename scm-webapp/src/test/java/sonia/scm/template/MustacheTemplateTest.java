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


package sonia.scm.template;

//~--- non-JDK imports --------------------------------------------------------

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import com.google.common.base.Function;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class MustacheTemplateTest extends TemplateTestBase
{

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Template getFailureTemplate() throws IOException
  {
    return getTemplate("sonia/scm/template/003.mustache");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Template getHelloTemplate()
  {
    return getTemplate("sonia/scm/template/001.mustache");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param env
   */
  @Override
  protected void prepareEnv(Map<String, Object> env)
  {
    env.put("test", (Function<String, String>) input -> {
      throw new UnsupportedOperationException("Not supported yet.");
    });
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private Template getTemplate(String path)
  {
    DefaultMustacheFactory factory = new DefaultMustacheFactory();
    Mustache mustache = factory.compile(path);

    return new MustacheTemplate(path, mustache);
  }
}
