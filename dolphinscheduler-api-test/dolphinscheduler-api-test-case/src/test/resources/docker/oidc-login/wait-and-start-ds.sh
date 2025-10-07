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
set -euo pipefail

echo "Container started. Waiting for 40 seconds to allow Keycloak to initialize..."

# Wait for 60 seconds
sleep 60

echo "Wait finished. Preparing to start DolphinScheduler standalone server..."

# Ensure the start script is executable
if [ ! -x /opt/dolphinscheduler/standalone-server/bin/start.sh ]; then
  echo "start.sh not executable. Applying chmod +x ..."
  chmod +x /opt/dolphinscheduler/standalone-server/bin/start.sh || {
    echo "Failed to chmod start.sh" >&2
    ls -l /opt/dolphinscheduler/standalone-server/bin/start.sh || true
    exit 1
  }
fi

ls -l /opt/dolphinscheduler/standalone-server/bin/start.sh || true

echo "Starting DolphinScheduler..."
/opt/dolphinscheduler/standalone-server/bin/start.sh

while true; do
  sleep 5
  if ! pgrep -f 'org.apache.dolphinscheduler' >/dev/null 2>&1; then
    echo "Warning: DolphinScheduler process not detected yet (or already exited)." >&2
  fi
done
