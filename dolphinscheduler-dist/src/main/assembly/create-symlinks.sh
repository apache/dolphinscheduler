#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -eu
script_dir=`dirname $0`
cd $script_dir/../../../target
package_file=`ls apache-dolphinscheduler-*-bin.tar.gz`
echo $package_file
decompress_dirname="${package_file%.tar.gz}"
rm -rf $decompress_dirname
#Decompress package file
tar -xf $package_file
cd $decompress_dirname

SHARED_LIB_DIR="libs"
# create share lib directory
mkdir -p $SHARED_LIB_DIR

echo 'iterate through the lib directory for all subprojects'
for module in api-server master-server worker-server alert-server tools; do
  MODULE_LIB_DIR="$module/libs"
  echo "handling $MODULE_LIB_DIR"

  if [ -d "$MODULE_LIB_DIR" ]; then
    cd $MODULE_LIB_DIR

    for jar in `ls *.jar`; do
      # Move jar file to share lib directory
      mv $jar ../../$SHARED_LIB_DIR/

      # Create a symbolic link in the subproject's lib directory
      ln -s ../../$SHARED_LIB_DIR/$jar .
    done

    cd - > /dev/null
  fi
done
#Recompress the package
cd ..
tar -zcf $package_file $decompress_dirname
rm -rf $decompress_dirname