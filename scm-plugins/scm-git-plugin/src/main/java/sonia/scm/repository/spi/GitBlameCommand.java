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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitBlameCommand extends AbstractGitCommand implements BlameCommand
{

  /**
   * the logger for GitBlameCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitBlameCommand.class);

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
  public GitBlameCommand(GitContext context, Repository repository)
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
  public BlameResult getBlameResult(BlameCommandRequest request)
          throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("try to create blame for {}", request);
    }

    Preconditions.checkArgument(!Strings.isNullOrEmpty(request.getPath()),
                                "path is empty or null");

    BlameResult result = null;

    try
    {
      org.eclipse.jgit.lib.Repository gr = open();
      org.eclipse.jgit.api.BlameCommand blame = new Git(gr).blame();

      blame.setFilePath(request.getPath());

      ObjectId revId = getCommitOrDefault(gr, request.getRevision());

      blame.setStartCommit(revId);

      org.eclipse.jgit.blame.BlameResult gitBlameResult = blame.call();

      if (gitBlameResult == null)
      {
        throw new RepositoryException(
            "could not create blame result for path ".concat(
              request.getPath()));
      }

      List<BlameLine> blameLines = new ArrayList<>();
      int total = gitBlameResult.getResultContents().size();
      int i = 0;

      for (; i < total; i++)
      {
        RevCommit commit = gitBlameResult.getSourceCommit(i);

        if (commit != null)
        {
          PersonIdent author = gitBlameResult.getSourceAuthor(i);
          BlameLine blameLine = new BlameLine();

          blameLine.setLineNumber(i + 1);
          blameLine.setAuthor(new Person(author.getName(),
                                         author.getEmailAddress()));
          blameLine.setDescription(commit.getShortMessage());

          long when = GitUtil.getCommitTime(commit);

          blameLine.setWhen(when);

          String rev = commit.getId().getName();

          blameLine.setRevision(rev);

          String content = gitBlameResult.getResultContents().getString(i);

          blameLine.setCode(content);
          blameLines.add(blameLine);
        }
      }

      result = new BlameResult(i, blameLines);
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryException("could not create blame view", ex);
    }

    return result;
  }
}
