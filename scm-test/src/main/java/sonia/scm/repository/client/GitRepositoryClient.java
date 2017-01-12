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



package sonia.scm.repository.client;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryClient extends AbstractRepositoryClient
{

  /**
   * Constructs ...
   *
   *
   * @param localRepository
   * @param remoteRepository
   * @param username
   * @param password
   *
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  GitRepositoryClient(File localRepository, String remoteRepository,
    String username, String password)
    throws URISyntaxException, IOException
  {
    super(localRepository, remoteRepository);
    uri = new URIish(remoteRepository);

    if ((username != null) && (password != null))
    {
      uri.setUser(username);
      uri.setPass(password);
      credentialsProvider = new UsernamePasswordCredentialsProvider(username,
        password);
    }

    this.repository = new FileRepository(new File(localRepository,
      Constants.DOT_GIT));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   * @param others
   *
   * @throws RepositoryClientException
   */
  @Override
  public void add(String file, String... others)
    throws RepositoryClientException
  {
    AddCommand cmd = new Git(repository).add();

    cmd.addFilepattern(file);

    if (others != null)
    {
      for (String f : others)
      {
        cmd.addFilepattern(f);
      }
    }

    callCommand(cmd);
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void checkout() throws RepositoryClientException
  {
    try
    {
      final FetchResult r = runFetch();

      if (r != null)
      {
        for (TrackingRefUpdate ru : r.getTrackingRefUpdates())
        {
          switch (ru.getResult())
          {
            case IO_FAILURE :
            case LOCK_FAILURE :
            case REJECTED :
            case REJECTED_CURRENT_BRANCH :
              throw new RepositoryClientException("could not checkout, status "
                + ru.getResult().name());
          }
        }

        Ref branch = guessHEAD(r);

        doCheckout(branch);
      }
    }
    catch (RepositoryClientException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param message
   *
   * @throws RepositoryClientException
   */
  @Override
  public void commit(String message) throws RepositoryClientException
  {
    try
    {
      CommitCommand cmd = new Git(repository).commit();

      cmd.setMessage(message);
      callCommand(cmd);

      List<Transport> transports = Transport.openAll(repository, "origin",
                                     Transport.Operation.PUSH);

      for (Transport transport : transports)
      {
        List<RefSpec> refSpecs = new ArrayList<RefSpec>();
        RefSpec rs = new RefSpec("+refs/heads/master:refs/heads/master");

        refSpecs.add(rs);

        if (credentialsProvider != null)
        {
          transport.setCredentialsProvider(credentialsProvider);
        }

        Collection<RemoteRefUpdate> toPush =
          transport.findRemoteRefUpdatesFor(refSpecs);

        try
        {
          PushResult result = transport.push(new TextProgressMonitor(), toPush);
          Collection<RemoteRefUpdate> refUpdates = result.getRemoteUpdates();

          if (Util.isEmpty(refUpdates))
          {
            throw new RepositoryClientException("no ref updated");
          }

          for (RemoteRefUpdate refUpdate : refUpdates)
          {
            switch (refUpdate.getStatus())
            {
              case OK :
              case UP_TO_DATE :
              case NON_EXISTING :
                break;

              case NOT_ATTEMPTED :
              case AWAITING_REPORT :
              case REJECTED_NODELETE :
              case REJECTED_NONFASTFORWARD :
              case REJECTED_REMOTE_CHANGED :
              case REJECTED_OTHER_REASON :
                throw new RepositoryClientException(
                  "could not update ref ".concat(refUpdate.getSrcRef()).concat(
                    " status ".concat(refUpdate.getStatus().toString())));
            }
          }
        }
        finally
        {
          transport.close();
        }
      }
    }
    catch (RepositoryClientException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws RepositoryClientException
   */
  @Override
  public void init() throws RepositoryClientException
  {
    try
    {
      repository.create(false);
      addRemote("origin", new URIish(remoteRepository), "master", null, true,
        null);
    }
    catch (Exception ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param remoteName
   * @param uri
   * @param branchName
   * @param tagName
   * @param allSelected
   * @param selectedBranches
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  private void addRemote(final String remoteName, final URIish uri,
    final String branchName, final String tagName, final boolean allSelected,
    final Collection<Ref> selectedBranches)
    throws URISyntaxException, IOException
  {

//  add remote configuration
    final RemoteConfig rc = new RemoteConfig(repository.getConfig(),
                              remoteName);

    rc.addURI(uri);

    final String dst = Constants.R_REMOTES + rc.getName();
    RefSpec wcrs = new RefSpec();

    wcrs = wcrs.setForceUpdate(true);
    wcrs = wcrs.setSourceDestination(Constants.R_HEADS + "*", dst + "/*");

    if (allSelected)
    {
      rc.addFetchRefSpec(wcrs);
    }
    else
    {
      for (final Ref ref : selectedBranches)
      {
        if (wcrs.matchSource(ref))
        {
          rc.addFetchRefSpec(wcrs.expandFromSource(ref));
        }
      }
    }

    rc.update(repository.getConfig());
    repository.getConfig().save();

//  setup the default remote branch for branchName
    repository.getConfig().setString("branch", branchName, "remote",
      remoteName);

    if (branchName != null)
    {
      repository.getConfig().setString("branch", branchName, "merge",
        Constants.R_HEADS + branchName);
    }

    if (tagName != null)
    {
      repository.getConfig().setString("branch", branchName, "merge",
        Constants.R_TAGS + branchName);
    }

    repository.getConfig().save();
  }

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @throws RepositoryClientException
   */
  private void callCommand(GitCommand cmd) throws RepositoryClientException
  {
    try
    {
      cmd.call();
    }
    catch (Exception ex)
    {
      throw new RepositoryClientException(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param branch
   *
   * @throws IOException
   */
  private void doCheckout(final Ref branch) throws IOException
  {
    if (!Constants.HEAD.equals(branch.getName()))
    {
      RefUpdate u = repository.updateRef(Constants.HEAD);

      u.disableRefLog();
      u.link(branch.getName());
    }

    RevCommit commit = parseCommit(branch);
    RefUpdate u = repository.updateRef(Constants.HEAD);

    u.setNewObjectId(commit);
    u.forceUpdate();

    DirCache dc = repository.lockDirCache();
    DirCacheCheckout co = new DirCacheCheckout(repository, dc,
                            commit.getTree());

    co.checkout();
  }

  /**
   * Method description
   *
   *
   * @param result
   *
   * @return
   */
  private Ref guessHEAD(final FetchResult result)
  {
    final Ref idHEAD = result.getAdvertisedRef(Constants.HEAD);
    final List<Ref> availableRefs = new ArrayList<Ref>();
    Ref head = null;

    for (final Ref r : result.getAdvertisedRefs())
    {
      final String n = r.getName();

      if (!n.startsWith(Constants.R_HEADS))
      {
        continue;
      }

      availableRefs.add(r);

      if ((idHEAD == null) || (head != null))
      {
        continue;
      }

      if (r.getObjectId().equals(idHEAD.getObjectId()))
      {
        head = r;
      }
    }

    Collections.sort(availableRefs, RefComparator.INSTANCE);

    if ((idHEAD != null) && (head == null))
    {
      head = idHEAD;
    }

    return head;
  }

  /**
   * Method description
   *
   *
   * @param branch
   *
   * @return
   *
   * @throws IOException
   * @throws IncorrectObjectTypeException
   * @throws MissingObjectException
   */
  private RevCommit parseCommit(Ref branch)
    throws MissingObjectException, IncorrectObjectTypeException, IOException
  {
    try (RevWalk rw = new RevWalk(repository))
    {
      return rw.parseCommit(branch.getObjectId());
    }
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws NotSupportedException
   * @throws RepositoryClientException
   * @throws TransportException
   * @throws URISyntaxException
   */
  private FetchResult runFetch()
    throws NotSupportedException, URISyntaxException, TransportException,
    RepositoryClientException
  {
    FetchResult r = null;

    try
    {
      Transport tn = Transport.open(repository, "origin");

      if (credentialsProvider != null)
      {
        tn.setCredentialsProvider(credentialsProvider);
      }

      List<RefSpec> refSpecs = new ArrayList<RefSpec>();
      RefSpec rs = new RefSpec("+refs/heads/master:refs/heads/master");

      try
      {
        r = tn.fetch(new TextProgressMonitor(), refSpecs);
      }
      finally
      {
        tn.close();
      }
    }
    catch (NoRemoteRepositoryException ex)
    {

      // empty repository, call init
      init();
    }

    return r;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CredentialsProvider credentialsProvider;

  /** Field description */
  private RemoteConfig remoteConfig;

  /** Field description */
  private FileRepository repository;

  /** Field description */
  private URIish uri;
}
