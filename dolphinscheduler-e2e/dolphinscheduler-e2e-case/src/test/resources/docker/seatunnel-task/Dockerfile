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

FROM apache/dolphinscheduler-standalone-server:ci

ENV SEATUNNEL_HOME=/opt/seatunnel

# The pinned Apache distribution includes the FakeSource and Console connectors.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl procps psmisc \
    && rm -rf /var/lib/apt/lists/* \
    && curl --fail --location --connect-timeout 30 --max-time 600 \
        --speed-limit 1024 --speed-time 60 --retry 0 \
        https://dlcdn.apache.org/seatunnel/2.3.3/apache-seatunnel-2.3.3-bin.tar.gz \
        --output /tmp/seatunnel.tar.gz \
    && echo "e82ad8335c3a8cd332a4c0626629cb48ad330474207f3ea4260eddf60869302c188d184b2f3c9c8f755ace91736cdf2a5b1b4283677426562feee1a9cb9ddcaa  /tmp/seatunnel.tar.gz" | sha512sum --check \
    && mkdir -p /opt/seatunnel \
    && tar -xzf /tmp/seatunnel.tar.gz --strip-components=1 -C /opt/seatunnel \
    && rm /tmp/seatunnel.tar.gz \
    && test -f /opt/seatunnel/connectors/seatunnel/connector-fake-2.3.3.jar \
    && test -f /opt/seatunnel/connectors/seatunnel/connector-console-2.3.3.jar \
    && sed -i -e 's/type: hdfs/type: localfile/' \
        -e '/storage.type:/d' -e '/fs.defaultFS:/d' \
        -e 's@namespace: /tmp/seatunnel/checkpoint_snapshot@namespace: /tmp/seatunnel/checkpoint_snapshot/@' \
        /opt/seatunnel/config/seatunnel.yaml \
    && mkdir -p /opt/seatunnel/logs \
    && chmod 1777 /opt/seatunnel/logs \
    && test -x "${JAVA_HOME}/bin/java" \
    && printf '%s\n' 'export SEATUNNEL_HOME=/opt/seatunnel' "export JAVA_HOME=${JAVA_HOME}" \
        'export PATH="$JAVA_HOME/bin:$PATH"' \
        > /etc/profile.d/seatunnel.sh

# SeaTunnel 2.3.3's CheckpointPlan uses Jersey's CopyOnWriteHashMap even with localfile storage.
RUN curl --fail --location --connect-timeout 30 --max-time 120 \
        --speed-limit 1024 --speed-time 30 --retry 0 \
        https://repo.maven.apache.org/maven2/com/sun/jersey/jersey-client/1.19/jersey-client-1.19.jar \
        --output /opt/seatunnel/lib/jersey-client-1.19.jar \
    && echo "591118924e4ecad37b25d2e955ae9531f4f7752aed587f99c6ccc98e03aaae5e  /opt/seatunnel/lib/jersey-client-1.19.jar" | sha256sum --check
