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


Sonia.config.ScmConfigPanel = Ext.extend(Sonia.config.ConfigPanel,{

  titleText: 'General Settings',
  servnameText: 'Servername',
  realmDescriptionText: 'Realm description',
  dateFormatText: 'Date format',
  enableForwardingText: 'Enable forwarding (mod_proxy)',
  forwardPortText: 'Forward Port',
  pluginRepositoryText: 'Plugin repository',
  allowAnonymousAccessText: 'Allow Anonymous Access',
  enableSSLText: 'Enable SSL',
  sslPortText: 'SSL Port',
  adminGroupsText: 'Admin Groups',
  adminUsersText: 'Admin Users',
  submitText: 'Submit ...',
  loadingText: 'Loading ...',
  errorTitleText: 'Error',
  errorMsgText: 'Could not load config.',
  errorSubmitMsgText: 'Could not submit config.',
  
  // TODO i18n
  skipFailedAuthenticatorsText: 'Skip failed authenticators',
  loginAttemptLimitText: 'Login Attempt Limit',
  loginAttemptLimitTimeoutText: 'Login Attempt Limit Timeout',
  
  enableProxyText: 'Enable Proxy',
  proxyServerText: 'Proxy Server',
  proxyPortText: 'Proxy Port',
  proxyUserText: 'Proxy User',
  proxyPasswordText: 'Proxy Password',
  proxyExcludesText: 'Proxy Excludes',
  baseUrlText: 'Base Url',
  forceBaseUrlText: 'Force Base Url',
  disableGroupingGridText: 'Disable repository Groups',
  enableRepositoryArchiveText: 'Enable repository archive',
  

  // help
  servernameHelpText: 'The name of this server. This name will be part of the repository url.',
  realmDescriptionHelpText: 'Enter authentication realm description',
  // TODO i18n
  dateFormatHelpText: 'Moments date format. Please have a look at \n\
                      <a href="http://momentjs.com/docs/#/displaying/format/" target="_blank">http://momentjs.com/docs/#/displaying/format/</a>.<br />\n\
                      <b>Note:</b><br />\n\
                      {0} - is replaced by a "time ago" string (e.g. 2 hours ago).',
  pluginRepositoryHelpText: 'The url of the plugin repository.<br />Explanation of the {placeholders}:\n\
  <br /><b>version</b> = SCM-Manager Version<br /><b>os</b> = Operation System<br /><b>arch</b> = Architecture',
  enableForwardingHelpText: 'Enbale mod_proxy port forwarding.',
  forwardPortHelpText: 'The forwarding port.',
  allowAnonymousAccessHelpText: 'Anonymous users have read access on public repositories.',
  enableSSLHelpText: 'Enable secure connections via HTTPS.',
  sslPortHelpText: 'The ssl port.',
  adminGroupsHelpText: 'Comma seperated list of groups with admin permissions.',
  adminUsersHelpText: 'Comma seperated list of users with admin permissions.',
  
  // TODO i18n
  skipFailedAuthenticatorsHelpText: 'Do not stop the authentication chain, \n\
                                     if an authenticator finds the user but fails to authenticate the user.',
  loginAttemptLimitHelpText: 'Maximum allowed login attempts. Use -1 to disable the login attempt limit.',
  loginAttemptLimitTimeoutHelpText: 'Timeout in seconds for users which are temporary disabled,\
                                     because of too many failed login attempts.',
  
  enableProxyHelpText: 'Enable Proxy',
  proxyServerHelpText: 'The proxy server',
  proxyPortHelpText: 'The proxy port',
  proxyUserHelpText: 'The username for the proxy server authentication.',
  proxyPasswordHelpText: 'The password for the proxy server authentication.',
  proxyExcludesHelpText: 'A comma separated list of glob patterns for hostnames which should be excluded from proxy settings.',
  baseUrlHelpText: 'The url of the application (with context path) i.e. http://localhost:8080/scm',
  forceBaseUrlHelpText: 'Redirects to the base url if the request comes from a other url',
  disableGroupingGridHelpText: 'Disable repository Groups. A complete page reload is required after a change of this value.',
  enableRepositoryArchiveHelpText: 'Enable repository archives. A complete page reload is required after a change of this value.',
  
  // TODO i18n
  enableXsrfProtectionText: 'Enable Xsrf Protection',
  enableXsrfProtectionHelpText: 'Enable Xsrf Cookie Protection. <b>Note:</b> This feature is still experimental.',

  initComponent: function(){

    var config = {
      title: main.tabGeneralConfigText,
      panels: [{
        xtype: 'configForm',
        title: this.titleText,
        items: [{
          xtype: 'textfield',
          fieldLabel: this.baseUrlText,
          name: 'base-url',
          helpText: this.baseUrlHelpText,
          allowBlank: false
        },{
          xtype: 'checkbox',
          fieldLabel: this.forceBaseUrlText,
          name: 'force-base-url',
          inputValue: 'true',
          helpText: this.forceBaseUrlHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.disableGroupingGridText,
          name: 'disableGroupingGrid',
          inputValue: 'true',
          helpText: this.disableGroupingGridHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableRepositoryArchiveText,
          name: 'enableRepositoryArchive',
          inputValue: 'true',
          helpText: this.enableRepositoryArchiveHelpText
        },{
          xtype: 'textfield',
          fieldLabel: this.realmDescriptionText,
          name: 'realmDescription',
          allowBlank: false,
          helpText: this.realmDescriptionHelpText
        },{
          xtype: 'textfield',
          fieldLabel: this.dateFormatText,
          name: 'dateFormat',
          helpText: this.dateFormatHelpText,
          helpDisableAutoHide: true,
          allowBlank: false
        },{
          xtype: 'textfield',
          fieldLabel: this.pluginRepositoryText,
          name: 'plugin-url',
          vtype: 'pluginurl',
          allowBlank: false,
          helpText: this.pluginRepositoryHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.allowAnonymousAccessText,
          name: 'anonymousAccessEnabled',
          inputValue: 'true',
          helpText: this.allowAnonymousAccessHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.skipFailedAuthenticatorsText,
          name: 'skip-failed-authenticators',
          inputValue: 'true',
          helpText: this.skipFailedAuthenticatorsHelpText
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableXsrfProtectionText,
          name: 'xsrf-protection',
          inputValue: 'true',
          helpText: this.enableXsrfProtectionHelpText
        },{
          xtype: 'numberfield',
          fieldLabel: this.loginAttemptLimitText,
          name: 'login-attempt-limit',
          allowBlank: false,
          helpText: this.loginAttemptLimitHelpText          
        },{
          xtype: 'numberfield',
          fieldLabel: this.loginAttemptLimitTimeoutText,
          name: 'login-attempt-limit-timeout',
          allowBlank: false,
          helpText: this.loginAttemptLimitTimeoutHelpText          
        },{
          xtype: 'checkbox',
          fieldLabel: this.enableProxyText,
          name: 'enableProxy',
          inputValue: 'true',
          helpText: this.enableProxyHelpText,
          listeners: {
            check: function(){
              Ext.getCmp('proxyServer').setDisabled( ! this.checked );
              Ext.getCmp('proxyPort').setDisabled( ! this.checked );
              Ext.getCmp('proxyUser').setDisabled( ! this.checked );
              Ext.getCmp('proxyPassword').setDisabled( ! this.checked );
              Ext.getCmp('proxyExcludes').setDisabled( ! this.checked );
            }
          }
        },{
          id: 'proxyServer',
          xtype: 'textfield',
          fieldLabel: this.proxyServerText,
          name: 'proxyServer',
          disabled: true,
          helpText: this.proxyServerHelpText,
          allowBlank: false
        },{
          id: 'proxyPort',
          xtype: 'numberfield',
          fieldLabel: this.proxyPortText,
          name: 'proxyPort',
          disabled: true,
          allowBlank: false,
          helpText: this.proxyPortHelpText
        },{
          id: 'proxyUser',
          xtype: 'textfield',
          fieldLabel: this.proxyUserText,
          name: 'proxyUser',
          disabled: true,
          helpText: this.proxyUserHelpText,
          allowBlank: true
        },{
          id: 'proxyPassword',
          xtype: 'textfield',
          inputType: 'password',
          fieldLabel: this.proxyPasswordText,
          name: 'proxyPassword',
          disabled: true,
          helpText: this.proxyPasswordHelpText,
          allowBlank: true
        },{
          id: 'proxyExcludes',
          xtype: 'textfield',
          fieldLabel: this.proxyExcludesText,
          name: 'proxy-excludes',
          disabled: true,
          helpText: this.proxyExcludesHelpText,
          allowBlank: true
        },{
          xtype : 'textfield',
          fieldLabel : this.adminGroupsText,
          name : 'admin-groups',
          allowBlank : true,
          helpText: this.adminGroupsHelpText
        },{
          xtype : 'textfield',
          fieldLabel : this.adminUsersText,
          name : 'admin-users',
          allowBlank : true,
          helpText: this.adminUsersHelpText
        }],
      
        onSubmit: function(values){
          this.el.mask(this.submitText);
          Ext.Ajax.request({
            url: restUrl + 'config.json',
            method: 'POST',
            jsonData: values,
            scope: this,
            disableCaching: true,
            success: function(){
              this.el.unmask();
            },
            failure: function(result){
              this.el.unmask();
              main.handleRestFailure(
                result, 
                this.errorTitleText, 
                this.errorMsgText
              );
            }
          });
        },

        onLoad: function(el){
          var tid = setTimeout( function(){ el.mask(this.loadingText); }, 100);
          Ext.Ajax.request({
            url: restUrl + 'config.json',
            method: 'GET',
            scope: this,
            disableCaching: true,
            success: function(response){
              var obj = Ext.decode(response.responseText);
              this.load(obj);
              if ( obj.enableProxy ){
                Ext.getCmp('proxyServer').setDisabled(false);
                Ext.getCmp('proxyPort').setDisabled(false);
                Ext.getCmp('proxyUser').setDisabled(false);
                Ext.getCmp('proxyPassword').setDisabled(false);
                Ext.getCmp('proxyExcludes').setDisabled(false);
              }
              clearTimeout(tid);
              el.unmask();
            },
            failure: function(result){
              el.unmask();
              clearTimeout(tid);
              main.handleRestFailure(
                result, 
                this.errorTitleText, 
                this.errorMsgText
              );
            }
          });
        }
      }, generalConfigPanels]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.config.ScmConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("scmConfig", Sonia.config.ScmConfigPanel);
