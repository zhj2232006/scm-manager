/**
 * Copyright (c) 2014, Sebastian Sdorra
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

package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.HandlerEventType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserModificationEvent;
import sonia.scm.user.UserTestData;

/**
 * Unit tests for {@link AuthorizationCollector}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthorizationCollectorTest {

  @Mock
  private Cache cache;
  
  @Mock
  private CacheManager cacheManager;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private SecuritySystem securitySystem;
  
  private DefaultAuthorizationCollector collector;
  
  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  /**
   * Set up object to test.
   */
  @Before
  public void setUp(){
    when(cacheManager.getCache(Mockito.any(String.class))).thenReturn(cache);
    
    collector = new DefaultAuthorizationCollector(cacheManager, repositoryDAO, securitySystem);
  }

  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.user.UserEvent)}.
   */
  @Test
  public void testOnUserEvent()
  {
    User user = UserTestData.createDent();
    collector.onEvent(new UserEvent(HandlerEventType.BEFORE_CREATE, user));
    verify(cache, never()).clear();
    
    collector.onEvent(new UserEvent(HandlerEventType.CREATE, user));
    verify(cache).removeAll(Mockito.any(Predicate.class));
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.user.UserEvent)} with modified user.
   */
  @Test  
  public void testOnUserModificationEvent()
  {
    User user = UserTestData.createDent();
    User userModified = UserTestData.createDent();
    userModified.setDisplayName("Super Dent");
    
    collector.onEvent(new UserModificationEvent(HandlerEventType.BEFORE_CREATE, userModified, user));
    verify(cache, never()).removeAll(Mockito.any(Predicate.class));
    
    collector.onEvent(new UserModificationEvent(HandlerEventType.CREATE, userModified, user));
    verify(cache, never()).removeAll(Mockito.any(Predicate.class));
    
    userModified.setAdmin(true);
    
    collector.onEvent(new UserModificationEvent(HandlerEventType.BEFORE_CREATE, userModified, user));
    verify(cache, never()).removeAll(Mockito.any(Predicate.class));
    
    collector.onEvent(new UserModificationEvent(HandlerEventType.CREATE, userModified, user));
    verify(cache).removeAll(Mockito.any(Predicate.class));
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.group.GroupEvent)}.
   */
  @Test
  public void testOnGroupEvent()
  {
    Group group = new Group("xml", "base");
    collector.onEvent(new GroupEvent(HandlerEventType.BEFORE_CREATE, group));
    verify(cache, never()).clear();
    
    collector.onEvent(new GroupEvent(HandlerEventType.CREATE, group));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.group.GroupEvent)} with modified groups.
   */
  @Test
  public void testOnGroupModificationEvent()
  {
    Group group = new Group("xml", "base");
    Group modifiedGroup = new Group("xml", "base");
    collector.onEvent(new GroupModificationEvent(HandlerEventType.BEFORE_MODIFY, modifiedGroup, group));
    verify(cache, never()).clear();
    
    collector.onEvent(new GroupModificationEvent(HandlerEventType.MODIFY, modifiedGroup, group));
    verify(cache, never()).clear();
    
    modifiedGroup.add("test");
    collector.onEvent(new GroupModificationEvent(HandlerEventType.MODIFY, modifiedGroup, group));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.repository.RepositoryEvent)}.
   */
  @Test
  public void testOnRepositoryEvent()
  {
    Repository repository = RepositoryTestData.createHeartOfGold();
    collector.onEvent(new RepositoryEvent(HandlerEventType.BEFORE_CREATE, repository));
    verify(cache, never()).clear();
    
    collector.onEvent(new RepositoryEvent(HandlerEventType.CREATE, repository));
    verify(cache).clear();
  }
  
 /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.repository.RepositoryEvent)} with modified repository.
   */
  @Test
  public void testOnRepositoryModificationEvent()
  {
    Repository repositoryModified = RepositoryTestData.createHeartOfGold();
    repositoryModified.setName("test123");
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    
    Repository repository = RepositoryTestData.createHeartOfGold();
    repository.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.BEFORE_CREATE, repositoryModified, repository));
    verify(cache, never()).clear();
    
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    verify(cache, never()).clear();
    
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test")));
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    verify(cache, never()).clear();
    
    repositoryModified.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("test123")));
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    verify(cache).clear();
    
    repositoryModified.setPermissions(
      Lists.newArrayList(new sonia.scm.repository.Permission("test", PermissionType.READ, true))
    );
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    verify(cache, times(2)).clear();
    
    repositoryModified.setPermissions(
      Lists.newArrayList(new sonia.scm.repository.Permission("test", PermissionType.WRITE))
    );
    collector.onEvent(new RepositoryModificationEvent(HandlerEventType.CREATE, repositoryModified, repository));
    verify(cache, times(3)).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#onEvent(sonia.scm.security.StoredAssignedPermissionEvent)}.
   */
  @Test
  public void testOnStoredAssignedPermissionEvent()
  {
    StoredAssignedPermission groupPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("_authenticated", true, "repository:read:*")
    );
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, groupPermission));
    verify(cache, never()).clear();
    
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.CREATE, groupPermission));
    verify(cache).clear();
    
    
    StoredAssignedPermission userPermission = new StoredAssignedPermission(
      "123", new AssignedPermission("trillian", false, "repository:read:*")
    );
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.BEFORE_CREATE, userPermission));
    verify(cache, never()).removeAll(Mockito.any(Predicate.class));
    verify(cache).clear();
    
    collector.onEvent(new StoredAssignedPermissionEvent(HandlerEventType.CREATE, userPermission));
    verify(cache).removeAll(Mockito.any(Predicate.class));
    verify(cache).clear();
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} without user role.
   */
  @Test
  @SubjectAware
  public void testCollectWithoutUserRole()
  {
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), nullValue());
    assertThat(authInfo.getStringPermissions(), nullValue());
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} from cache.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectFromCache()
  {
    AuthorizationInfo info = new SimpleAuthorizationInfo();
    when(cache.get(anyObject())).thenReturn(info);
    authenticate(UserTestData.createTrillian(), "main");
    
    AuthorizationInfo authInfo = collector.collect();
    assertSame(info, authInfo);
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} with cache.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithCache(){
    authenticate(UserTestData.createTrillian(), "main");
    
    AuthorizationInfo authInfo = collector.collect();
    verify(cache).put(any(), any());
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} without permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithoutPermissions()
  {
    authenticate(UserTestData.createTrillian(), "main");
    
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.contains(Role.USER));
    assertThat(authInfo.getStringPermissions(), hasSize(0));
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} as admin.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectAsAdmin()
  {
    User trillian = UserTestData.createTrillian();
    trillian.setAdmin(true);
    authenticate(trillian, "main");
    
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER, Role.ADMIN));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), Matchers.contains("*"));
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} with repository permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithRepositoryPermissions()
  {
    String group = "heart-of-gold-crew";
    authenticate(UserTestData.createTrillian(), group);
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("one");
    heartOfGold.setPermissions(Lists.newArrayList(new sonia.scm.repository.Permission("trillian")));
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    puzzle42.setId("two");
    sonia.scm.repository.Permission permission = new sonia.scm.repository.Permission(group, PermissionType.WRITE, true);
    puzzle42.setPermissions(Lists.newArrayList(permission));
    when(repositoryDAO.getAll()).thenReturn(Lists.newArrayList(heartOfGold, puzzle42));
    
    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("repository:read:one", "repository:read,write:two"));
  }
  
  /**
   * Tests {@link AuthorizationCollector#collect()} with global permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithGlobalPermissions(){
    authenticate(UserTestData.createTrillian(), "main");
    
    StoredAssignedPermission p1 = new StoredAssignedPermission("one", new AssignedPermission("one", "one:one"));
    StoredAssignedPermission p2 = new StoredAssignedPermission("two", new AssignedPermission("two", "two:two"));
    when(securitySystem.getPermissions(Mockito.any(Predicate.class))).thenReturn(Lists.newArrayList(p1, p2));
    
    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("one:one", "two:two"));
  }
  
  private void authenticate(User user, String group, String... groups)
  {
    SimplePrincipalCollection spc = new SimplePrincipalCollection();
    spc.add(user.getName(), "unit");
    spc.add(user, "unit");
    spc.add(new GroupNames(group, groups), "unit");
    Subject subject = new Subject.Builder().authenticated(true).principals(spc).buildSubject();
    shiro.setSubject(subject);
  }

}