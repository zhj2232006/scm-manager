/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.debug;

import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;

/**
 * {@link PostReceiveRepositoryHookEvent} which stores receives data and passes it to the {@link DebugService}.
 * 
 * @author Sebastian Sdorra
 */
@EagerSingleton
public final class DebugHook
{
  /**
   * the logger for DebugHook
   */
  private static final Logger LOG = LoggerFactory.getLogger(DebugHook.class);
  
  private final DebugService debugService;

  /**
   * Constructs a new instance.
   * 
   * @param debugService debug service
   */
  @Inject
  public DebugHook(DebugService debugService)
  {
    this.debugService = debugService;
  }
  
  /**
   * Processes the received {@link PostReceiveRepositoryHookEvent} and transforms it to a {@link DebugHookData} and 
   * passes it to the {@link DebugService}.
   * 
   * @param event received event
   */
  @Subscribe(referenceType = ReferenceType.STRONG)
  public void processEvent(PostReceiveRepositoryHookEvent event){
    LOG.trace("store changeset ids from repository", event.getRepository().getId());
    
    debugService.put(
      event.getRepository().getId(), 
      new DebugHookData(Collections2.transform(
        event.getContext().getChangesetProvider().getChangesetList(), IDEXTRACTOR)
      ));
  }
  
  private static final Function<Changeset, String> IDEXTRACTOR = (Changeset changeset) -> changeset.getId();
}
