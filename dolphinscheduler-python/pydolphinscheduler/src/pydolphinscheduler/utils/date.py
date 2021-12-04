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

"""Date util function collections."""

from datetime import datetime

from pydolphinscheduler.constants import Delimiter, Time

LEN_SUPPORT_DATETIME = (
    15,
    19,
)

FMT_SHORT = f"{Time.FMT_SHORT_DATE} {Time.FMT_NO_COLON_TIME}"
FMT_DASH = f"{Time.FMT_DASH_DATE} {Time.FMT_STD_TIME}"
FMT_STD = f"{Time.FMT_STD_DATE} {Time.FMT_STD_TIME}"

MAX_DATETIME = datetime(9999, 12, 31, 23, 59, 59)


def conv_to_schedule(src: datetime) -> str:
    """Convert given datetime to schedule date string."""
    return datetime.strftime(src, FMT_STD)


def conv_from_str(src: str) -> datetime:
    """Convert given string to datetime.

    This function give an ability to convert string to datetime, and for now it could handle
    format like:
    - %Y-%m-%d
    - %Y/%m/%d
    - %Y%m%d
    - %Y-%m-%d %H:%M:%S
    - %Y/%m/%d %H:%M:%S
    - %Y%m%d %H%M%S
    If pattern not like above be given will raise NotImplementedError.
    """
    len_ = len(src)
    if len_ == Time.LEN_SHORT_DATE:
        return datetime.strptime(src, Time.FMT_SHORT_DATE)
    elif len_ == Time.LEN_STD_DATE:
        if Delimiter.BAR in src:
            return datetime.strptime(src, Time.FMT_STD_DATE)
        elif Delimiter.DASH in src:
            return datetime.strptime(src, Time.FMT_DASH_DATE)
        else:
            raise NotImplementedError(
                "%s could not be convert to datetime for now.", src
            )
    elif len_ in LEN_SUPPORT_DATETIME:
        if Delimiter.BAR in src and Delimiter.COLON in src:
            return datetime.strptime(src, FMT_STD)
        elif Delimiter.DASH in src and Delimiter.COLON in src:
            return datetime.strptime(src, FMT_DASH)
        elif (
            Delimiter.DASH not in src
            and Delimiter.BAR not in src
            and Delimiter.COLON not in src
        ):
            return datetime.strptime(src, FMT_SHORT)
        else:
            raise NotImplementedError(
                "%s could not be convert to datetime for now.", src
            )
    else:
        raise NotImplementedError("%s could not be convert to datetime for now.", src)
