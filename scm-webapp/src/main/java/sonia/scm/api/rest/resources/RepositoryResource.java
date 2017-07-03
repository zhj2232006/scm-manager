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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import org.apache.shiro.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.Branches;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.HealthChecker;
import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryIsNotArchivedException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.Tags;
import sonia.scm.repository.api.BlameCommandBuilder;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.apache.shiro.authz.AuthorizationException;

/**
 * Repository related RESTful Web Service Endpoint.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
@Path("repositories")
public class RepositoryResource extends AbstractManagerResource<Repository, RepositoryException>
{

  /** Field description */
  public static final String PATH_PART = "repositories";

  /** the logger for RepositoryResource */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param repositoryManager
   * @param servicefactory
   * @param healthChecker
   */
  @Inject
  public RepositoryResource(ScmConfiguration configuration,
    RepositoryManager repositoryManager,
    RepositoryServiceFactory servicefactory, HealthChecker healthChecker)
  {
    super(repositoryManager);
    this.configuration = configuration;
    this.repositoryManager = repositoryManager;
    this.servicefactory = servicefactory;
    this.healthChecker = healthChecker;
    setDisableCache(false);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new repository.<strong>Note:</strong> This method requires admin privileges.
   *
   * @param uriInfo current uri informations
   * @param repository the repository to be created
   *
   * @return empty response with location header to the new repository
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 201, condition = "success", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to the new created repository")
    }),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response create(@Context UriInfo uriInfo, Repository repository)
  {
    return super.create(uriInfo, repository);
  }

  /**
   * Deletes a repository. <strong>Note:</strong> This method requires owner privileges.
   *
   * @param id the id of the repository to delete.
   *
   * @return
   */
  @DELETE
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no owner privileges"),
    @ResponseCode(code = 404, condition = "could not find repository"),
    @ResponseCode(
      code = 412, 
      condition = "precondition failed, the repository is not archived, this error occurs only with enabled repository archive"
    ),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Override
  public Response delete(@PathParam("id") String id)
  {
    Response response;
    Repository repository = manager.get(id);

    if (repository != null)
    {
      preDelete(repository);

      try
      {
        manager.delete(repository);
        response = Response.noContent().build();
      }
      catch (RepositoryIsNotArchivedException ex)
      {
        logger.warn("non archived repository could not be deleted", ex);
        response = Response.status(Response.Status.PRECONDITION_FAILED).build();
      }
      catch (AuthorizationException ex)
      {
        logger.warn("delete not allowed", ex);
        response = Response.status(Response.Status.FORBIDDEN).build();
      }
      catch (RepositoryException | IOException ex)
      {
        logger.error("error during create", ex);
        response = createErrorResonse(ex);
      }
    }
    else
    {
      logger.warn("could not find repository {}", id);
      response = Response.status(Status.NOT_FOUND).build();
    }

    return response;
  }

  /**
   * Re run repository health checks.
   *
   * @param id id of the repository
   *
   * @return
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 200, condition = "re run success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no owner privileges"),
    @ResponseCode(code = 404, condition = "could not find repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Path("{id}/healthcheck")
  public Response runHealthChecks(@PathParam("id") String id)
  {
    Response response;

    try
    {
      healthChecker.check(id);
      // TODO should return 204 instead of 200
      response = Response.ok().build();
    }
    catch (RepositoryNotFoundException ex)
    {
      logger.warn("could not find repository ".concat(id), ex);
      response = Response.status(Status.NOT_FOUND).build();
    }
    catch (RepositoryException | IOException ex)
    {
      logger.error("error occured during health check", ex);
      response = Response.serverError().build();
    }

    return response;
  }

  /**
   * Modifies the given repository. <strong>Note:</strong> This method requires owner privileges.
   *
   * @param uriInfo current uri informations
   * @param id id of the repository to be modified
   * @param repository repository object to modify
   *
   * @return
   */
  @PUT
  @Path("{id}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update successful"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no owner privileges"),
    @ResponseCode(code = 404, condition = "could not find repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Override
  public Response update(@Context UriInfo uriInfo, @PathParam("id") String id, Repository repository)
  {
    return super.update(uriInfo, id, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the {@link Repository} with the specified id.
   *
   * @param request the current request
   * @param id the id/name of the user
   *
   * @return the {@link Repository} with the specified id
   */
  @GET
  @Path("{id}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified id available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Repository.class)
  @Override
  public Response get(@Context Request request, @PathParam("id") String id)
  {
    return super.get(request, id);
  }

  /**
   * Returns all repositories.
   *
   * @param request the current request
   * @param start the start value for paging
   * @param limit the limit value for paging
   * @param sortby sort parameter
   * @param desc sort direction desc or aesc
   *
   * @return all repositories
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Repository[].class)
  @Override
  public Response getAll(@Context Request request, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
  @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
    @DefaultValue("false")
  @QueryParam("desc") boolean desc)
  {
    return super.getAll(request, start, limit, sortby, desc);
  }

  /**
   * Returns a annotate/blame view for the given path.
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path the path of the file
   *
   * @return a annotate/blame view for the given path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/blame")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the blame feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the path could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(BlameResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getBlame(@PathParam("id") String id,
    @QueryParam("revision") String revision, @QueryParam("path") String path)
    throws RepositoryException, IOException
  {
    Response response = null;
    RepositoryService service = null;

    try
    {
      AssertUtil.assertIsNotNull(path);
      service = servicefactory.create(id);

      BlameCommandBuilder builder = service.getBlameCommand();

      if (!Strings.isNullOrEmpty(revision))
      {
        builder.setRevision(revision);
      }

      BlameResult blamePagingResult = builder.getBlameResult(path);

      if (blamePagingResult != null)
      {
        response = Response.ok(blamePagingResult).build();
      }
      else
      {
        response = Response.ok().build();
      }
    }
    catch (IllegalStateException ex)
    {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    finally
    {
      IOUtil.close(service);
    }

    return response;
  }

  /**
   * Returns all {@link Branches} of a repository.
   *
   * @param id the id of the repository
   *
   * @return all {@link Branches} of a repository
   *
   * @throws IOException
   * @throws RepositoryException
   *
   * @since 1.18
   */
  @GET
  @Path("{id}/branches")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the branch feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Branches.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getBranches(@PathParam("id") String id)
    throws RepositoryException, IOException
  {
    Response response = null;
    RepositoryService service = null;

    try
    {
      service = servicefactory.create(id);

      Branches branches = service.getBranchesCommand().getBranches();

      if (branches != null)
      {
        response = Response.ok(branches).build();
      }
      else
      {
        response = Response.status(Status.NOT_FOUND).build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    finally
    {
      IOUtil.close(service);
    }

    return response;
  }

  /**
   * Returns a list of folders and files for the given folder.
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path the path of the folder
   * @param disableLastCommit true disables fetch of last commit message
   * @param disableSubRepositoryDetection true disables sub repository detection
   * @param recursive true to enable recursive browsing
   *
   * @return a list of folders and files for the given folder
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/browse")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the browse feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the path could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(BrowserResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  //J-
  public Response getBrowserResult(
    @PathParam("id") String id,
    @QueryParam("revision") String revision, 
    @QueryParam("path") String path,
    @QueryParam("disableLastCommit") @DefaultValue("false") boolean disableLastCommit,
    @QueryParam("disableSubRepositoryDetection") @DefaultValue("false") boolean disableSubRepositoryDetection,
    @QueryParam("recursive") @DefaultValue("false") boolean recursive)
    throws RepositoryException, IOException
  //J+
  {
    Response response = null;
    RepositoryService service = null;

    try
    {
      service = servicefactory.create(id);

      BrowseCommandBuilder builder = service.getBrowseCommand();

      if (!Strings.isNullOrEmpty(revision))
      {
        builder.setRevision(revision);
      }

      if (!Strings.isNullOrEmpty(path))
      {
        builder.setPath(path);
      }

      //J-
      builder.setDisableLastCommit(disableLastCommit)
             .setDisableSubRepositoryDetection(disableSubRepositoryDetection)
             .setRecursive(recursive);
      //J+

      BrowserResult result = builder.getBrowserResult();

      if (result != null)
      {
        response = Response.ok(result).build();
      }
      else
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    finally
    {
      IOUtil.close(service);
    }

    return response;
  }

  /**
   * Returns the {@link Repository} with the specified type and name.
   *
   * @param type the type of the repository
   * @param name the name of the repository
   *
   * @return the {@link Repository} with the specified type and name
   */
  @GET
  @Path("{type: [a-z]+}/{name: .*}")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 404, condition = "not found, no repository with the specified type and name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Repository.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getByTypeAndName(@PathParam("type") String type,
    @PathParam("name") String name)
  {
    Response response;
    Repository repository = repositoryManager.get(type, name);

    if (repository != null)
    {
      prepareForReturn(repository);
      response = Response.ok(repository).build();
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
  }

  /**
   * Returns the {@link Changeset} from the given repository
   * with the specified revision.
   *
   * @param id the id of the repository
   * @param revision the revision of the changeset
   *
   * @return a {@link Changeset} from the given repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/changeset/{revision}")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the changeset feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the revision could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Changeset.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getChangeset(@PathParam("id") String id,
    @PathParam("revision") String revision)
    throws IOException, RepositoryException
  {
    Response response = null;

    if (Util.isNotEmpty(id) && Util.isNotEmpty(revision))
    {
      RepositoryService service = null;

      try
      {
        service = servicefactory.create(id);

        Changeset changeset = service.getLogCommand().getChangeset(revision);

        if (changeset != null)
        {
          response = Response.ok(changeset).build();
        }
        else
        {
          response = Response.status(Status.NOT_FOUND).build();
        }
      }
      catch (RepositoryNotFoundException ex)
      {
        response = Response.status(Response.Status.NOT_FOUND).build();
      }
      catch (CommandNotSupportedException ex)
      {
        response = Response.status(Response.Status.BAD_REQUEST).build();
      }
      finally
      {
        IOUtil.close(service);
      }
    }
    else
    {
      logger.warn("id or revision is empty");
      response = Response.status(Status.BAD_REQUEST).build();
    }

    return response;
  }

  /**
   * Returns a list of {@link Changeset} for the given repository.
   *
   * @param id the id of the repository
   * @param path path of a file
   * @param revision the revision of the file specified by the path parameter
   * @param branch name of the branch
   * @param start the start value for paging
   * @param limit the limit value for paging
   *
   * @return a list of {@link Changeset} for the given repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/changesets")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the changeset feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the path could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(ChangesetPagingResult.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  //J-
  public Response getChangesets(
    @PathParam("id") String id,
    @QueryParam("path") String path, 
    @QueryParam("revision") String revision,
    @QueryParam("branch") String branch,
    @DefaultValue("0") @QueryParam("start") int start, 
    @DefaultValue("20") @QueryParam("limit") int limit
  ) throws RepositoryException, IOException
  //J+
  {
    Response response = null;
    RepositoryService service = null;

    try
    {
      ChangesetPagingResult changesets;

      service = servicefactory.create(id);

      LogCommandBuilder builder = service.getLogCommand();

      if (!Strings.isNullOrEmpty(path))
      {
        builder.setPath(path);
      }

      if (!Strings.isNullOrEmpty(revision))
      {
        builder.setStartChangeset(revision);
      }

      if (!Strings.isNullOrEmpty(branch))
      {
        builder.setBranch(branch);
      }

      changesets =
        builder.setPagingStart(start).setPagingLimit(limit).getChangesets();

      if (changesets != null)
      {
        response = Response.ok(changesets).build();
      }
      else
      {
        response = Response.ok().build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    finally
    {
      IOUtil.close(service);
    }

    return response;
  }

  /**
   * Returns the content of a file.
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path path to the file
   *
   * @return the content of a file
   */
  @GET
  @Path("{id}/content")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the content feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the path could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(StreamingOutput.class)
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
  public Response getContent(@PathParam("id") String id,
    @QueryParam("revision") String revision, @QueryParam("path") String path)
  {
    Response response;
    StreamingOutput output;
    RepositoryService service;

    try
    {
      service = servicefactory.create(id);

      CatCommandBuilder builder = service.getCatCommand();

      if (!Strings.isNullOrEmpty(revision))
      {
        builder.setRevision(revision);
      }

      output = new BrowserStreamingOutput(service, builder, path);

      /**
       * protection for crlf injection
       * see https://bitbucket.org/sdorra/scm-manager/issue/320/crlf-injection-vulnerability-in-diff-api
       */
      path = HttpUtil.removeCRLFInjectionChars(path);

      String contentDispositionName = getContentDispositionNameFromPath(path);

      response = Response.ok(output).header("Content-Disposition",
        contentDispositionName).build();
    }
    catch (RepositoryNotFoundException ex)
    {
      logger.warn("could not find repository browser for respository {}", id);
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    catch (Exception ex)
    {
      logger.error("could not retrive content", ex);
      response = createErrorResonse(ex);
    }

    return response;
  }

  /**
   * Returns the modifications of a {@link Changeset}.
   *
   * @param id the id of the repository
   * @param revision the revision of the file
   * @param path path to the file
   * @param format
   *
   * @return the modifications of a {@link Changeset}
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @GET
  @Path("{id}/diff")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the diff feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository or the path could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(DiffStreamingOutput.class)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getDiff(@PathParam("id") String id,
    @QueryParam("revision") String revision, @QueryParam("path") String path,
    @QueryParam("format") DiffFormat format)
    throws RepositoryException, IOException
  {
    AssertUtil.assertIsNotEmpty(id);
    AssertUtil.assertIsNotEmpty(revision);

    /**
     * check for a crlf injection attack
     * see https://bitbucket.org/sdorra/scm-manager/issue/320/crlf-injection-vulnerability-in-diff-api
     */
    HttpUtil.checkForCRLFInjection(revision);

    RepositoryService service;
    Response response;

    try
    {
      service = servicefactory.create(id);

      DiffCommandBuilder builder = service.getDiffCommand();

      if (!Strings.isNullOrEmpty(revision))
      {
        builder.setRevision(revision);
      }

      if (!Strings.isNullOrEmpty(path))
      {
        builder.setPath(path);
      }

      if (format != null)
      {
        builder.setFormat(format);
      }

      String name = service.getRepository().getName().concat("-").concat(
                      revision).concat(".diff");
      String contentDispositionName = getContentDispositionName(name);

      response = Response.ok(new DiffStreamingOutput(service,
        builder)).header("Content-Disposition", contentDispositionName).build();
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    catch (Exception ex)
    {
      logger.error("could not create diff", ex);
      response = createErrorResonse(ex);
    }

    return response;
  }

  /**
   * Returns all {@link Tags} of a repository.
   *
   * @param id the id of the repository
   *
   * @return all {@link Tags} of a repository
   *
   * @throws IOException
   * @throws RepositoryException
   *
   * @since 1.18
   */
  @GET
  @Path("{id}/tags")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, the tag feature is not supported by this type of repositories."),
    @ResponseCode(code = 404, condition = "not found, the repository could not be found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(Tags.class)
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response getTags(@PathParam("id") String id)
    throws RepositoryException, IOException
  {
    Response response = null;
    RepositoryService service = null;

    try
    {
      service = servicefactory.create(id);

      Tags tags = service.getTagsCommand().getTags();

      if (tags != null)
      {
        response = Response.ok(tags).build();
      }
      else
      {
        response = Response.status(Status.NOT_FOUND).build();
      }
    }
    catch (RepositoryNotFoundException ex)
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }
    catch (CommandNotSupportedException ex)
    {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    }
    finally
    {
      IOUtil.close(service);
    }

    return response;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param items
   *
   * @return
   */
  @Override
  protected GenericEntity<Collection<Repository>> createGenericEntity(
    Collection<Repository> items)
  {
    return new GenericEntity<Collection<Repository>>(items) {}
    ;
  }

  /**
   * Method description
   *
   *
   *
   * @param repositories
   * @return
   */
  @Override
  protected Collection<Repository> prepareForReturn(
    Collection<Repository> repositories)
  {
    for (Repository repository : repositories)
    {
      prepareForReturn(repository);
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  protected Repository prepareForReturn(Repository repository)
  {
    if (SecurityUtils.getSubject().isPermitted(
      "repository:modify:".concat(repository.getId())))
    {
      if (repository.getPermissions() == null)
      {
        repository.setPermissions(new ArrayList<>());
      }
    }
    else
    {
      logger.trace("remove properties and permissions from repository, "
        + "because the user is not privileged");

      repository.setProperties(null);
      repository.setPermissions(null);
      repository.setHealthCheckFailures(null);
    }

    return repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  protected String getId(Repository item)
  {
    return item.getId();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getPathPart()
  {
    return PATH_PART;
  }

  /**
   * Method description
   *
   *
   *
   * @param name
   *
   * @return
   */
  private String getContentDispositionName(String name)
  {
    return HttpUtil.createContentDispositionAttachmentHeader(name);
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String getContentDispositionNameFromPath(String path)
  {
    String name = path;
    int index = path.lastIndexOf('/');

    if (index >= 0)
    {
      name = path.substring(index + 1);
    }

    return getContentDispositionName(name);
  }
  
  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final HealthChecker healthChecker;

  /** Field description */
  private final RepositoryManager repositoryManager;

  /** Field description */
  private final RepositoryServiceFactory servicefactory;
}
