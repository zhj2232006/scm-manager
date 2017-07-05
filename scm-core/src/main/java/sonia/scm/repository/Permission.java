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
import sonia.scm.security.PermissionObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * Permissions controls the access to {@link Repository}.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission implements PermissionObject, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -2915175031430884040L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link Permission}.
   * This constructor is used by JAXB.
   *
   */
  public Permission() {}

  /**
   * Constructs a new {@link Permission} with type = {@link PermissionType#READ}
   * for the specified user.
   *
   *
   * @param name name of the user
   */
  public Permission(String name)
  {
    this();
    this.name = name;
  }

  /**
   * Constructs a new {@link Permission} with the specified type for
   * the given user.
   *
   *
   * @param name name of the user
   * @param type type of the permission
   */
  public Permission(String name, PermissionType type)
  {
    this(name);
    this.type = type;
  }

  /**
   * Constructs a new {@link Permission} with the specified type for
   * the given user or group.
   *
   *
   * @param name name of the user or group
   * @param type type of the permission
   * @param groupPermission true if the permission is a permission for a group
   */
  public Permission(String name, PermissionType type, boolean groupPermission)
  {
    this(name, type);
    this.groupPermission = groupPermission;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns true if the {@link Permission} is the same as the obj argument.
   *
   *
   * @param obj the reference object with which to compare
   *
   * @return true if the {@link Permission} is the same as the obj argument
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

    final Permission other = (Permission) obj;

    return Objects.equal(name, other.name) 
      && Objects.equal(type, other.type)
      && Objects.equal(groupPermission, other.groupPermission);
  }

  /**
   * Returns the hash code value for the {@link Permission}.
   *
   *
   * @return the hash code value for the {@link Permission}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, type, groupPermission);
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
                      .add("name", name)
                      .add("type", type)
                      .add("groupPermission", groupPermission)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the name of the user or group.
   *
   *
   * @return name of the user or group
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Returns the {@link PermissionType} of the permission.
   *
   *
   * @return {@link PermissionType} of the permission
   */
  public PermissionType getType()
  {
    return type;
  }

  /**
   * Returns true if the permission is a permission which affects a group.
   *
   *
   * @return true if the permision is a group permission
   */
  @Override
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets true if the permission is a group permission.
   *
   *
   * @param groupPermission true if the permission is a group permission
   */
  public void setGroupPermission(boolean groupPermission)
  {
    this.groupPermission = groupPermission;
  }

  /**
   * The name of the user or group.
   *
   *
   * @param name name of the user or group
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Sets the type of the permission.
   *
   *
   * @param type type of the permission
   */
  public void setType(PermissionType type)
  {
    this.type = type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean groupPermission = false;

  /** Field description */
  private String name;

  /** Field description */
  private PermissionType type = PermissionType.READ;
}
