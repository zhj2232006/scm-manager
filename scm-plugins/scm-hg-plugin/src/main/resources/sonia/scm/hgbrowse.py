#
# Copyright (c) 2010, Sebastian Sdorra
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of SCM-Manager; nor the names of its
#    contributors may be used to endorse or promote products derived from this
#    software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# http://bitbucket.org/sdorra/scm-manager
#
#

import sys, os

pythonPath = os.environ['SCM_PYTHON_PATH']

if len(pythonPath) > 0:
  pathParts = pythonPath.split(os.pathsep)
  for i in range(len(pathParts)):
    sys.path.insert(i, pathParts[i])


from mercurial import hg, ui
import datetime, time

def getName(path):
  parts = path.split('/')
  length = len(parts)
  if path.endswith('/'):
    length =- 1
  return parts[length - 1]

repositoryPath = os.environ['SCM_REPOSITORY_PATH']

revision = os.environ['SCM_REVISION']
path = os.environ['SCM_PATH']
name = getName(path)
length = 0
paths = []
repo = hg.repository(ui.ui(), path = repositoryPath)
subrepos = {}
revCtx = repo[revision]
mf = revCtx.manifest()
hgsub = revCtx.filectx('.hgsub').data().split('\n')
for line in hgsub:
  parts = line.split('=')
  if len(parts) > 1:
    subrepos[parts[0].strip()] = parts[1].strip()

if path is "":
  length = 1
  for f in mf:
    paths.append(f)
else:
  length = len(path.split('/')) + 1
  for f in mf:
    if f.startswith(path):
      paths.append(f)

files = []
directories = []

for k, v in subrepos.iteritems():
  if k.startswith(path):
    directories.append(k)

for p in paths:
  parts = p.split('/')
  depth = len(parts)
  if depth is length:
    file = repo[revision][p]
    files.append(file)
  elif depth > length:
    dirpath = ''
    for i in range(0, length):
      dirpath += parts[i] + '/'
    if not dirpath in directories:
      directories.append(dirpath)
    
print '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
print '<browser-result>'
print '  <revision>' + revision + '</revision>'
# todo print tag, and branch
print '  <files>'
for dir in directories:
  print '    <file>'
  print '      <name>' + getName(dir) + '</name>'
  print '      <path>' + dir + '</path>'
  print '      <directory>true</directory>'
  subrepo = subrepos[dir]
  if subrepo != None:
    print '      <subRepositoryUrl>' + subrepo + '</subRepositoryUrl>'
  print '    </file>'
    
for file in files:
  linkrev = repo[file.linkrev()]
  time = int(linkrev.date()[0]) * 1000
  desc = linkrev.description()
  print '    <file>'
  print '      <name>' + getName(file.path()) + '</name>'
  print '      <path>' + file.path() + '</path>'
  print '      <directory>false</directory>'
  print '      <length>' + str(file.size()) + '</length>'
  print '      <lastModified>' + str(time).split('.')[0] + '</lastModified>'
  print '      <description>' + desc + '</description>'
  print '    </file>'
print '  </files>'
print '</browser-result>'
