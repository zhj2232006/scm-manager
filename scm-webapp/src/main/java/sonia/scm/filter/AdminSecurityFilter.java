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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.apache.shiro.subject.Subject;

import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Role;

/**
 * Security filter which allow only administrators to access the underlying
 * resources.
 * 
 * @author Sebastian Sdorra
 */
@WebElement(
  value = Filters.PATTERN_CONFIG,
  morePatterns = { 
    Filters.PATTERN_USERS, 
    Filters.PATTERN_GROUPS,
    Filters.PATTERN_PLUGINS 
  }
)
@Priority(Filters.PRIORITY_AUTHORIZATION + 1)
public class AdminSecurityFilter extends SecurityFilter
{

  /**
   * Constructs a new instance.
   *
   * @param configuration scm-manager main configuration
   */
  @Inject
  public AdminSecurityFilter(ScmConfiguration configuration)
  {
    super(configuration);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if the subject has the admin role.
   *
   * @param subject subject
   * 
   * @return {@code true} if the subject has the admin role
   */
  @Override
  protected boolean hasPermission(Subject subject)
  {
    return subject.hasRole(Role.ADMIN);
  }
}
