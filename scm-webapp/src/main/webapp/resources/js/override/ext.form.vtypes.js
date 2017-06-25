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

Ext.apply(Ext.form.VTypes, {

  // passord validator

  password: function(val, field) {
    if (field.initialPassField) {
      var pwd = Ext.getCmp(field.initialPassField);
      return (val === pwd.getValue());
    }
    return true;
  },
  
  passwordText: 'The passwords entered do not match!',
  
  // name validator
  
  name: function(val){
    return val.match(/^[^ ][A-z0-9\.\-_@ ]*[^ ]$/);
  },
  
  nameText: 'The name is invalid.',
  
  repositoryNameRegex: /(?!^\.\.$)(?!^\.$)(?!.*[\[\]])^[A-z0-9\.][A-z0-9\.\-_/]*$/,
  
  // repository name validator
  repositoryName: function(val){
    var result = true;
    if (val){
      var p = val.split('/');
      for (var i=0; i<p.length; i++){
        if (!this.repositoryNameRegex.test(p[i])){
          result = false;
          break;
        }
      }
    } else {
      result = false;
    }
    return result;
  },
  
  repositoryNameText: 'The name of the repository is invalid.',
  
  // username validator
  
  username: function(val){
    return this.name(val);
  },
  
  usernameText: 'The username is invalid.',
  
  emailRegex: /^[A-z0-9][\w.-]*@[A-z0-9][\w\-\.]*\.[A-z0-9][A-z0-9-]+$/,
  
  // override extjs email format validation to match backend validation rules
  // see https://bitbucket.org/sdorra/scm-manager/issues/909/new-gtld-support
  email: function(email) {
    return this.emailRegex.test(email);
  }
  
});