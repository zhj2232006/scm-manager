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



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.util.MockUtil;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.List;
import org.junit.Rule;

/**
 *
 * @author Sebastian Sdorra
 */
@SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class DefaultUserManagerTest extends UserManagerTestBase
{
  
  @Rule
  public ShiroRule shiro = new ShiroRule();

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public UserManager createManager()
  {
    return new DefaultUserManager(createXmlUserDAO());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDefaultAccountAfterFristStart()
  {
    UserDAO userDAO = mock(UserDAO.class);
    List<User> users = Lists.newArrayList(new User("tuser"));

    when(userDAO.getAll()).thenReturn(users);

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.init(contextProvider);
    verify(userDAO, never()).add(any(User.class));
  }

  /**
   * Method description
   *
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testDefaultAccountCreation()
  {
    UserDAO userDAO = mock(UserDAO.class);

    when(userDAO.getAll()).thenReturn(Collections.EMPTY_LIST);

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.init(contextProvider);
    verify(userDAO, times(2)).add(any(User.class));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private XmlUserDAO createXmlUserDAO()
  {
    StoreFactory factory = new JAXBStoreFactory();

    factory.init(contextProvider);

    return new XmlUserDAO(factory);
  }
}
