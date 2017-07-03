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



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableSet;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class EnvList
{

  /** Field description */
  private static final ImmutableSet<String> SENSITIVE =
    ImmutableSet.of("HTTP_AUTHORIZATION", "SCM_CHALLENGE", "SCM_CREDENTIALS");

  //~--- constructors ---------------------------------------------------------

  /**
   *    Constructs ...
   *
   */
  public EnvList()
  {
    envMap = new HashMap<>();
  }

  /**
   * Constructs ...
   *
   *
   * @param l
   */
  public EnvList(EnvList l)
  {
    envMap = new HashMap<>(l.envMap);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns environment as mutable map.
   *
   * @return environment as mutable map
   * @since 1.31
   */
  public Map<String, String> asMutableMap()
  {
    return new MapDelegate(envMap);
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean containsKey(String key)
  {
    return envMap.containsKey(key);
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
    String s = System.getProperty("line.separator");
    StringBuilder out = new StringBuilder("Environment:");

    Iterator<String> it = envMap.values().iterator();

    String v;

    while (it.hasNext())
    {
      v = converSensitive(it.next());
      out.append(s).append("  ").append(v);
    }

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Get representation suitable for passing to exec.
   *
   * @return
   */
  public String[] getEnvArray()
  {
    return envMap.values().toArray(new String[envMap.size()]);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Set a name/value pair, null values will be treated as an empty String
   *
   * @param name
   * @param value
   */
  public void set(String name, String value)
  {
    envMap.put(name, name.concat("=").concat(Util.nonNull(value)));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private String converSensitive(String v)
  {
    String result = v;

    for (String s : SENSITIVE)
    {
      if (v.startsWith(s))
      {
        result = s.concat("=(is set)");

        break;
      }
    }

    return result;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/05/15
   * @author         Enter your name here...
   */
  private static class MapDelegate extends ForwardingMap<String, String>
  {

    /**
     * Constructs ...
     *
     *
     * @param delegate
     */
    private MapDelegate(Map<String, String> delegate)
    {
      this.delegate = delegate;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param key
     * @param value
     *
     * @return
     */
    @Override
    public String put(String key, String value)
    {
      return super.put(key, key.concat("=").concat(Strings.nullToEmpty(value)));
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    protected Map<String, String> delegate()
    {
      return delegate;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Map<String, String> delegate;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, String> envMap;
}
