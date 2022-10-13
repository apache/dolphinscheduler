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

"""Constants for pydolphinscheduler."""


class TaskPriority(str):
    """Constants for task priority."""

    HIGHEST = "HIGHEST"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"
    LOWEST = "LOWEST"


class TaskFlag(str):
    """Constants for task flag."""

    YES = "YES"
    NO = "NO"


class TaskTimeoutFlag(str):
    """Constants for task timeout flag."""

    CLOSE = "CLOSE"


class TaskType(str):
    """Constants for task type, it will also show you which kind we support up to now."""

    SHELL = "SHELL"
    HTTP = "HTTP"
    PYTHON = "PYTHON"
    SQL = "SQL"
    SUB_PROCESS = "SUB_PROCESS"
    PROCEDURE = "PROCEDURE"
    DATAX = "DATAX"
    DEPENDENT = "DEPENDENT"
    CONDITIONS = "CONDITIONS"
    SWITCH = "SWITCH"
    FLINK = "FLINK"
    SPARK = "SPARK"
    MR = "MR"
    SAGEMAKER = "SAGEMAKER"
    MLFLOW = "MLFLOW"
    OPENMLDB = "OPENMLDB"
    PYTORCH = "PYTORCH"
    DVC = "DVC"


class DefaultTaskCodeNum(str):
    """Constants and default value for default task code number."""

    DEFAULT = 1


class JavaGatewayDefault(str):
    """Constants and default value for java gateway."""

    RESULT_MESSAGE_KEYWORD = "msg"
    RESULT_MESSAGE_SUCCESS = "success"

    RESULT_STATUS_KEYWORD = "status"
    RESULT_STATUS_SUCCESS = "SUCCESS"

    RESULT_DATA = "data"


class Delimiter(str):
    """Constants for delimiter."""

    BAR = "-"
    DASH = "/"
    COLON = ":"
    UNDERSCORE = "_"
    DIRECTION = "->"


class Time(str):
    """Constants for date."""

    FMT_STD_DATE = "%Y-%m-%d"
    LEN_STD_DATE = 10

    FMT_DASH_DATE = "%Y/%m/%d"

    FMT_SHORT_DATE = "%Y%m%d"
    LEN_SHORT_DATE = 8

    FMT_STD_TIME = "%H:%M:%S"
    FMT_NO_COLON_TIME = "%H%M%S"


class ResourceKey(str):
    """Constants for key of resource."""

    ID = "id"


class Symbol(str):
    """Constants for symbol."""

    SLASH = "/"
    POINT = "."
    COMMA = ","
    UNDERLINE = "_"
