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
BIN_DIR="$DIST_DIR/apache-dolphinscheduler-*-bin"
if [ ! -d $BIN_DIR ]; then
  echo "$BIN_DIR not found!!!"
  exit 1
fi

cd $BIN_DIR

# move *-plugins/target/*-plugin/target/*.jar to *-plugins/
PLUGINS_PATH=(
alert-plugins
datasource-plugins
storage-plugins
task-plugins
)

if [ "$PLUGINS_ASSEMBLY_SKIP" = "true" ]; then
  rm -rf "$BIN_DIR/plugins/"* 2>/dev/null
else
  for plugin_path in "${PLUGINS_PATH[@]}"
  do
    PLUGIN_DIR="$BIN_DIR/plugins/$plugin_path"
    [ ! -d "$PLUGIN_DIR" ] && { echo "WARN: 插件目录不存在，跳过 → $PLUGIN_DIR"; continue; }
    cd "$PLUGIN_DIR" || { echo "ERROR: 进入插件目录失败 → $PLUGIN_DIR"; exit 1; }

    # 核心：仅处理datasource-plugins，完全按你的路径要求实现
    if [ "$plugin_path" = "datasource-plugins" ]; then
      echo -e "\n===== 【精准处理】datasource-plugins 驱动包+JAR包迁移 ====="

      # 遍历所有数据源插件子目录（dolphinscheduler-datasource-*）
      for ds_subdir in dolphinscheduler-datasource-*; do
        [ ! -d "$ds_subdir" ] && continue
        echo "处理数据源插件 → $ds_subdir"

        # 【原始驱动目录】严格匹配你的路径：xxx/target/driver/xxx
        src_driver_root="$ds_subdir/target/driver"
        if [ -d "$src_driver_root" ]; then
          # 遍历原始driver下的子目录（如mysql、postgresql，保证目录结构不变）
          for src_subdir in "$src_driver_root"/*; do
            [ ! -d "$src_subdir" ] && continue
            # 提取子目录名（如mysql）
            subdir_name=$(basename "$src_subdir")
            # 【目标驱动目录】严格匹配你的要求：driver/子目录名
            dest_driver_dir="driver/$subdir_name"
            # 创建目标目录（自动递归，不存在则创建）
            mkdir -p "$dest_driver_dir"

            # 【核心移动】原始jar → 目标目录，覆盖同名文件
            # 原始：ds_subdir/target/driver/mysql/mysql-connector-j-8.0.33.jar
            # 目标：driver/mysql/mysql-connector-j-8.0.33.jar
            mv -f "$src_subdir"/* "$dest_driver_dir/"
            echo "✅ 驱动包迁移成功：$src_subdir/* → $dest_driver_dir/"
          done
        else
            # 无driver目录仅打印提示，直接跳过驱动处理
            echo "WARN: 无driver目录，跳过当前插件驱动包处理 → $ds_subdir"
        fi

        # 【核心修改】迁移target目录下【所有JAR包】到根目录（无名称/层级限制）
        # 匹配：ds_subdir/target/下任意JAR（如dolphinscheduler-datasource-mysql-dev-SNAPSHOT-shade.jar）
        # 结果：datasource-plugins/xxx.jar
        find "$ds_subdir/target" -type f -name "*.jar" -exec mv -f {} ./ \; 2>/dev/null
        echo "✅ 所有JAR包迁移成功：$ds_subdir/target/下所有*.jar → 根目录"
      done

      echo -e "\n===== 清理：模糊删除所有dolphinscheduler-datasource-*目录 ====="
      # 仅删除目录，避免通配符匹配到文件/软链接，2>/dev/null屏蔽无匹配的警告
      rm -rf dolphinscheduler-datasource-*/ 2>/dev/null
      echo "✅ 清理完成，仅保留【根目录所有JAR + driver目录】"

    # 其他插件（alert/storage/task）：保持原有逻辑，简洁处理
    else
      echo -e "\n===== 处理普通插件 → $plugin_path ====="
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

# 定义共享库目录，加引号防止路径含空格/特殊字符
SHARED_LIB_DIR="$BIN_DIR/libs"
# 确保目录存在，-p屏蔽已存在警告
mkdir -p "$SHARED_LIB_DIR"

for module in "${MODULES_PATH[@]}"; do
    MODULE_LIB_DIR="$BIN_DIR/$module/libs"
    [ ! -d "$MODULE_LIB_DIR" ] && continue
    cd "$MODULE_LIB_DIR" || continue

    # 关键步骤1：先在模块本地记录自身的jar包名（仅当前模块，无跨模块污染）
    # 用数组存储，纯内置操作，无额外进程
    local_jars=()
    for jar in *.jar; do
        [ -f "$jar" ] && local_jars+=("$jar")
    done
    # 无本地jar包，直接跳过后续操作
    [ ${#local_jars[@]} -eq 0 ] && continue

    # 步骤2：tar管道批量迁移当前模块的jar包到共享库（仅迁移本地记录的jar，精准无冗余）
    tar -cf - "${local_jars[@]}" 2>/dev/null | tar -xf - -C "$SHARED_LIB_DIR" 2>/dev/null

    # 步骤3：删除模块本地原jar包（先删，避免软链创建失败）
    rm -f "${local_jars[@]}" 2>/dev/null

    # 步骤4：仅基于本地记录的jar包名创建软链（精准匹配当前模块，彻底避免跨模块污染）
    for jar_name in "${local_jars[@]}"; do
        ln -s ../../libs/"$jar_name" "$jar_name" 2>/dev/null
    done
done

# create symbolic link for standalone-server
cd $BIN_DIR/standalone-server && ln -s ../tools/sql/sql sql

# repack bin tar
BIN_DIR_NAME=$(basename $BIN_DIR)
BIN_TAR_FILE_NAME=$BIN_DIR_NAME.tar.gz
cd $DIST_DIR && tar -zcf $BIN_TAR_FILE_NAME $BIN_DIR_NAME

echo "assembly-plugins.sh done"
