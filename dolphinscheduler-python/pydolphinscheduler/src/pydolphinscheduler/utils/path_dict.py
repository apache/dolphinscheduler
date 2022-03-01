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

"""Path dict allow users access value by key chain, like `var.key1.key2.key3`."""


# TODO maybe we should rewrite it by `collections.abc.MutableMapping` later,
#  according to https://stackoverflow.com/q/3387691/7152658
class PathDict(dict):
    """Path dict allow users access value by key chain, like `var.key1.key2.key3`."""

    def __init__(self, original=None):
        super().__init__()
        if original is None:
            pass
        elif isinstance(original, dict):
            for key in original:
                self.__setitem__(key, original[key])
        else:
            raise TypeError(
                "Parameter original expected dict type but get %s", type(original)
            )

    def __getitem__(self, key):
        if "." not in key:
            # try:
            return dict.__getitem__(self, key)
        # except KeyError:
        #     # cPickle would get error when key without value pairs, in this case we just skip it
        #     return
        my_key, rest_of_key = key.split(".", 1)
        target = dict.__getitem__(self, my_key)
        if not isinstance(target, PathDict):
            raise KeyError(
                'Cannot get "%s" to (%s) as sub-key of "%s".'
                % (rest_of_key, repr(target), my_key)
            )
        return target[rest_of_key]

    def __setitem__(self, key, value):
        if "." in key:
            my_key, rest_of_key = key.split(".", 1)
            target = self.setdefault(my_key, PathDict())
            if not isinstance(target, PathDict):
                raise KeyError(
                    'Cannot set "%s" from (%s) as sub-key of "%s"'
                    % (rest_of_key, repr(target), my_key)
                )
            target[rest_of_key] = value
        else:
            if isinstance(value, dict) and not isinstance(value, PathDict):
                value = PathDict(value)
            dict.__setitem__(self, key, value)

    def __contains__(self, key):
        if "." not in key:
            return dict.__contains__(self, key)
        my_key, rest_of_key = key.split(".", 1)
        target = dict.__getitem__(self, my_key)
        if not isinstance(target, PathDict):
            return False
        return rest_of_key in target

    def setdefault(self, key, default):
        """Overwrite method dict.setdefault."""
        if key not in self:
            self[key] = default
        return self[key]

    __setattr__ = __setitem__
    __getattr__ = __getitem__
