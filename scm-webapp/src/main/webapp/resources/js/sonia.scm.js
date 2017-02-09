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
Ext.ns("Sonia.scm");

Sonia.scm.Main = Ext.extend(Ext.util.Observable, {

  tabRepositoriesText: 'Repositories',
  navImportRepositoriesText: 'Import Repositories',
  navChangePasswordText: 'Change Password',
  sectionMainText: 'Main',
  sectionSecurityText: 'Security',
  navRepositoriesText: 'Repositories',
  sectionConfigText: 'Config',
  navGeneralConfigText: 'General',
  tabGeneralConfigText: 'SCM Config',

  navRepositoryTypesText: 'Repository Types',
  tabRepositoryTypesText: 'Repository Config',
  navPluginsText: 'Plugins',
  tabPluginsText: 'Plugins',
  navUsersText: 'Users',
  tabUsersText: 'Users',
  navGroupsText: 'Groups',
  tabGroupsText: 'Groups',
  
  sectionLoginText: 'Login',
  navLoginText: 'Login',

  sectionLogoutText: 'Log out',
  navLogoutText: 'Log out',

  logoutFailedText: 'Logout Failed!',
  
  errorTitle: 'Error',
  errorMessage: 'Unknown error occurred.',
  
  errorSessionExpiredTitle: 'Session expired',
  errorSessionExpiredMessage: 'Your session is expired. Please relogin.',
  
  errorNoPermissionsTitle: 'Not permitted',
  errorNoPermissionsMessage: 'You have not enough permissions to execute this action.',
  
  errorNotFoundTitle: 'Not found',
  errorNotFoundMessage: 'The resource could not be found.',
  
  loggedInTextTemplate: 'logged in as <a id="scm-userinfo-tip">{state.user.name}</a> - ',
  userInfoMailText: 'Mail',
  userInfoGroupsText: 'Groups',

  mainTabPanel: null,
  
  infoPanels: [],
  settingsForm: [],
  scripts: [],
  stylesheets: [],

  constructor : function(config) {
    this.addEvents('login', 'logout', 'init');
    this.mainTabPanel = Ext.getCmp('mainTabPanel');
    this.addListener('login', this.postLogin, this);
    Sonia.scm.Main.superclass.constructor.call(this, config);
  },
  
  init: function(){
    this.fireEvent('init', this);
  },
  
  registerInfoPanel: function(type, panel){
    this.infoPanels[type] = panel;
  },
  
  registerSettingsForm: function(type, form){
    this.settingsForm[type] = form;
  },
  
  getSettingsForm: function(type){
    var rp = null;
    var panel = this.settingsForm[type];
    if ( ! panel ){
      rp = {
        xtype: 'repositorySettingsForm'
      };
    } else {
      rp = Sonia.util.clone( panel );
    }
    return rp;
  },
  
  getInfoPanel: function(type){
    var rp = null;
    var panel = this.infoPanels[type];
    if ( ! panel ){
      rp = {
        xtype: 'repositoryInfoPanel'
      };
    } else {
      rp = Sonia.util.clone( panel );
    }
    return rp;
  },

  postLogin: function(){
    this.createMainMenu();
    this.createHomePanel();
  },

  createHomePanel: function(){
    if ( debug ){
      console.debug('create home panel');
    }
    this.mainTabPanel.add({
      id: 'repositories',
      xtype: 'repositoryPanel',
      title: this.tabRepositoriesText,
      closeable: false,
      autoScroll: true
    });
    this.mainTabPanel.setActiveTab('repositories');
  },
  
  addRepositoriesTabPanel: function(){
    this.addTabPanel("repositories", "repositoryPanel", this.tabRepositoriesText);
  },
  
  addScmConfigTabPanel: function(){
    if (admin){
      this.addTabPanel("scmConfig", "scmConfig", this.navGeneralConfigText);
    }
  },
  
  addRepositoryConfigTabPanel: function(){
    if (admin){
      this.addTabPanel('repositoryConfig', 'repositoryConfig', this.tabRepositoryTypesText);
    }
  },
  
  addPluginTabPanel: function(){
    if (admin){
      this.addTabPanel('plugins', 'pluginGrid', this.navPluginsText);
    }
  },
  
  addUsersTabPanel: function(){
    if (admin){
      this.addTabPanel('users', 'userPanel', this.navUsersText);
    }
  },
  
  addGroupsTabPanel: function(){
    if (admin){
      this.addTabPanel('groups', 'groupPanel', this.tabGroupsText);
    }
  },

  createMainMenu: function(){
    if ( debug ){
      console.debug('create main menu');
    }
    var panel = Ext.getCmp('navigationPanel');
    
    var repositoryLinks = [{
      label: this.navRepositoriesText,
      fn: this.addRepositoriesTabPanel,
      scope: this
    }];
    
    if ( admin ){
      repositoryLinks.push({
        label: this.navImportRepositoriesText,
        fn: function(){
          new Sonia.repository.ImportWindow().show();
        }
      });
    }
    
    panel.addSection({
      id: 'navMain',
      title: this.sectionMainText,
      links: repositoryLinks
    });

    var securitySection = null;

    if ( state.user.type === state.defaultUserType && state.user.name !== 'anonymous' ){
      securitySection = {
        id: 'securityConfig',
        title: this.sectionSecurityText,
        links: [{
          label: this.navChangePasswordText,
          fn: function(){
            new Sonia.action.ChangePasswordWindow().show();
          }
        }]
      };
    }

    if ( admin ){

      panel.addSections([{
        id: 'navConfig',
        title: this.sectionConfigText,
        links: [{
          label: this.navGeneralConfigText,
          fn: this.addScmConfigTabPanel,
          scope: this
        },{
          label: this.navRepositoryTypesText,
          fn: this.addRepositoryConfigTabPanel,
          scope: this
        },{
          label: this.navPluginsText,
          fn: this.addPluginTabPanel,
          scope: this
        }]
      }]);

      if ( ! securitySection ){
        securitySection = {
          id: 'securityConfig',
          title: this.sectionSecurityText,
          links: []
        };
      }

      securitySection.links.push({
        label: this.navUsersText,
        fn: this.addUsersTabPanel,
        scope: this
      },{
        label: this.navGroupsText,
        fn: this.addGroupsTabPanel,
        scope: this
      });
    }

    if ( securitySection ){
      panel.addSection( securitySection );
    }

    if ( state.user.name === 'anonymous' ){
      panel.addSection({
        id: 'navLogin',
        title: this.sectionLoginText,
        links: [{
          label: this.sectionLoginText,
          fn: this.login,
          scope: this
        }]
      });
    } else {
      panel.addSection({
        id: 'navLogout',
        title: this.sectionLogoutText,
        links: [{
          label: this.navLogoutText,
          fn: this.logout,
          scope: this
        }]
      });
    }

    //fix hidden logout button
    panel.doLayout();
  },

  addTabPanel: function(id, xtype, title){
    if (!xtype){
      xtype = id;
    }
    var panel = {
      id: id,
      xtype: xtype,
      closable: true,
      autoScroll: true
    };
    if (title){
      panel.title = title;
    }
    this.addTab(panel);
  },

  addTab: function(panel){
    var tab = this.mainTabPanel.findById(panel.id);
    if ( !tab ){
      this.mainTabPanel.add(panel);
    }
    this.mainTabPanel.setActiveTab(panel.id);
  },

  loadState: function(s){
    if ( debug ){
      console.debug( s );
    }
    state = s;
    admin = s.user.admin;

    // call login callback functions
    this.fireEvent("login", state);
  },

  clearState: function(){
    // clear state
    state = null;
    // clear repository store
    repositoryTypeStore.removeAll();
    // remove all tabs
    this.mainTabPanel.removeAll();
    // remove navigation items
    Ext.getCmp('navigationPanel').removeAll();
  },

  checkLogin: function(){
    Ext.Ajax.request({
      url: restUrl + 'auth/state.json',
      method: 'GET',
      scope: this,
      success: function(response){
        if ( debug ){
          console.debug('login success');
        }
        var s = Ext.decode(response.responseText);
        this.loadState(s);
      },
      failure: function(){
        if ( debug ){
          console.debug('login failed');
        }
        var loginWin = new Sonia.login.Window();
        loginWin.show();
      }
    });
  },

  login: function(){
    this.clearState();
    var loginWin = new Sonia.login.Window();
    loginWin.show();
  },

  logout: function(){
    Ext.Ajax.request({
      url: restUrl + 'auth/logout.json',
      method: 'GET',
      scope: this,
      success: function(response){
        if ( debug ){
          console.debug('logout success');
        }
        this.clearState();
        // call logout callback functions
        this.fireEvent( "logout", state );

        var s = null;
        var text = response.responseText;
        if ( text && text.length > 0 ){
          s = Ext.decode( text );
        }
        if ( s && s.success ){
          this.loadState(s);
        } else {
          // show login window
          var loginWin = new Sonia.login.Window();
          loginWin.show();
        }
      },
      failure: function(){
        if ( debug ){
          console.debug('logout failed');
        }
        Ext.Msg.alert(this.logoutFailedText);
      }
    });
  },

  addListeners: function(event, callbacks){
    Ext.each(callbacks, function(callback){
      if ( Ext.isFunction(callback) ){
        this.addListener(event, callback);
      } else if (Ext.isObject(callback)) {
        this.addListener(event, callback.fn, callback.scope);
      } else if (debug){
        console.debug( "callback is not a function or object. " + callback );
      }
    }, this);
  },
  
  handleRestFailure: function(response, title, message){
    this.handleFailure(response.status, title, message, response.responseText);
  },
  
  handleFailure: function(status, title, message, serverException){
    if (debug){
      console.debug( 'handle failure for status code: ' + status );
    }
    // TODO handle already exists exceptions specific
    if ( status === 401 ){
      Ext.Msg.show({
        title: this.errorSessionExpiredTitle,
        msg: this.errorSessionExpiredMessage,
        buttons: Ext.Msg.OKCANCEL,
        fn: function(btn){
          if ( btn === 'ok' ){
            this.login();
          }
        },
        scope: this
      });
    } else if ( status === 403 ){
      Ext.Msg.show({
        title: this.errorNoPermissionsTitle,
        msg: this.errorNoPermissionsMessage,
        buttons: Ext.Msg.OKCANCEL
      });
    } else if ( status === 404 ){
      Ext.Msg.show({
        title: this.errorNotFoundTitle,
        msg: this.errorNotFoundMessage,
        buttons: Ext.Msg.OKCANCEL
      });      
    } else {
      if ( ! title ){
        title = this.errorTitle;
      }
      if ( ! message ){
        message = this.errorMessage;
      }

      var text = null;
      if (serverException){
        try {
          if ( Ext.isString(serverException) ){
            serverException = Ext.decode(serverException);
          }
          text = serverException.stacktrace;
          if ( debug ){
            console.debug( text );
          }
        } catch (e){
          if ( debug ){
            console.debug(e);
          }
        }
      }
      
      message = String.format(message, status);
      
      if ( ! text ){

        Ext.MessageBox.show({
          title: title,
          msg: message,
          buttons: Ext.MessageBox.OK,
          icon: Ext.MessageBox.ERROR
        });
      
      } else {
        new Sonia.action.ExceptionWindow({
          title: title,
          message: message,
          stacktrace: text
        }).show();
      }
    }
  },
  
  loadScript: function(url, callback, scope){
    var doCallback = function(){
      if (debug){
        console.debug('call callback for script ' + url);
      }
      if ( scope ){
        callback.call(scope);
      } else {
        callback();
      }
    };
    if ( this.scripts.indexOf(url) < 0 ){
      var js = document.createElement('script');
      js.type = "text/javascript";
      js.language = 'javascript';
      js.src = url;
      
      if ( Ext.isIE ){
        js.onreadystatechange = function (){
          if (this.readyState === 'loaded' ||
              this.readyState === 'complete'){
              doCallback();
          }
        };
      } else {
        js.onload = doCallback;
        js.onerror = doCallback;
      }
      
      if (debug){
        console.debug('load script ' + url);
      }
      
      document.body.appendChild(js);
      // var head = document.getElementsByTagName('head')[0];
      // head.appendChild(js);
      this.scripts.push(url);
    } else {
      if (debug){
        console.debug( 'script ' + url + ' allready loaded' );
      }
      doCallback();
    }
  },
  
  loadStylesheet: function(url){
    if ( this.stylesheets.indexOf(url) < 0 ){
      var css = document.createElement('link');
      css.rel = 'stylesheet';
      css.type = 'text/css';
      css.href = url;
      document.getElementsByTagName('head')[0].appendChild(css);
      this.stylesheets.push(url);
    }
  },
  
  getMainTabPanel: function(){
    return this.mainTabPanel;
  },
  
  renderUserInformations: function(state){
    if ( state.user.name !== 'anonymous' ){
      var tpl = new Ext.XTemplate(this.loggedInTextTemplate);
      tpl.overwrite(Ext.get('scm-userinfo'), state);
      var text = '';
      if (state.user.mail){
        text += this.userInfoMailText + ': ' + state.user.mail + '<br />';
      }
      if (state.groups && state.groups.length > 0){
        text += this.userInfoGroupsText + ': ' + this.getGroups(state.groups) + '<br />';
      }

      Ext.QuickTips.register({
        target : 'scm-userinfo-tip',
        title : state.user.displayName,
        text : text,
        enabled : true
      });
    }
  },
  
  getGroups: function(groups){
    var out = '';
    var s = groups.length;
    for ( var i=0; i<s; i++ ){
      out += groups[i];
      if ( (i+1)<s ){
        out += ', ';
      }
    }
    return out;
  },
  
  removeUserInformations: function(){
    Ext.get('scm-userinfo').dom.innerHTML = '';
    Ext.QuickTips.unregister('scm-userinfo-tip');
  }

});

