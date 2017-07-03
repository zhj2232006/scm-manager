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



package sonia.scm.search;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.TransformFilter;
import sonia.scm.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class SearchUtil
{

  /**
   * the logger for SearchUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SearchUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private SearchUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param value
   * @param other
   *
   * @return
   */
  public static boolean matchesAll(SearchRequest request, String value,
    String... other)
  {
    boolean result = false;
    String query = createStringQuery(request);

    if (value.matches(query))
    {
      result = true;

      if (Util.isNotEmpty(other))
      {
        for (String o : other)
        {
          if ((o == null) ||!o.matches(query))
          {
            result = false;

            break;
          }
        }
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param value
   * @param other
   *
   * @return
   */
  public static boolean matchesOne(SearchRequest request, String value,
    String... other)
  {
    boolean result = false;
    String query = createStringQuery(request);

    if (!value.matches(query))
    {
      if (Util.isNotEmpty(other))
      {
        for (String o : other)
        {
          if ((o != null) && o.matches(query))
          {
            result = true;

            break;
          }
        }
      }
    }
    else
    {
      result = true;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param searchRequest
   * @param collection
   * @param filter
   * @param <T>
   *
   * @return
   */
  public static <T> Collection<T> search(SearchRequest searchRequest,
    Collection<T> collection, TransformFilter<T> filter)
  {
    List<T> items = new ArrayList<T>();
    int index = 0;
    int counter = 0;

    for (final T aCollection : collection) {
      T item = filter.accept(aCollection);

      if (item != null) {
        index++;

        if (searchRequest.getStartWith() <= index) {
          items.add(item);
          counter++;

          if (searchRequest.getMaxResults() <= counter) {
            break;
          }
        }
      }
    }

    return items;
  }

  /**
   * Method description
   *
   *
   * @param pattern
   * @param c
   */
  private static void appendChar(StringBuilder pattern, char c)
  {
    switch (c)
    {
      case '*' :
        pattern.append(".*");

        break;

      case '?' :
        pattern.append(".");

        break;

      case '(' :
        pattern.append("\\(");

        break;

      case ')' :
        pattern.append("\\)");

        break;

      case '{' :
        pattern.append("\\{");

        break;

      case '}' :
        pattern.append("\\}");

        break;

      case '[' :
        pattern.append("\\[");

        break;

      case ']' :
        pattern.append("\\]");

        break;

      case '|' :
        pattern.append("\\|");

        break;

      case '.' :
        pattern.append("\\.");

        break;

      case '\\' :
        pattern.append("\\\\");

        break;

      default :
        pattern.append(c);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private static String createStringQuery(SearchRequest request)
  {
    String query = request.getQuery().trim();

    StringBuilder pattern = new StringBuilder();

    if (request.isIgnoreCase())
    {
      pattern.append("(?i)");
      query = query.toLowerCase(Locale.ENGLISH);
    }

    pattern.append(".*");

    for (char c : query.toCharArray())
    {
      appendChar(pattern, c);
    }

    pattern.append(".*");

    logger.trace("converted query \"{}\" to regex pattern \"{}\"",
      request.getQuery(), pattern);

    return pattern.toString();
  }
}
