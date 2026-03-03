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
set -xeo pipefail

PLUGINS_ASSEMBLY_SKIP=$1

DIST_DIR="$(pwd)/target"
BIN_TAR_FILE="$DIST_DIR/apache-dolphinscheduler-*-bin.tar.gz"
if [ ! -f $BIN_TAR_FILE ]; then
  echo "$BIN_TAR_FILE not found!!!"
  exit 1
fi

cd $DIST_DIR && tar -zxf apache-dolphinscheduler-*-bin.tar.gz
cd $DIST_DIR/apache-dolphinscheduler-*-bin
BIN_DIR=$(pwd)

# move *-plugins/target/*-plugin/target/*.jar to *-plugins/
PLUGINS_PATH=(
alert-plugins
datasource-plugins
storage-plugins
task-plugins
)

if [ $PLUGINS_ASSEMBLY_SKIP == "true" ]; then
  rm -rf $BIN_DIR/plugins/*
else
  for plugin_path in ${PLUGINS_PATH[@]}
  do
    PLUGIN_DIR="$BIN_DIR/plugins/$plugin_path"
    [ ! -d "$PLUGIN_DIR" ] && { echo "WARN: Plugin directory not found, skip → $PLUGIN_DIR"; continue; }
    cd "$PLUGIN_DIR" || { echo "ERROR: Failed to enter plugin directory → $PLUGIN_DIR"; exit 1; }

    # Process datasource-plugins specifically
    if [ "$plugin_path" = "datasource-plugins" ]; then
      echo -e "\n===== Processing datasource-plugins: driver + JAR migration ====="

      # Iterate through all datasource plugin subdirectories
      for ds_subdir in dolphinscheduler-datasource-*; do
        [ ! -d "$ds_subdir" ] && continue
        echo "Processing datasource plugin → $ds_subdir"

        # Source driver directory path
        src_driver_root="$ds_subdir/target/driver"
        if [ -d "$src_driver_root" ]; then
          # Process each driver subdirectory (mysql, postgresql, etc.)
          for src_subdir in "$src_driver_root"/*; do
            [ ! -d "$src_subdir" ] && continue
            subdir_name=$(basename "$src_subdir")
            dest_driver_dir="driver/$subdir_name"
            mkdir -p "$dest_driver_dir"

            # Move driver JAR files to target directory
            mv -f "$src_subdir"/* "$dest_driver_dir/"
            echo "✅ Driver packages migrated: $src_subdir/* → $dest_driver_dir/"
          done
        else
            echo "WARN: No driver directory, skip driver processing → $ds_subdir"
        fi

        # Move all JAR files from target directory to root
        find "$ds_subdir/target" -type f -name "*.jar" -exec mv -f {} ./ \; 2>/dev/null
        echo "✅ All JAR files migrated: $ds_subdir/target/*.jar → root directory"
      done

      echo -e "\n===== Cleanup: remove all dolphinscheduler-datasource-* directories ====="
      rm -rf dolphinscheduler-datasource-*/ 2>/dev/null
      echo "✅ Cleanup completed, only JAR files and driver directory remain"

    # Other plugins (alert/storage/task): standard processing
    else
      echo -e "\n===== Processing regular plugin → $plugin_path ====="
      find . -type f -name "*.jar" -exec mv -f {} . \; 2>/dev/null
      find . -type d -mindepth 1 -exec rm -rf {} + 2>/dev/null
    fi
  done
fi

# move *-server/libs/*.jar to libs/ and create symbolic link in *-server/libs/
MODULES_PATH=(
api-server
master-server
worker-server
alert-server
tools
)

SHARED_LIB_DIR="$BIN_DIR/libs"
mkdir -p $SHARED_LIB_DIR

for module in ${MODULES_PATH[@]}
do
  MODULE_LIB_DIR="$BIN_DIR/$module/libs"
  cd $MODULE_LIB_DIR
  for jar in $(find $MODULE_LIB_DIR/* -name "*.jar" -execdir echo {} ';'); do
    # move jar file to share lib directory
    mv $MODULE_LIB_DIR/$jar $SHARED_LIB_DIR/$jar

    # create a symbolic link in the subproject's lib directory
    ln -s ../../libs/$jar $jar
  done
done

# create symbolic link for standalone-server
cd $BIN_DIR/standalone-server && ln -s ../tools/sql/sql sql

# repack bin tar
BIN_TAR_FILE_NAME=$(basename $BIN_TAR_FILE)
cd $DIST_DIR && tar -zcf $BIN_TAR_FILE_NAME apache-dolphinscheduler-*-bin

echo "assembly-plugins.sh done"
