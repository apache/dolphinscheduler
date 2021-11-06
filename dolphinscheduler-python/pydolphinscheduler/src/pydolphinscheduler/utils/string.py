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

"""String util function collections."""


def attr2camel(attr: str, include_private=True):
    """Covert class attribute name to camel case."""
    if include_private:
        attr = attr.lstrip("_")
    return snake2camel(attr)


def snake2camel(snake: str):
    """Covert snake case to camel case."""
    components = snake.split("_")
    return components[0] + "".join(x.title() for x in components[1:])


def class_name2camel(class_name: str):
    """Covert class name string to camel case."""
    return class_name[0].lower() + class_name[1:]
