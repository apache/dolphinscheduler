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

"""Testing util about file operating."""

from pathlib import Path
from typing import Union


def get_file_content(path: Union[str, Path]) -> str:
    """Get file content in given path."""
    with open(path, mode="r") as f:
        return f.read()


def delete_file(path: Union[str, Path]) -> None:
    """Delete file in given path."""
    path = Path(path).expanduser() if isinstance(path, str) else path.expanduser()
    if path.exists():
        path.unlink()
