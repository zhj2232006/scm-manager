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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.util.IOUtil;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class CacheTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract CacheManager createCacheManager();

  /**
   * Method description
   *
   */
  @After
  public void after()
  {
    IOUtil.close(cm);
  }

  /**
   * Method description
   *
   */
  @Before
  public void before()
  {
    cm = createCacheManager();
    cache = cm.getCache("test");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testClear()
  {
    cache.put("test", "test123");
    cache.put("test-1", "test123");
    cache.clear();
    assertNull(cache.get("test"));
    assertNull(cache.get("test-1"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testContains()
  {
    cache.put("test", "test123");
    cache.put("test-1", "test123");
    assertTrue(cache.contains("test"));
    assertTrue(cache.contains("test-1"));
    assertFalse(cache.contains("test-2"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testOverride()
  {
    cache.put("test", "test123");

    String previous = cache.put("test", "test456");

    assertEquals("test123", previous);
    assertEquals("test456", cache.get("test"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testPutAndGet()
  {
    cache.put("test", "test123");
    assertEquals("test123", cache.get("test"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testRemove()
  {
    cache.put("test", "test123");
    assertEquals("test123", cache.get("test"));

    String previous = cache.remove("test");

    assertEquals("test123", previous);
    assertNull(cache.get("test"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testRemoveAll()
  {
    cache.put("test-1", "test123");
    cache.put("test-2", "test456");
    cache.put("a-1", "test123");
    cache.put("a-2", "test123");

    Iterable<String> previous = cache.removeAll(item -> item != null && item.startsWith("test"));

    assertThat(previous, containsInAnyOrder("test123", "test456"));
    assertNull(cache.get("test-1"));
    assertNull(cache.get("test-2"));
    assertNotNull(cache.get("a-1"));
    assertNotNull(cache.get("a-2"));
  }
  
  @Test
  public void testCacheStatistics(){
    CacheStatistics stats = cache.getStatistics();
    // skip test if implementation does not support stats
    Assume.assumeTrue( stats != null );
    assertEquals("test", stats.getName());
    assertEquals(0l, stats.getHitCount());
    assertEquals(0l, stats.getMissCount());
    cache.put("test-1", "test123");
    cache.put("test-2", "test456");
    cache.get("test-1");
    cache.get("test-1");
    cache.get("test-1");
    cache.get("test-3");
    // check that stats have not changed
    assertEquals(0l, stats.getHitCount());
    assertEquals(0l, stats.getMissCount());
    stats = cache.getStatistics();
    assertEquals(3l, stats.getHitCount());
    assertEquals(1l, stats.getMissCount());
    assertEquals(0.75d, stats.getHitRate(), 0.0d);
    assertEquals(0.25d, stats.getMissRate(), 0.0d);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSize()
  {
    assertEquals(0, cache.size());
    cache.put("test", "test123");
    assertEquals(1, cache.size());
    cache.put("test-1", "test123");
    assertEquals(2, cache.size());
    cache.remove("test");
    assertEquals(1, cache.size());
    cache.clear();
    assertEquals(0, cache.size());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<String, String> cache;

  /** Field description */
  private CacheManager cm;
}
