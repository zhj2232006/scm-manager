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


package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;

import sonia.scm.security.Role;
import sonia.scm.security.SecuritySystem;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Resource for managing system security permissions.
 * 
 * @author Sebastian Sdorra
 */
@Path("security/permission")
public class SecuritySystemResource
{

  /**
   * Constructs ...
   *
   *
   * @param system
   */
  @Inject
  public SecuritySystemResource(SecuritySystem system)
  {
    this.system = system;

    // only administrators can use this resource
    SecurityUtils.getSubject().checkRole(Role.ADMIN);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns group permission sub resource.
   *
   * @param group name of group
   *
   * @return sub resource
   */
  @Path("group/{group}")
  public GroupPermissionResource getGroupSubResource(@PathParam("group") String group)
  {
    return new GroupPermissionResource(system, group);
  }

  /**
   * Returns user permission sub resource.
   *
   *
   * @param user name of user
   *
   * @return sub resource
   */
  @Path("user/{user}")
  public UserPermissionResource getUserSubResource(@PathParam("user") String user)
  {
    return new UserPermissionResource(system, user);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SecuritySystem system;
}
