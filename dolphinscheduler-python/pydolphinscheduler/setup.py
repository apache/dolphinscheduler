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
import logging
import os
import sys
from distutils.dir_util import remove_tree
from os.path import dirname, join
from typing import List

from setuptools import Command, find_packages, setup

if sys.version_info[0] < 3:
    raise Exception(
        "pydolphinscheduler does not support Python 2. Please upgrade to Python 3."
    )

logger = logging.getLogger(__name__)

version = "2.0.7"

# Start package required
prod = [
    "py4j~=0.10",
]

build = [
    "build",
    "setuptools>=42",
    "wheel",
]

doc = [
    "sphinx>=4.3",
    "sphinx_rtd_theme>=1.0",
]

test = [
    "pytest>=6.2",
    "freezegun>=1.1",
    "coverage>=6.1",
]

style = [
    "flake8>=4.0",
    "flake8-docstrings>=1.6",
    "flake8-black>=0.2",
    "isort>=5.10",
]

dev = style + test + doc + build

all_dep = prod + dev
# End package required


def read(*names, **kwargs):
    """Read file content from given file path."""
    return open(
        join(dirname(__file__), *names), encoding=kwargs.get("encoding", "utf8")
    ).read()


class CleanCommand(Command):
    """Command to clean up python api before setup by running `python setup.py pre_clean`."""

    description = "Clean up project root"
    user_options: List[str] = []
    clean_list = [
        "build",
        "htmlcov",
        "dist",
        ".pytest_cache",
        ".coverage",
    ]

    def initialize_options(self) -> None:
        """Set default values for options."""
        pass

    def finalize_options(self) -> None:
        """Set final values for options."""
        pass

    def run(self) -> None:
        """Run and remove temporary files."""
        for cl in self.clean_list:
            if not os.path.exists(cl):
                logger.info("Path %s do not exists.", cl)
            elif os.path.isdir(cl):
                remove_tree(cl)
            else:
                os.remove(cl)
        logger.info("Finish pre_clean process.")


setup(
    name="apache-dolphinscheduler",
    version=version,
    license="Apache License 2.0",
    description="Apache DolphinScheduler Python API",
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
        "Documentation": "https://dolphinscheduler.apache.org/python/index.html",
        "Source": "https://github.com/apache/dolphinscheduler/dolphinscheduler-python/pydolphinscheduler",
        "Issue Tracker": "https://github.com/apache/dolphinscheduler/issues",
        "Discussion": "https://github.com/apache/dolphinscheduler/discussions",
        "Twitter": "https://twitter.com/dolphinschedule",
    },
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    include_package_data=True,
    package_data={
        "examples": ["examples.tutorial.py"],
    },
    platforms=["any"],
    classifiers=[
        # complete classifier list: http://pypi.python.org/pypi?%3Aaction=list_classifiers
        "Development Status :: 3 - Alpha",
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
    install_requires=prod,
    extras_require={
        "all": all_dep,
        "dev": dev,
        "style": style,
        "test": test,
        "doc": doc,
        "build": build,
    },
    cmdclass={
        "pre_clean": CleanCommand,
    },
)
