/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.i18n;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.repository.Changeset;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;
import java.util.MissingResourceException;

/**
 *
 * @author Sebastian Sdorra
 */
public class I18nMessagesTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testI18n()
  {
    /*
      lookup-order for this test:
      - TM_en (es specified, but not ava)
      - TM_<execution-locale>
      - TM

      This means that, if there is no default locale specified,  this test accidentally passes on non-german machines, an fails on german machines, since the execution locale is de_DE, which is checked even before the fallback locale is considered.
     */

    Locale.setDefault(Locale.ENGLISH);

    TestMessages msg = I18nMessages.get(TestMessages.class);

    assertEquals("Normal Key", msg.normalKey);
    assertEquals("Key with Annotation", msg.keyWithAnnotation);
    assertNull(msg.someObject);
    assertNotNull(msg.bundle);
    assertEquals(Locale.ENGLISH, msg.locale);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testI18nOtherLanguage()
  {
    TestMessages msg = I18nMessages.get(TestMessages.class, Locale.GERMANY);

    assertEquals("Normaler Schlüssel", msg.normalKey);
    assertEquals("Schlüssel mit Annotation", msg.keyWithAnnotation);
    assertNull(msg.someObject);
    assertNotNull(msg.bundle);
    assertEquals(Locale.GERMANY, msg.locale);
  }

  /**
   * Method description
   *
   */
  @Test(expected = MissingResourceException.class)
  public void testMissingBundle()
  {
    Changeset msg = I18nMessages.get(Changeset.class);
  }
}
