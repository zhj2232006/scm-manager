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
package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------
import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * File based implementation of {@link BlobStore}.
 *
 * @author Sebastian Sdorra
 */
public class FileBlobStore extends FileBasedStore<Blob> implements BlobStore {

  /**
   * the logger for FileBlobStore
   */
  private static final Logger LOG
    = LoggerFactory.getLogger(FileBlobStore.class);

  private static final String SUFFIX = ".blob";

  private final KeyGenerator keyGenerator;

  FileBlobStore(KeyGenerator keyGenerator, File directory) {
    super(directory, SUFFIX);
    this.keyGenerator = keyGenerator;
  }

  @Override
  public Blob create() {
    return create(keyGenerator.createKey());
  }

  @Override
  public Blob create(String id) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");
    LOG.debug("create new blob with id {}", id);

    File file = getFile(id);

    try {
      if (file.exists()) {
        throw new EntryAlreadyExistsStoreException(
          "blob with id ".concat(id).concat(" allready exists"));
      }
      else if (!file.createNewFile()) {
        throw new StoreException("could not create blob for id ".concat(id));
      }
    }
    catch (IOException ex) {
      throw new StoreException("could not create blob for id ".concat(id), ex);
    }

    return new FileBlob(id, file);
  }

  @Override
  public void remove(Blob blob) {
    Preconditions.checkNotNull(blob, "blob argument is required");
    remove(blob.getId());
  }

  @Override
  public List<Blob> getAll() {
    LOG.trace("get all items from data store");

    Builder<Blob> builder = ImmutableList.builder();

    for (File file : directory.listFiles()) {
      builder.add(read(file));
    }

    return builder.build();
  }

  @Override
  protected FileBlob read(File file) {
    FileBlob blob = null;

    if (file.exists()) {
      String id = getId(file);

      blob = new FileBlob(id, file);
    }

    return blob;
  }

}
