/*
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

Sonia.repository.ChangesetViewerPanel = Ext.extend(Ext.Panel, {

  repository: null,
  start: 0,
  pageSize: 20,
  changesetStore: null,
  startLimit: null,
  
  // parameters for file history view
  inline: false,
  path: null,
  revision: null,
  
  changesetViewerTitleText: 'Commits {0}',
  
  initComponent: function(){
    if (! this.url){
      this.url = restUrl + 'repositories/' + this.repository.id  + '/changesets.json';
    }
    
    if ( ! this.startLimit ){
      this.startLimit = this.pageSize;
    }
    
    var params = {
      start: this.start,
      limit: this.startLimit
    };
    
    var baseParams = {};
    
    if (this.path){
      baseParams.path = this.path;
    }
    
    if (this.revision){
      baseParams.revision = this.revision;
    }

    this.changesetStore = new Sonia.rest.JsonStore({
      id: 'changesetStore',
      proxy: new Ext.data.HttpProxy({
        url: this.url,
        method: 'GET'
      }),
      fields: ['id', 'date', 'author', 'description', 'parents', 'modifications', 'tags', 'branches', 'properties'],
      root: 'changesets',
      idProperty: 'id',
      totalProperty: 'total',
      baseParams: baseParams,
      autoLoad: {
        params: params
      },
      autoDestroy: true,
      listeners: {
        load: {
          fn: this.updateHistory,
          scope: this
        }
      }
    });

    var config = {
      items: [{
        xtype: 'repositoryChangesetViewerGrid',
        repository: this.repository,
        store: this.changesetStore
      }]
    };
    
    if ( ! this.inline ){
      config.title = String.format(this.changesetViewerTitleText, this.repository.name);
      
      var type = Sonia.repository.getTypeByName( this.repository.type );
      if ( type && type.supportedCommands && type.supportedCommands.indexOf('BRANCHES') >= 0){
        config.tbar = this.createTopToolbar();
      }
      config.bbar = {
        xtype: 'paging',
        store: this.changesetStore,
        displayInfo: true,
        pageSize: this.pageSize,
        prependButtons: true
      };
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.ChangesetViewerPanel.superclass.initComponent.apply(this, arguments);
  },
  
  createTopToolbar: function(){
    return {
      xtype: 'toolbar',
      items: [
        this.repository.name,
        '->',
        'Branches:', ' ',{
        xtype: 'repositoryBranchComboBox',
        repositoryId: this.repository.id,
        listeners: {
          select: {
            fn: this.selectBranch,
            scope: this
          }
        }
      }]
    };
  },
  
  selectBranch: function(combo, rec){
    var branch = rec.get('name');
    if (debug){
      console.debug('select branch ' + branch);
    }
    this.changesetStore.baseParams.branch = branch;
    this.start = 0;
    this.changesetStore.load({
      params: {
        start: this.start,
        limit: this.startLimit
      }
    });
  },
  
  updateHistory: function(store, records, options){
    if ( ! this.inline && options && options.params ){
      this.start = options.params.start;
      this.pageSize = options.params.limit;
      var token = Sonia.History.createToken(
        'repositoryChangesetViewerPanel', 
        this.repository.id,
        this.start, 
        this.pageSize
      );
      Sonia.History.add(token);
    }
  },
  
  loadChangesets: function(start, limit){
    this.changesetStore.load({params: {
      start: start,
      limit: limit
    }});
  }

});

// register xtype
Ext.reg('repositoryChangesetViewerPanel', Sonia.repository.ChangesetViewerPanel);

// register history handler
Sonia.History.register('repositoryChangesetViewerPanel', {
  
  onActivate: function(panel){
    return Sonia.History.createToken(
      'repositoryChangesetViewerPanel', 
      panel.repository.id, 
      panel.start, 
      panel.pageSize
    );
  },
  
  onChange: function(repoId, start, limit){
    if (start){
      start = parseInt(start);
    }
    if (limit){
      limit = parseInt(limit);
    }
    var id = 'repositoryChangesetViewerPanel;' + repoId;
    Sonia.repository.get(repoId, function(repository){
      var panel = Ext.getCmp(id);
      if (! panel){
        panel = {
          id: id,
          xtype: 'repositoryChangesetViewerPanel',
          repository : repository,
          start: start,
          pageSize: limit,
          closable: true,
          autoScroll: true
        };
      } else {
        panel.loadChangesets(start, limit);
      }
      main.addTab(panel);
    });
  }
});