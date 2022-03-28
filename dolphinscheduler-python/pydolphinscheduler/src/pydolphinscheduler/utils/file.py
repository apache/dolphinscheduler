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

"""File util for pydolphinscheduler."""

from pathlib import Path
from typing import Optional


def write(
    content: str,
    to_path: str,
    create: Optional[bool] = True,
    overwrite: Optional[bool] = False,
) -> None:
    """Write configs dict to configuration file.

    :param content: The source string want to write to :param:`to_path`.
    :param to_path: The path want to write content.
    :param create: Whether create the file parent directory or not if it does not exist.
      If set ``True`` will create file with :param:`to_path` if path not exists, otherwise
      ``False`` will not create. Default ``True``.
    :param overwrite: Whether overwrite the file or not if it exists. If set ``True``
      will overwrite the exists content, otherwise ``False`` will not overwrite it. Default ``True``.
    """
    path = Path(to_path)
    if not path.parent.exists():
        if create:
            path.parent.mkdir(parents=True)
        else:
            raise ValueError(
                "Parent directory do not exists and set param `create` to `False`."
            )
    if not path.exists():
        with path.open(mode="w") as f:
            f.write(content)
    elif overwrite:
        with path.open(mode="w") as f:
            f.write(content)
    else:
        raise FileExistsError(
            "File %s already exists and you choose not overwrite mode.", to_path
        )