Ext.onReady(function(){
  
  function isLocalStorageAvailable(){
   var mod = '__scm-manager';
    try {
        localStorage.setItem(mod, mod);
        localStorage.removeItem(mod);
        return true;
    } catch(e) {
        return false;
    }
  }

  var stateProvider;
  if (isLocalStorageAvailable()){
    if (debug){
      console.debug('use localStore to save application state');
    }
    stateProvider = new Sonia.uistate.WebStorageProvider();
  } else {
    if (debug){
      console.debug('use cookies to save application state, because localStore is not available');
    }
    stateProvider = new Ext.state.CookieProvider();
  }

  Ext.state.Manager.setProvider(stateProvider);

  var mainTabPanel = new Ext.TabPanel({
    id: 'mainTabPanel',
    region: 'center',
    deferredRender: false,
    enableTabScroll: true,
    listeners: {
      tabchange: function(tabPanel, tab){
        if ( Ext.isDefined(tab) ){
          Sonia.History.onActivate(tab);
        }
      }
    }
  });

  new Ext.Viewport({
    layout: 'border',
    items: [
    new Ext.BoxComponent({
      region: 'north',
      id: 'north-panel',
      contentEl: 'north',
      height: 60
    }), {
      region: 'west',
      id: 'navigationPanel',
      title: 'Navigation',
      xtype: 'navPanel',
      split: true,
      width: 200,
      minSize: 175,
      maxSize: 400,
      collapsible: true,
      margins: '0 0 0 5'
    },
    new Ext.BoxComponent({
      region: 'south',
      id: 'south-panel',
      contentEl: 'south',
      height: 16,
      margins: '2 2 2 5'
    }),
    mainTabPanel
    ]
  });

  main = new Sonia.scm.Main();

  /**
   * Adds a tab to main TabPanel
   *
   * @deprecated use main.addTabPanel
   */
  function addTabPanel(id, xtype, title){
    main.addTabPanel(id, xtype, title);
  }

  main.addListeners('init', initCallbacks);
  main.addListeners('login', loginCallbacks);
  main.addListeners('logout', logoutCallbacks);

  main.addListeners('login', function(){
    Ext.History.init();
  });
  
  // user informations
  main.addListeners('login', main.renderUserInformations);
  main.addListeners('logout', main.removeUserInformations);
  
  main.init();
  main.checkLogin();
});