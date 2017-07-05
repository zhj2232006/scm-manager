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


package sonia.scm.repository.client.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class TagRequest
{

  /**
   * Method description
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

    final TagRequest other = (TagRequest) obj;

    return Objects.equal(revision, other.revision)
      && Objects.equal(name, other.name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(revision, name);
  }

  /**
   * Method description
   *
   */
  public void reset()
  {
    this.name = null;
    this.revision = null;
  }

  /**
   * Method description
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
                      .add("name", name)
                      .toString();
    //J+
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  String getRevision()
  {
    return revision;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String name;

  /** Field description */
  private String revision;
}
