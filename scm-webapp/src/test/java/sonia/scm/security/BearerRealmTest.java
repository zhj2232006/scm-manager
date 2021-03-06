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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.group.GroupDAO;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.security.SecureRandom;

import java.util.Date;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

/**
 * Unit tests for {@link BearerRealm}.
 * 
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class BearerRealmTest
{
  
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  /**
   * Method description
   *
   */
  @Test
  public void testDoGetAuthenticationInfo()
  {
    SecureKey key = createSecureKey();

    User marvin = UserTestData.createMarvin();

    when(userDAO.get(marvin.getName())).thenReturn(marvin);

    resolveKey(key);

    String compact = createCompactToken(marvin.getName(), key);

    BearerToken token = BearerToken.valueOf(compact);
    AuthenticationInfo info = realm.doGetAuthenticationInfo(token);

    assertNotNull(info);

    PrincipalCollection principals = info.getPrincipals();

    assertEquals(marvin.getName(), principals.getPrimaryPrincipal());
    assertEquals(marvin, principals.oneByType(User.class));
    assertNotNull(principals.oneByType(Scope.class));
    assertTrue(principals.oneByType(Scope.class).isEmpty());
  }
  
  /**
   * Test {@link BearerRealm#doGetAuthenticationInfo(AuthenticationToken)} with scope.
   *
   */
  @Test
  public void testDoGetAuthenticationInfoWithScope()
  {
    SecureKey key = createSecureKey();

    User marvin = UserTestData.createMarvin();

    when(userDAO.get(marvin.getName())).thenReturn(marvin);

    resolveKey(key);

    String compact = createCompactToken(
      marvin.getName(), 
      key, 
      new Date(System.currentTimeMillis() + 60000), 
      Scope.valueOf("repo:*", "user:*")
    );
    
    AuthenticationInfo info = realm.doGetAuthenticationInfo(BearerToken.valueOf(compact));
    Scope scope = info.getPrincipals().oneByType(Scope.class);
    assertThat(scope, Matchers.containsInAnyOrder("repo:*", "user:*"));
  }
  
  /**
   * Test {@link BearerRealm#doGetAuthenticationInfo(AuthenticationToken)} with a failed
   * claims validation.
   */
  @Test
  public void testDoGetAuthenticationInfoWithInvalidClaims()
  {
    SecureKey key = createSecureKey();
    User marvin = UserTestData.createMarvin();
    when(userDAO.get(marvin.getName())).thenReturn(marvin);

    resolveKey(key);

    String compact = createCompactToken(marvin.getName(), key);

    // treat claims as invalid
    when(validator.validate(Mockito.anyMap())).thenReturn(false);
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
    expectedException.expectMessage(Matchers.containsString("claims"));
    
    // kick authentication
    realm.doGetAuthenticationInfo(BearerToken.valueOf(compact));
  }

  /**
   * Method description
   *
   */
  @Test(expected = AuthenticationException.class)
  public void testDoGetAuthenticationInfoWithExpiredToken()
  {
    User trillian = UserTestData.createTrillian();

    when(userDAO.get(trillian.getName())).thenReturn(trillian);

    SecureKey key = createSecureKey();

    resolveKey(key);

    Date exp = new Date(System.currentTimeMillis() - 600L);
    String compact = createCompactToken(trillian.getName(), key, exp, Scope.empty());

    realm.doGetAuthenticationInfo(BearerToken.valueOf(compact));
  }

  /**
   * Method description
   *
   */
  @Test(expected = AuthenticationException.class)
  public void testDoGetAuthenticationInfoWithInvalidSignature()
  {
    resolveKey(createSecureKey());

    User trillian = UserTestData.createTrillian();
    String compact = createCompactToken(trillian.getName(), createSecureKey());

    realm.doGetAuthenticationInfo(BearerToken.valueOf(compact));
  }

  /**
   * Method description
   *
   */
  @Test(expected = AuthenticationException.class)
  public void testDoGetAuthenticationInfoWithoutSignature()
  {
    User trillian = UserTestData.createTrillian();

    when(userDAO.get(trillian.getName())).thenReturn(trillian);

    String compact = Jwts.builder().setSubject("test").compact();

    realm.doGetAuthenticationInfo(BearerToken.valueOf(compact));
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDoGetAuthenticationInfoWrongToken()
  {
    realm.doGetAuthenticationInfo(new UsernamePasswordToken("test", "test"));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    when(validator.validate(Mockito.anyMap())).thenReturn(true);
    Set<TokenClaimsValidator> validators = Sets.newHashSet(validator);
    realm = new BearerRealm(helperFactory, keyResolver, validators);
  }

  //~--- methods --------------------------------------------------------------

private String createCompactToken(String subject, SecureKey key) {
    return createCompactToken(subject, key, Scope.empty());
  }
  
  private String createCompactToken(String subject, SecureKey key, Scope scope) {
    return createCompactToken(subject, key, new Date(System.currentTimeMillis() + 60000), scope);
  }

  private String createCompactToken(String subject, SecureKey key, Date exp, Scope scope) {
    return Jwts.builder()
      .claim(Scopes.CLAIMS_KEY, ImmutableList.copyOf(scope))
      .setSubject(subject)
      .setExpiration(exp)
      .signWith(SignatureAlgorithm.HS256, key.getBytes())
      .compact();
  }

  private SecureKey createSecureKey() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return new SecureKey(bytes, System.currentTimeMillis());
  }

  private void resolveKey(SecureKey key) {
    when(
      keyResolver.resolveSigningKey(
        any(JwsHeader.class), 
        any(Claims.class)
      )
    )
    .thenReturn(
      new SecretKeySpec(
        key.getBytes(), 
        SignatureAlgorithm.HS256.getValue()
      )
    );
  }

  //~--- fields ---------------------------------------------------------------
  
  /** Field description */
  private final SecureRandom random = new SecureRandom();

  @InjectMocks
  private DAORealmHelperFactory helperFactory;
  
  @Mock
  private LoginAttemptHandler loginAttemptHandler;
  
  @Mock
  private TokenClaimsValidator validator;
  
  /** Field description */
  @Mock
  private GroupDAO groupDAO;

  /** Field description */
  @Mock
  private SecureKeyResolver keyResolver;

  /** Field description */
  private BearerRealm realm;

  /** Field description */
  @Mock
  private UserDAO userDAO;
}
