# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""Init root of pydolphinscheduler."""

import logging

from pkg_resources import get_distribution

from pydolphinscheduler.java_gateway import JavaGate

__version__ = get_distribution("apache-dolphinscheduler").version

# validate the versions of python api and java gateway are equal
if gateway_version := JavaGate().get_gateway_version():
    if __version__ != gateway_version != "dev-SNAPSHOT":
        logging.getLogger(__name__).critical(
            f"Using unmatched version of pydolphinscheduler (version {__version__}) "
            f"and Java gateway (version {gateway_version}) may cause errors. "
            "We strongly recommend you to find the matched version (find another "
            "version at: https://pypi.org/project/apache-dolphinscheduler)"
        )
