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


package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
public class DeactivatedUserITCase
{

  /**
   * Method description
   *
   */
  @Before
  public void createDeactivatedUser()
  {
    Client client = createAdminClient();

    try
    {
      WebResource wr = createResource(client, "users");

      slarti = UserTestData.createSlarti();
      slarti.setPassword("slart123");
      slarti.setActive(false);

      ClientResponse response =
        wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, slarti);

      assertNotNull(response);
      assertEquals(201, response.getStatus());
      response.close();
    }
    finally
    {
      client.destroy();
    }
  }

  /**
   * Method description
   *
   */
  @After
  public void destroyDeactivatedUser()
  {
    Client client = createAdminClient();

    try
    {
      WebResource wr = createResource(client,
                                      "users/".concat(slarti.getName()));
      ClientResponse response =
        wr.type(MediaType.APPLICATION_XML).delete(ClientResponse.class);

      assertNotNull(response);
      assertEquals(204, response.getStatus());
      response.close();
    }
    finally
    {
      client.destroy();
    }
  }

  /**
   * Method description
   *
   */
  @Test
  public void testFailedAuthentication()
  {
    Client client = createClient();
    ClientResponse response = authenticate(client, slarti.getName(),
                                "slart123");
    assertNotNull(response);
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private User slarti;
}
