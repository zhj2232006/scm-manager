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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.BlameLine;
import sonia.scm.repository.FileObject;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
public class FileObjectWrapper
{

  /**
   * Constructs ...
   *
   *
   *
   * @param repositoryBrowser
   * @param revision
   * @param file
   */
  public FileObjectWrapper(ClientRepositoryBrowser repositoryBrowser,
                           String revision, FileObject file)
  {
    this.repositoryBrowser = repositoryBrowser;
    this.revision = revision;
    this.file = file;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<BlameLine> getBlameLines()
  {
    return repositoryBrowser.getBlameLines(revision, getPath());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<FileObjectWrapper> getChildren()
  {
    List<FileObjectWrapper> children = null;

    if (isDirectory())
    {
      children = repositoryBrowser.getFiles(revision, getPath());
    }

    return children;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public InputStream getContent() throws IOException
  {
    return repositoryBrowser.getContent(revision, getPath());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return file.getDescription();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getLastModified()
  {
    return file.getLastModified();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getLength()
  {
    return file.getLength();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return file.getName();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPath()
  {
    return file.getPath();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDirectory()
  {
    return file.isDirectory();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private FileObject file;

  /** Field description */
  private ClientRepositoryBrowser repositoryBrowser;

  /** Field description */
  private String revision;
}
