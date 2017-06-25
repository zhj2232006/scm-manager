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
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;

import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * RESTful Web Service Resource to manage users.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
@Path("users")
public class UserResource extends AbstractManagerResource<User, UserException>
{

  /** Field description */
  public static final String DUMMY_PASSWORT = "__dummypassword__";

  /** Field description */
  public static final String PATH_PART = "users";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param userManager
   * @param passwordService
   */
  @Inject
  public UserResource(UserManager userManager, PasswordService passwordService)
  {
    super(userManager);
    this.passwordService = passwordService;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new user. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param uriInfo current uri informations
   * @param user the user to be created
   *
   * @return
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to the created group")
    }),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response create(@Context UriInfo uriInfo, User user)
  {
    return super.create(uriInfo, user);
  }

  /**
   * Deletes a user. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param name the name of the user to delete.
   *
   * @return
   */
  @DELETE
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Override
  public Response delete(@PathParam("id") String name)
  {
    return super.delete(name);
  }

  /**
   * Modifies the given user. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param uriInfo current uri informations
   * @param name name of the user to be modified
   * @param user user object to modify
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response update(@Context UriInfo uriInfo,
    @PathParam("id") String name, User user)
  {
    return super.update(uriInfo, name, user);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a user. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request the current request
   * @param id the id/name of the user
   *
   * @return the {@link User} with the specified id
   */
  @GET
  @Path("{id}")
  @TypeHint(User.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 404, condition = "not found, no group with the specified id/name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response get(@Context Request request, @PathParam("id") String id)
  {
    Response response = null;

    if (SecurityUtils.getSubject().hasRole(Role.ADMIN))
    {
      response = super.get(request, id);
    }
    else
    {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
  }

  /**
   * Returns all users. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request the current request
   * @param start the start value for paging
   * @param limit the limit value for paging
   * @param sortby sort parameter
   * @param desc sort direction desc or aesc
   *
   * @return
   */
  @GET
  @TypeHint(User[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response getAll(@Context Request request, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
  @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
    @DefaultValue("false")
  @QueryParam("desc") boolean desc)
  {
    return super.getAll(request, start, limit, sortby, desc);
  }

  //~--- methods --------------------------------------------------------------

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(
    Collection<User> items)
  {
    return new GenericEntity<Collection<User>>(items) {}
    ;
  }

  @Override
  protected void preCreate(User user)
  {
    encryptPassword(user);
  }
  
  @Override
  protected void preUpate(User user)
  {
    if (DUMMY_PASSWORT.equals(user.getPassword()))
    {
      User o = manager.get(user.getName());

      AssertUtil.assertIsNotNull(o);
      user.setPassword(o.getPassword());
    }
    else
    {
      encryptPassword(user);
    }
  }

  @Override
  protected Collection<User> prepareForReturn(Collection<User> users)
  {
    if (Util.isNotEmpty(users))
    {
      for (User u : users)
      {
        u.setPassword(DUMMY_PASSWORT);
      }
    }

    return users;
  }

  @Override
  protected User prepareForReturn(User user)
  {
    user.setPassword(DUMMY_PASSWORT);

    return user;
  }

  @Override
  protected String getId(User user)
  {
    return user.getName();
  }

  @Override
  protected String getPathPart()
  {
    return PATH_PART;
  }

  private void encryptPassword(User user)
  {
    String password = user.getPassword();

    if (Util.isNotEmpty(password))
    {
      user.setPassword(passwordService.encryptPassword(password));
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PasswordService passwordService;
}
