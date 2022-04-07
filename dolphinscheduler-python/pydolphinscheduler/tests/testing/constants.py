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

"""Constants variables for test module."""

import os

# Record some task without example in directory `example`. Some of them maybe can not write example,
# but most of them just without adding by mistake, and we should add it later.
task_without_example = {
    "sql",
    "http",
    "sub_process",
    "python",
    "procedure",
}

# The examples ignore test to run it. Those examples could not be run directly cause it need other
# support like resource files, data source and etc. But we should try to run them later for more coverage
ignore_exec_examples = {
    "task_datax_example",
    "task_flink_example",
    "task_map_reduce_example",
    "task_spark_example",
}

# pydolphinscheduler environment home
ENV_PYDS_HOME = "PYDOLPHINSCHEDULER_HOME"

# whether in dev mode, if true we will add or remove some tests. Or make be and more detail infos when
# test failed.
DEV_MODE = str(
    os.environ.get("PY_DOLPHINSCHEDULER_DEV_MODE", False)
).strip().lower() in {"true", "t", "1"}
