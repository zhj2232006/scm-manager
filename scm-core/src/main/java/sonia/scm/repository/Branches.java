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
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Represents all branches of a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@XmlRootElement(name="branches")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Branches implements Iterable<Branch>
{

  /**
   * Constructs a new instance of branches.
   * This constructor should only be called from JAXB.
   *
   */
  public Branches() {}

  /**
   * Constructs a new instance of branches.
   *
   *
   * @param branches list of branches.
   */
  public Branches(Branch... branches)
  {
    this.branches = Lists.newArrayList(branches);
  }

  /**
   * Constructs a new instance of branches.
   *
   *
   * @param branches list of branches.
   */
  public Branches(List<Branch> branches)
  {
    this.branches = branches;
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

    final Branches other = (Branches) obj;

    return Objects.equal(branches, other.branches);
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
    return Objects.hashCode(branches);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<Branch> iterator()
  {
    return getBranches().iterator();
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
                      .add("branches", branches)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all branches of a repository.
   *
   *
   * @return all branches
   */
  public List<Branch> getBranches()
  {
    if (branches == null)
    {
      branches = Lists.newArrayList();
    }

    return branches;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets all branches.
   *
   *
   * @param branches branches
   */
  public void setBranches(List<Branch> branches)
  {
    this.branches = branches;
  }

  //~--- fields ---------------------------------------------------------------

  /** branches */
  @XmlElement(name="branch")
  private List<Branch> branches;
}
