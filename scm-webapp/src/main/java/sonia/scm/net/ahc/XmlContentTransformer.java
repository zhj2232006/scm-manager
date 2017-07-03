/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteSource;
import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 * {@link ContentTransformer} for xml. The {@link XmlContentTransformer} uses 
 * jaxb to marshalling/unmarshalling.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
@Extension
public class XmlContentTransformer implements ContentTransformer
{

  /**
   * {@inheritDoc}
   */
  @Override
  public ByteSource marshall(Object object)
  {
    ByteSource source = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try
    {
      JAXB.marshal(object, baos);
      source = ByteSource.wrap(baos.toByteArray());
    }
    catch (DataBindingException ex)
    {
      throw new ContentTransformerException("could not marshall object", ex);
    }

    return source;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T unmarshall(Class<T> type, ByteSource content)
  {
    T object = null;
    InputStream stream = null;

    try
    {
      stream = content.openBufferedStream();
      object = JAXB.unmarshal(stream, type);
    }
    catch (IOException | DataBindingException ex)
    {
      throw new ContentTransformerException("could not unmarshall content", ex);
    } finally
    {
      IOUtil.close(stream);
    }

    return object;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true}, if the content type is compatible with 
   * application/xml.
   *
   *
   * @param type object type
   * @param contentType content type
   *
   * @return {@code true}, if the content type is compatible with 
   *   application/xml
   */
  @Override
  public boolean isResponsible(Class<?> type, String contentType)
  {
    return MediaType.valueOf(contentType).isCompatible(
      MediaType.APPLICATION_XML_TYPE);
  }
}
