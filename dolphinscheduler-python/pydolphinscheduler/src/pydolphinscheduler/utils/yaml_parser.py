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

"""YAML parser utils, parser yaml string to ``ruamel.yaml`` object and nested key dict."""

import copy
import io
from typing import Any, Dict, Optional

from ruamel.yaml import YAML
from ruamel.yaml.comments import CommentedMap


class YamlParser:
    """A parser to parse Yaml file and provider easier way to access or change value.

    This parser provider delimiter string key to get or set :class:`ruamel.yaml.YAML` object

    For example, yaml config named ``test.yaml`` and its content as below:

    .. code-block:: yaml

        one:
          two1:
            three: value1
          two2: value2

    you could get ``value1`` and ``value2`` by nested path

    .. code-block:: python

        yaml_parser = YamlParser("test.yaml")

        # Use function ``get`` to get value
        value1 = yaml_parser.get("one.two1.three")
        # Or use build-in ``__getitem__`` to get value
        value2 = yaml_parser["one.two2"]

    or you could change ``value1`` to ``value3``, also change ``value2`` to ``value4`` by nested path assigned

    .. code-block:: python

        yaml_parser["one.two1.three"] = "value3"
        yaml_parser["one.two2"] = "value4"
    """

    def __init__(self, content: str, delimiter: Optional[str] = "."):
        self._content = content
        self.src_parser = content
        self._delimiter = delimiter

    @property
    def src_parser(self) -> CommentedMap:
        """Get src_parser property."""
        return self._src_parser

    @src_parser.setter
    def src_parser(self, content: str) -> None:
        """Set src_parser property."""
        self._yaml = YAML()
        self._src_parser = self._yaml.load(content)

    def parse_nested_dict(
        self, result: Dict, commented_map: CommentedMap, key: str
    ) -> None:
        """Parse :class:`ruamel.yaml.comments.CommentedMap` to nested dict using :param:`delimiter`."""
        if not isinstance(commented_map, CommentedMap):
            return
        for sub_key in set(commented_map.keys()):
            next_key = f"{key}{self._delimiter}{sub_key}"
            result[next_key] = commented_map[sub_key]
            self.parse_nested_dict(result, commented_map[sub_key], next_key)

    @property
    def dict_parser(self) -> Dict:
        """Get :class:`CommentedMap` to nested dict using :param:`delimiter` as key delimiter.

        Use Depth-First-Search get all nested key and value, and all key connect by :param:`delimiter`.
        It make users could easier access or change :class:`CommentedMap` object.

        For example, yaml config named ``test.yaml`` and its content as below:

        .. code-block:: yaml

            one:
              two1:
                three: value1
              two2: value2

        It could parser to nested dict as

        .. code-block:: python

            {
                "one": ordereddict([('two1', ordereddict([('three', 'value1')])), ('two2', 'value2')]),
                "one.two1": ordereddict([('three', 'value1')]),
                "one.two1.three": "value1",
                "one.two2": "value2",
            }
        """
        res = dict()
        src_parser_copy = copy.deepcopy(self.src_parser)

        base_keys = set(src_parser_copy.keys())
        if not base_keys:
            return res
        else:
            for key in base_keys:
                res[key] = src_parser_copy[key]
                self.parse_nested_dict(res, src_parser_copy[key], key)
            return res

    def __contains__(self, key) -> bool:
        return key in self.dict_parser

    def __getitem__(self, key: str) -> Any:
        return self.dict_parser[key]

    def __setitem__(self, key: str, val: Any) -> None:
        if key not in self.dict_parser:
            raise KeyError("Key %s do not exists.", key)

        mid = None
        keys = key.split(self._delimiter)
        for idx, k in enumerate(keys, 1):
            if idx == len(keys):
                mid[k] = val
            else:
                mid = mid[k] if mid else self.src_parser[k]

    def get(self, key: str) -> Any:
        """Get value by key, is call ``__getitem__``."""
        return self[key]

    def get_int(self, key: str) -> int:
        """Get value and covert it to int."""
        return int(self.get(key))

    def get_bool(self, key: str) -> bool:
        """Get value and covert it to boolean."""
        val = self.get(key)
        if isinstance(val, str):
            return val.lower() in {"true", "t"}
        elif isinstance(val, int):
            return val != 0
        else:
            return val

    def __str__(self) -> str:
        """Transfer :class:`YamlParser` to string object.

        It is useful when users want to output the :class:`YamlParser` object they change just now.
        """
        buf = io.StringIO()
        self._yaml.dump(self.src_parser, buf)
        return buf.getvalue()

    def __repr__(self) -> str:
        return f"YamlParser({str(self)})"
