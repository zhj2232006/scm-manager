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
var repositoryTypeStore = new Ext.data.JsonStore({
  id: 1,
  fields: [ 'displayName', 'name', 'supportedCommands' ]
});

function loadRepositoryTypes(state){
  repositoryTypeStore.loadData( state.repositoryTypes );
}

// register login callback
loginCallbacks.push( loadRepositoryTypes );

// register namespace
Ext.ns('Sonia.repository');

Sonia.repository.openListeners = [];

Sonia.repository.typeIcons = [];

// functions

Sonia.repository.getTypeIcon = function(type){
  return Sonia.repository.typeIcons[type];
};

Sonia.repository.createContentUrl = function(repository, path, revision){
  var contentUrl = restUrl + 'repositories/' + repository.id  + '/';
  contentUrl += 'content?path=' + path;
  if ( revision ){
    contentUrl += "&revision=" + revision;
  }
  return contentUrl;
};

Sonia.repository.createContentId = function(repository, path, revision){
  var id = repository.id + '-b-'  + path;
  if ( revision ){
    id += '-r-' + revision;
  }
  return id;
};

Sonia.repository.isOwner = function(repository){
  return admin || repository.permissions;
};

Sonia.repository.setEditPanel = function(panels, activeTab){
  var editPanel = Ext.getCmp('repositoryEditPanel');
  editPanel.removeAll();
  Ext.each(panels, function(panel){
    editPanel.add(panel);
  });
  
  if (!activeTab){
    activeTab = 0;
  }
  
  editPanel.setActiveTab(activeTab);
  editPanel.doLayout();
};

Sonia.repository.createUrl = function(type, name){
  return Sonia.util.getBaseUrl() + '/' + type + '/' + name;
};

Sonia.repository.createUrlFromObject = function(repository){
  return Sonia.repository.createUrl(repository.type, repository.name);
};

Sonia.repository.getTypeByName = function(name){
  var type = null;
  repositoryTypeStore.queryBy(function(rec){
    if ( rec.get('name') === name ){
      type = rec.data;
    }
  });
  return type;
};

Sonia.repository.getPermissionType = function(repository){
  var values = [];
  values['READ'] = 0;
  values['WRITE'] = 10;
  values['OWNER'] = 20;
  
  var type = 'READ';
  
  if (state.assignedPermissions){
    Ext.each(state.assignedPermissions, function(p){
      var parts = p.split(':');
      if ( parts[0] === 'repository' && (parts[1] === '*' || parts[1] === repository.id)){
        if ( values[parts[2]] > values[type] ){
          type = parts[2];
        }
      }
    });
  }
  return type;
};

/**
 * default panel
 */
Sonia.repository.DefaultPanel = {
  region: 'south',
  title: 'Repository Form',
  padding: 5,
  xtype: 'panel',
  bodyCssClass: 'x-panel-mc',
  html: 'Add or select an Repository'
};

// load object from store or from web service

Sonia.repository.get = function(id, callback){
  function execCallback(item){
    if (Ext.isFunction(callback)){
      callback(item);
    } else {
      callback.call(callback.scope, item);
    }
  }
  
  var repository = null;
  
  var grid = Ext.getCmp('repositoryGrid');
  if ( grid ){
    var store = grid.getStore();
    if (store){
      var rec = null;
      var index = id.indexOf('/');
      if ( index > 0 ){
        var type = id.substring(0, index);
        var name = id.substring(index);
        index = store.findBy(function(rec){
          return rec.get('name') === name && rec.get('type') === type;
        });
        if ( index >= 0 ){
          rec = store.getAt(index);
        }
      } else {
        rec = store.getById(id);
      }
      if (rec){
        repository = rec.data;
      }
    }
  }
  
  if (repository){
    execCallback(repository);
  } else {
    Ext.Ajax.request({
      url: restUrl + 'repositories/' + id + '.json',
      method: 'GET',
      scope: this,
      success: function(response){
        execCallback(Ext.decode(response.responseText));
      },
      failure: function(result){
        main.handleRestFailure(
          result
        );
      }
    }); 
  }
};

/** open file */
Sonia.repository.openFile = function(repository, path, revision){
  if ( debug ){
    console.debug( 'open file: ' + path );
  }

  var id = Sonia.repository.createContentId(
    repository, 
    path, 
    revision
  );

  main.addTab({
    id: id,
    path: path,
    revision: revision,
    repository: repository,
    xtype: 'contentPanel',
    closable: true,
    autoScroll: true
  });
};