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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.Util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "overrides")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassOverrides implements Iterable<ClassOverride>
{

  /** Field description */
  public static final String OVERRIDE_PATH = "META-INF/scm/override.xml";

  /**
   * the logger for ClassOverrides
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ClassOverrides.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @return
   *
   */
  public static ClassOverrides findOverrides(ClassLoader classLoader)
  {
    final ClassOverrides overrides = new ClassOverrides();

    try
    {
      final Enumeration<URL> overridesEnm =
        classLoader.getResources(OVERRIDE_PATH);
      final JAXBContext context = JAXBContext.newInstance(ClassOverrides.class);

      ClassLoaders.executeInContext(classLoader, () -> {
        while (overridesEnm.hasMoreElements())
        {
          URL overrideUrl = overridesEnm.nextElement();

          if (logger.isInfoEnabled())
          {
            logger.info("load override from {}",
              overrideUrl.toExternalForm());
          }

          try
          {
            ClassOverrides co =
              (ClassOverrides) context.createUnmarshaller().unmarshal(
                overrideUrl);

            overrides.append(co);
          }
          catch (JAXBException ex)
          {
            logger.error(
              "could not load ".concat(overrideUrl.toExternalForm()), ex);
          }
        }
      });

    }
    catch (IOException ex)
    {
      logger.error("could not load overrides", ex);
    }
    catch (JAXBException ex)
    {
      logger.error("could not create jaxb context for ClassOverrides", ex);
    }

    return overrides;
  }

  /**
   * Method description
   *
   *
   * @param overrides
   */
  public void append(ClassOverrides overrides)
  {
    AssertUtil.assertIsNotNull(overrides);

    for (ClassOverride co : overrides)
    {
      if (co.isValid())
      {
        getOverrides().add(co);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not append ClassOverride, because it is not valid");
      }
    }

    getModuleClasses().addAll(overrides.getModuleClasses());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<ClassOverride> iterator()
  {
    return getOverrides().iterator();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<Class<? extends Module>> getModuleClasses()
  {
    if (moduleClasses == null)
    {
      moduleClasses = Lists.newArrayList();
    }

    return moduleClasses;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<? extends Module> getModules()
  {
    List<? extends Module> modules;

    if (Util.isNotEmpty(moduleClasses))
    {
      final Function<Class<? extends Module>, Module> classModuleFunction = moduleClass -> {
        Module module = null;

        try {
          module = moduleClass.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
          logger.error(
            "could not create module instance of ".concat(
              moduleClass.getName()), ex);
        }

        return module;
      };
      modules = Lists.transform(moduleClasses,
                                classModuleFunction);
    }
    else
    {
      modules = Collections.EMPTY_LIST;
    }

    return modules;
  }

  /**
   * Method description
   *
   *
   * @param clazz
   * @param <T>
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T> Class<T> getOverride(Class<T> clazz)
  {
    Class<T> implementation = null;

    for (ClassOverride co : getOverrides())
    {
      if (co.getBind().equals(clazz))
      {
        implementation = (Class<T>) co.getTo();
      }
    }

    return implementation;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<ClassOverride> getOverrides()
  {
    if (overrides == null)
    {
      overrides = Lists.newArrayList();
    }

    return overrides;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param moduleClasses
   */
  public void setModuleClasses(List<Class<? extends Module>> moduleClasses)
  {
    this.moduleClasses = moduleClasses;
  }

  /**
   * Method description
   *
   *
   * @param overrides
   */
  public void setOverrides(List<ClassOverride> overrides)
  {
    this.overrides = overrides;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "module")
  private List<Class<? extends Module>> moduleClasses;

  /** Field description */
  @XmlElement(name = "override")
  private List<ClassOverride> overrides;
}
