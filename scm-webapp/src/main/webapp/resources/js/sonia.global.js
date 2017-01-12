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

// enable debug mode, if console is available
var debug = typeof console !== 'undefined' && typeof console.debug !== 'undefined';

// send X-SCM-Client on every ajax request
Ext.Ajax.defaultHeaders = {
  'X-SCM-Client': 'WUI'
};

// XSRF protection
Ext.Ajax.on('beforerequest', function(conn, options){
  var token = Ext.util.Cookies.get('X-XSRF-Token');
  if (token){
    if (!options.headers){
      options.headers = {};
    }
    options.headers['X-XSRF-Token'] = token;
  }
});

var state = null;
var admin = false;

// sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT
var dummyPassword = '__dummypassword__';

/**
 * functions called after login
 *
 * @deprecated use main.addListener('login', fn, scope)
 */
var loginCallbacks = [];

/**
 * functions called after logout
 *
 * @deprecated use main.addListener('logout', fn, scope)
 */
var logoutCallbacks = [];

/**
 * functions called after initialisation
 */
var initCallbacks = [];

/**
 * The base url for the rest api
 */
var restUrl = "api/rest/";

var userSearchStore = new Ext.data.JsonStore({
  root: 'results',
  idProperty: 'value',
  fields: ['value','label'],
  proxy: new Ext.data.HttpProxy({
    url: restUrl + 'search/users.json',
    method: 'GET'
  })
});

var groupSearchStore = new Ext.data.JsonStore({
  root: 'results',
  idProperty: 'value',
  fields: ['value','label'],
  proxy: new Ext.data.HttpProxy({
    url: restUrl + 'search/groups.json',
    method: 'GET'
  })
});

// SONIA

Sonia = {
  
  idSeparator: ';',
  idNoneObject: '-',
  
  id: function(){
    var id = '';
    for ( var i=0; i<arguments.length; i++ ){
      id += arguments[i];
      if ( (i+1) < arguments.length ){
        id += this.idSeparator;
      }
    }
    if (id.length() === 0){
      id = Ext.id();
    }
    return id;
  }
  
};


// the main object (sonia.scm)
var main = null;

// enable extjs quicktips
Ext.QuickTips.init();
