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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import sonia.scm.Type;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.StoreFactory;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;
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
public class DefaultRepositoryManagerTest extends RepositoryManagerTestBase
{

  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public HookContext createHookContext(Repository repository)
  {
    PreProcessorUtil ppu = mock(PreProcessorUtil.class);
    HookContextProvider provider = mock(HookContextProvider.class);
    Set<HookFeature> features = ImmutableSet.of();

    when(provider.getSupportedFeatures()).thenReturn(features);

    return new HookContextFactory(ppu).createContext(provider, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Test
  public void getRepositoryFromRequestUriTest()
    throws RepositoryException, IOException
  {
    RepositoryManager m = createManager();

    m.init(contextProvider);
    createRepository(m, new Repository("1", "hg", "scm"));
    createRepository(m, new Repository("2", "hg", "scm-test"));
    createRepository(m, new Repository("3", "git", "project1/test-1"));
    createRepository(m, new Repository("4", "git", "project1/test-2"));
    assertEquals("scm", m.getFromUri("hg/scm").getName());
    assertEquals("scm-test", m.getFromUri("hg/scm-test").getName());
    assertEquals("scm-test", m.getFromUri("/hg/scm-test").getName());
    assertEquals("project1/test-1",
      m.getFromUri("/git/project1/test-1").getName());
    assertEquals("project1/test-1",
      m.getFromUri("/git/project1/test-1/ka/some/path").getName());
    assertNull(m.getFromUri("/git/project1/test-3/ka/some/path"));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected DefaultRepositoryManager createManager()
  {
    return createRepositoryManager(false);
  }

  /**
   * Method description
   *
   *
   * @param archiveEnabled
   *
   * @return
   */
  @Override
  protected DefaultRepositoryManager createRepositoryManager(
    boolean archiveEnabled)
  {
    Set<RepositoryHandler> handlerSet = new HashSet<>();
    StoreFactory factory = new JAXBStoreFactory();

    factory.init(contextProvider);
    handlerSet.add(new DummyRepositoryHandler(factory));
    handlerSet.add(new DummyRepositoryHandler(factory)
    {
      @Override
      public Type getType()
      {
        return new Type("hg", "Mercurial");
      }
    });
    handlerSet.add(new DummyRepositoryHandler(factory)
    {
      @Override
      public Type getType()
      {
        return new Type("git", "Git");
      }
    });

    XmlRepositoryDAO repositoryDAO = new XmlRepositoryDAO(factory);

    ScmConfiguration configuration = new ScmConfiguration();

    configuration.setEnableRepositoryArchive(archiveEnabled);

    return new DefaultRepositoryManager(configuration, contextProvider,
      new DefaultKeyGenerator(), repositoryDAO, handlerSet);
  }

  /**
   * Method description
   *
   *
   * @param m
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void createRepository(RepositoryManager m, Repository repository)
    throws RepositoryException, IOException
  {
    m.create(repository);
  }
}
