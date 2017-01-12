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



package sonia.scm.net;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;
import java.util.Map;

/**
 * Simple client for http operations.
 *
 * @author Sebastian Sdorra
 * 
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.net.HttpRequest
 * @apiviz.uses sonia.scm.net.HttpResponse
 * 
 * @deprecated use {@link sonia.scm.net.ahc.AdvancedHttpClient} instead.
 */
@Deprecated
public interface HttpClient
{

  /**
   * Send a post request to the given url.
   *
   *
   * @param url url for post request
   *
   * @return the response of the http request
   *
   * @throws IOException
   */
  public HttpResponse post(String url) throws IOException;

  /**
   * Sends a post request with the parameter specified in the 
   * {@link HttpRequest} object.
   *
   *
   * @param request request object
   *
   * @return the response of the http request
   * @since 1.9
   *
   * @throws IOException
   */
  public HttpResponse post(HttpRequest request) throws IOException;

  /**
   * Send a post request to the given url with the specified post parameters.
   *
   *
   * @param url url for post request
   * @param parameters parameters for the post request
   *
   * @return the response of the http request
   *
   * @throws IOException
   */
  public HttpResponse post(String url, Map<String, List<String>> parameters)
          throws IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Send a get request to the given url.
   *
   *
   * @param url url for get request
   *
   * @return the response of the http request
   *
   * @throws IOException
   */
  public HttpResponse get(String url) throws IOException;

  /**
   * Sends a get request with the parameter specified in the 
   * {@link HttpRequest} object.
   *
   *
   * @param request request object
   *
   * @return the response of the http request
   * @since 1.9
   *
   * @throws IOException
   */
  public HttpResponse get(HttpRequest request) throws IOException;

  /**
   * Send a get request to the given url with the specified post parameters.
   *
   *
   * @param url url for get request
   * @param parameters parameters for the get request
   *
   * @return the response of the http request
   *
   * @throws IOException
   */
  public HttpResponse get(String url, Map<String, List<String>> parameters)
          throws IOException;
}
