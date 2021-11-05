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

"""The script for setting up pydolphinscheduler."""

import sys
from os.path import dirname, join

from setuptools import find_packages, setup

version = "0.0.1.dev0"

if sys.version_info[0] < 3:
    raise Exception(
        "pydolphinscheduler does not support Python 2. Please upgrade to Python 3."
    )


def read(*names, **kwargs):
    """Read file content from given file path."""
    return open(
        join(dirname(__file__), *names), encoding=kwargs.get("encoding", "utf8")
    ).read()


setup(
    name="pydolphinscheduler",
    version=version,
    license="Apache License 2.0",
    description="Apache DolphinScheduler python SDK",
    long_description=read("README.md"),
    # Make sure pypi is expecting markdown
    long_description_content_type="text/markdown",
    author="Apache Software Foundation",
    author_email="dev@dolphinscheduler.apache.org",
    url="https://dolphinscheduler.apache.org/",
    python_requires=">=3.6",
    keywords=[
        "dolphinscheduler",
        "workflow",
        "scheduler",
        "taskflow",
    ],
    project_urls={
        "Homepage": "https://dolphinscheduler.apache.org",
        "Documentation": "https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/quick-start.html",
        "Source": "https://github.com/apache/dolphinscheduler",
        "Issue Tracker": "https://github.com/apache/dolphinscheduler/issues",
        "Discussion": "https://github.com/apache/dolphinscheduler/discussions",
        "Twitter": "https://twitter.com/dolphinschedule",
    },
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    include_package_data=True,
    classifiers=[
        # complete classifier list: http://pypi.python.org/pypi?%3Aaction=list_classifiers
        "Development Status :: 1 - Planning",
        "Environment :: Console",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: Unix",
        "Operating System :: POSIX",
        "Operating System :: Microsoft :: Windows",
        "Programming Language :: Python",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: Implementation :: CPython",
        "Programming Language :: Python :: Implementation :: PyPy",
        "Topic :: Software Development :: User Interfaces",
    ],
    install_requires=[
        # Core
        "py4j~=0.10",
        # Dev
        "pytest~=6.2",
    ],
)
