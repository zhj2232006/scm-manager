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

import com.google.common.collect.Maps;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link XsrfTokenClaimsValidator}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class XsrfTokenClaimsValidatorTest {

  @Mock
  private HttpServletRequest request;

  private XsrfTokenClaimsValidator validator;
  
  /**
   * Prepare object under test.
   */
  @Before
  public void prepareObjectUnderTest() {
    validator = new XsrfTokenClaimsValidator(() -> request);
  }
  
  /**
   * Tests {@link XsrfTokenClaimsValidator#validate(java.util.Map)}.
   */
  @Test
  public void testValidate() {
    // prepare
    Map<String, Object> claims = Maps.newHashMap();
    claims.put(Xsrf.TOKEN_KEY, "abc");
    when(request.getHeader(Xsrf.HEADER_KEY)).thenReturn("abc");
    
    // execute and assert
    assertTrue(validator.validate(claims));
  }
  
  /**
   * Tests {@link XsrfTokenClaimsValidator#validate(java.util.Map)} with wrong header.
   */
  @Test
  public void testValidateWithWrongHeader() {
    // prepare
    Map<String, Object> claims = Maps.newHashMap();
    claims.put(Xsrf.TOKEN_KEY, "abc");
    when(request.getHeader(Xsrf.HEADER_KEY)).thenReturn("123");
    
    // execute and assert
    assertFalse(validator.validate(claims));
  }
  
  /**
   * Tests {@link XsrfTokenClaimsValidator#validate(java.util.Map)} without header.
   */
  @Test
  public void testValidateWithoutHeader() {
    // prepare
    Map<String, Object> claims = Maps.newHashMap();
    claims.put(Xsrf.TOKEN_KEY, "abc");
    
    // execute and assert
    assertFalse(validator.validate(claims));
  }
  
  /**
   * Tests {@link XsrfTokenClaimsValidator#validate(java.util.Map)} without claims key.
   */
  @Test
  public void testValidateWithoutClaimsKey() {
    // prepare
    Map<String, Object> claims = Maps.newHashMap();
    
    // execute and assert
    assertTrue(validator.validate(claims));
  }
}