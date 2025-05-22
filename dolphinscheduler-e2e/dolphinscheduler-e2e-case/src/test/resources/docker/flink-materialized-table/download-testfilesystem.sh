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

set -ex

FLINK_HOME=/opt/flink/lib/
TEST_FILESYSTEM_URL="https://repo.maven.apache.org/maven2/org/apache/flink/flink-table-filesystem-test-utils/2.0.0/flink-table-filesystem-test-utils-2.0.0.jar"
TEST_FILESYSTEM_JAR="flink-table-filesystem-test-utils-2.0.0.jar"

if ! curl -Lo "${FLINK_HOME}/${TEST_FILESYSTEM_JAR}" ${TEST_FILESYSTEM_URL}; then
    echo "Fail to download ${TEST_FILESYSTEM_JAR}."
    exit 1
fi
