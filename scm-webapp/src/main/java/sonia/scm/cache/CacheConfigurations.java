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


package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Iterators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author Sebastian Sdorra
 */
public final class CacheConfigurations
{

  /**
   * the logger for CacheConfigurations
   */
  private static final Logger logger =
    LoggerFactory.getLogger(CacheConfigurations.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private CacheConfigurations() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param loadingClass
   * @param resource
   *
   * @return
   */
  public static Iterator<URL> findModuleResources(Class<?> loadingClass,
    String resource)
  {
    Iterator<URL> it = null;

    try
    {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (classLoader == null)
      {
        classLoader = loadingClass.getClassLoader();
      }

      Enumeration<URL> enm = classLoader.getResources(resource);

      if (enm != null)
      {
        it = Iterators.forEnumeration(enm);
      }

    }
    catch (IOException ex)
    {
      logger.error("could not read module resources", ex);
    }

    if (it == null)
    {
      it = Collections.emptyIterator();
    }

    return it;
  }
}
