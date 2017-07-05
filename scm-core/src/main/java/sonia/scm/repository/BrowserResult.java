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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "browser-result")
public class BrowserResult implements Iterable<FileObject>, Serializable
{

  /** Field description */
  private static final long serialVersionUID = 2818662048045182761L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BrowserResult() {}

  /**
   * Constructs ...
   *
   *
   * @param revision
   * @param tag
   * @param branch
   * @param files
   */
  public BrowserResult(String revision, String tag, String branch,
                       List<FileObject> files)
  {
    this.revision = revision;
    this.tag = tag;
    this.branch = branch;
    this.files = files;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final BrowserResult other = (BrowserResult) obj;

    return Objects.equal(revision, other.revision)
           && Objects.equal(tag, other.tag)
           && Objects.equal(branch, other.branch)
           && Objects.equal(files, other.files);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(revision, tag, branch, files);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<FileObject> iterator()
  {
    Iterator<FileObject> it = null;

    if (files != null)
    {
      it = files.iterator();
    }

    return it;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("revision", revision)
                      .add("tag", tag)
                      .add("branch", branch)
                      .add("files", files)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBranch()
  {
    return branch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<FileObject> getFiles()
  {
    return files;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRevision()
  {
    return revision;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getTag()
  {
    return tag;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param branch
   */
  public void setBranch(String branch)
  {
    this.branch = branch;
  }

  /**
   * Method description
   *
   *
   * @param files
   */
  public void setFiles(List<FileObject> files)
  {
    this.files = files;
  }

  /**
   * Method description
   *
   *
   * @param revision
   */
  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  /**
   * Method description
   *
   *
   * @param tag
   */
  public void setTag(String tag)
  {
    this.tag = tag;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String branch;

  /** Field description */
  @XmlElement(name = "file")
  @XmlElementWrapper(name = "files")
  private List<FileObject> files;

  /** Field description */
  private String revision;

  /** Field description */
  private String tag;
}
