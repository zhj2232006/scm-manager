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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;

import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SubRepository;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnBrowseCommand extends AbstractSvnCommand
  implements BrowseCommand
{

  /**
   * the logger for SvnBrowseCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnBrowseCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  SvnBrowseCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  @SuppressWarnings("unchecked")
  public BrowserResult getBrowserResult(BrowseCommandRequest request)
    throws IOException, RepositoryException
  {
    String path = request.getPath();
    long revisionNumber = SvnUtil.getRevisionNumber(request.getRevision());

    if (logger.isDebugEnabled()) {
      logger.debug("browser repository {} in path {} at revision {}", repository.getName(), path, revisionNumber);
    }

    BrowserResult result = null;

    try
    {
      SVNRepository svnRepository = open();
      Collection<SVNDirEntry> entries =
        svnRepository.getDir(Util.nonNull(path), revisionNumber, null,
          (Collection) null);
      List<FileObject> children = Lists.newArrayList();
      String basePath = createBasePath(path);

      if (request.isRecursive())
      {
        browseRecursive(svnRepository, revisionNumber, request, children,
          entries, basePath);
      }
      else
      {
        for (SVNDirEntry entry : entries)
        {
          children.add(createFileObject(request, svnRepository, revisionNumber,
            entry, basePath));

        }
      }

      result = new BrowserResult();
      result.setRevision(String.valueOf(revisionNumber));
      result.setFiles(children);
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param svnRepository
   * @param revisionNumber
   * @param request
   * @param children
   * @param entries
   * @param basePath
   *
   * @throws SVNException
   */
  @SuppressWarnings("unchecked")
  private void browseRecursive(SVNRepository svnRepository,
    long revisionNumber, BrowseCommandRequest request,
    List<FileObject> children, Collection<SVNDirEntry> entries, String basePath)
    throws SVNException
  {
    for (SVNDirEntry entry : entries)
    {
      FileObject fo = createFileObject(request, svnRepository, revisionNumber,
                        entry, basePath);

      children.add(fo);

      if (fo.isDirectory())
      {
        Collection<SVNDirEntry> subEntries =
          svnRepository.getDir(Util.nonNull(fo.getPath()), revisionNumber,
            null, (Collection) null);

        browseRecursive(svnRepository, revisionNumber, request, children,
          subEntries, createBasePath(fo.getPath()));
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String createBasePath(String path)
  {
    String basePath = Util.EMPTY_STRING;

    if (Util.isNotEmpty(path))
    {
      basePath = path;

      if (!basePath.endsWith("/"))
      {
        basePath = basePath.concat("/");
      }
    }

    return basePath;
  }

  /**
   * Method description
   *
   *
   *
   *
   * @param request
   * @param repository
   * @param revision
   * @param entry
   * @param path
   *
   * @return
   */
  private FileObject createFileObject(BrowseCommandRequest request,
    SVNRepository repository, long revision, SVNDirEntry entry, String path)
  {
    FileObject fileObject = new FileObject();

    fileObject.setName(entry.getName());
    fileObject.setPath(path.concat(entry.getRelativePath()));
    fileObject.setDirectory(entry.getKind() == SVNNodeKind.DIR);

    if (!request.isDisableLastCommit())
    {
      if (entry.getDate() != null)
      {
        fileObject.setLastModified(entry.getDate().getTime());
      }

      fileObject.setDescription(entry.getCommitMessage());
    }

    fileObject.setLength(entry.getSize());

    if (!request.isDisableSubRepositoryDetection() && fileObject.isDirectory()
      && entry.hasProperties())
    {
      fetchExternalsProperty(repository, revision, entry, fileObject);
    }

    return fileObject;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revision
   * @param entry
   * @param fileObject
   */
  private void fetchExternalsProperty(SVNRepository repository, long revision,
    SVNDirEntry entry, FileObject fileObject)
  {
    try
    {
      SVNProperties properties = new SVNProperties();

      repository.getFile(entry.getRelativePath(), revision, properties, null);

      String externals = properties.getStringValue(SVNProperty.EXTERNALS);

      if (Util.isNotEmpty(externals))
      {
        SubRepository subRepository = new SubRepository(externals);

        fileObject.setSubRepository(subRepository);
      }
    }
    catch (SVNException ex)
    {
      logger.error("could not fetch file properties");
    }
  }
}
