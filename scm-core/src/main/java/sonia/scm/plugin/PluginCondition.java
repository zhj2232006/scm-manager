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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import sonia.scm.PlatformType;
import sonia.scm.SCMContext;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;
import sonia.scm.version.Version;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "conditions")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginCondition implements Cloneable, Serializable
{

  /** Field description */
  private static final long serialVersionUID = 2406156062634968672L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public PluginCondition() {}

  /**
   * Constructs ...
   *
   *
   * @param minVersion
   * @param os
   * @param arch
   */
  public PluginCondition(String minVersion, List<String> os, String arch)
  {
    this.minVersion = minVersion;
    this.os = os;
    this.arch = arch;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   * @since 1.11
   */
  @Override
  public PluginCondition clone()
  {
    PluginCondition clone = new PluginCondition(minVersion, null, arch);

    if (Util.isNotEmpty(os))
    {
      clone.setOs(new ArrayList<String>(os));
    }

    return clone;
  }

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

    final PluginCondition other = (PluginCondition) obj;

    return Objects.equal(arch, other.arch)
      && Objects.equal(minVersion, other.minVersion)
      && Objects.equal(os, other.os);
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
    return Objects.hashCode(arch, minVersion, os);
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
    return Objects.toStringHelper(this)
                  .add("arch", arch)
                  .add("minVersion", minVersion)
                  .add("os", os)
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
  public String getArch()
  {
    return arch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getMinVersion()
  {
    return minVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getOs()
  {
    return os;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSupported()
  {
    return isSupported(SCMContext.getContext().getVersion(),
      SystemUtil.getOS(), SystemUtil.getArch());
  }

  /**
   * Method description
   *
   *
   * @param version
   * @param os
   * @param arch
   *
   * @return
   */
  public boolean isSupported(String version, String os, String arch)
  {
    boolean supported = true;

    if (Util.isNotEmpty(minVersion) && Util.isNotEmpty(version))
    {
      supported = (minVersion.equalsIgnoreCase(version)
        || Version.parse(version).isNewer(minVersion));
    }

    if (supported && Util.isNotEmpty(this.os) && Util.isNotEmpty(os))
    {
      supported = false;

      PlatformType platformType = PlatformType.createPlatformType(os);

      for (String osType : this.os)
      {
        supported = isOs(osType, platformType);

        if (supported)
        {
          break;
        }
      }
    }

    if (supported && Util.isNotEmpty(this.arch) && Util.isNotEmpty(arch))
    {
      supported = arch.equals(this.arch);
    }

    return supported;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param arch
   */
  public void setArch(String arch)
  {
    this.arch = arch;
  }

  /**
   * Method description
   *
   *
   * @param minVersion
   */
  public void setMinVersion(String minVersion)
  {
    this.minVersion = minVersion;
  }

  /**
   * Method description
   *
   *
   * @param os
   */
  public void setOs(List<String> os)
  {
    this.os = os;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param osType
   * @param type
   *
   * @return
   */
  private boolean isOs(String osType, PlatformType type)
  {
    osType = osType.toLowerCase(Locale.ENGLISH);

    //J-
    return ((osType.contains("win")) && (PlatformType.WINDOWS == type))
      || ((osType.contains("unix")) && type.isUnix())
      || ((osType.contains("posix")) && type.isPosix())
      || ((osType.contains("mac")) && (PlatformType.MAC == type))
      || ((osType.contains("linux")) && (PlatformType.LINUX == type))
      || ((osType.contains("solaris")) && (PlatformType.SOLARIS == type))
      || ((osType.contains("openbsd")) && (PlatformType.OPENBSD == type))
      || ((osType.contains("freebsd")) && (PlatformType.FREEBSD == type));
    //J+
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String arch;

  /** Field description */
  @XmlElement(name = "min-version")
  private String minVersion;

  /** Field description */
  @XmlElement(name = "name")
  @XmlElementWrapper(name = "os")
  private List<String> os;
}
