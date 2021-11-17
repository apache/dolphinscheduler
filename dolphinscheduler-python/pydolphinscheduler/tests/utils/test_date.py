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

"""Test utils.date module."""

from datetime import datetime

import pytest

from pydolphinscheduler.utils.date import FMT_STD, conv_from_str, conv_to_schedule

curr_date = datetime.now()


@pytest.mark.parametrize(
    "src,expect",
    [
        (curr_date, curr_date.strftime(FMT_STD)),
        (datetime(2021, 1, 1), "2021-01-01 00:00:00"),
        (datetime(2021, 1, 1, 1), "2021-01-01 01:00:00"),
        (datetime(2021, 1, 1, 1, 1), "2021-01-01 01:01:00"),
        (datetime(2021, 1, 1, 1, 1, 1), "2021-01-01 01:01:01"),
        (datetime(2021, 1, 1, 1, 1, 1, 1), "2021-01-01 01:01:01"),
    ],
)
def test_conv_to_schedule(src: datetime, expect: str) -> None:
    """Test function conv_to_schedule."""
    assert expect == conv_to_schedule(src)


@pytest.mark.parametrize(
    "src,expect",
    [
        ("2021-01-01", datetime(2021, 1, 1)),
        ("2021/01/01", datetime(2021, 1, 1)),
        ("20210101", datetime(2021, 1, 1)),
        ("2021-01-01 01:01:01", datetime(2021, 1, 1, 1, 1, 1)),
        ("2021/01/01 01:01:01", datetime(2021, 1, 1, 1, 1, 1)),
        ("20210101 010101", datetime(2021, 1, 1, 1, 1, 1)),
    ],
)
def test_conv_from_str_success(src: str, expect: datetime) -> None:
    """Test function conv_from_str success case."""
    assert expect == conv_from_str(
        src
    ), f"Function conv_from_str convert {src} not expect to {expect}."


@pytest.mark.parametrize(
    "src",
    [
        "2021-01-01 010101",
        "2021:01:01",
        "202111",
        "20210101010101",
        "2021:01:01 01:01:01",
    ],
)
def test_conv_from_str_not_impl(src: str) -> None:
    """Test function conv_from_str fail case."""
    with pytest.raises(NotImplementedError):
        conv_from_str(src)
