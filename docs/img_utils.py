#!/usr/bin/env python

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

"""Utils for documentation's images."""

import argparse
import logging
import re
from pathlib import Path
from typing import Set, Tuple

log = logging.getLogger(__file__)
log.addHandler(logging.StreamHandler())

root_dir: Path = Path(__file__).parent
img_dir: Path = root_dir.joinpath("img")
doc_dir: Path = root_dir.joinpath("docs")
dev_en_dir: Path = doc_dir.joinpath("en", "development")
dev_zh_dir: Path = doc_dir.joinpath("zh", "development")


def get_files_recurse(path: Path) -> Set:
    """Get all files recursively from given :param:`path`."""
    res = set()
    for p in path.rglob("*"):
        if p.is_dir():
            continue
        res.add(p)
    return res


def get_paths_uniq_suffix(paths: Set[Path]) -> Set:
    """Get file suffix without dot in given :param:`paths`."""
    res = set()
    for path in paths:
        if path.suffix == "":
            log.warning("There is a path %s without suffix.", path)
        res.add(path.suffix[1:])
    return res


def get_paths_rel_path(paths: Set[Path], rel: Path) -> Set:
    """Get files relative path to :param:`rel` with ``/`` prefix from given :param:`paths`."""
    return {f"/{path.relative_to(rel)}" for path in paths}


def get_docs_img_path(paths: Set[Path]) -> Set:
    """Get all img syntax from given :param:`paths` using the regexp from :param:`pattern`."""
    res = set()
    pattern = re.compile(r"../img[\w./-]+")
    for path in paths:
        content = path.read_text()
        find = pattern.findall(content)
        if find:
            res |= {item.lstrip(".") for item in find}
    return res


def del_rel_path(paths: Set[str]) -> None:
    """Delete all relative :param:`paths` from current root/docs directory."""
    for path in paths:
        log.debug("Deleting file in the path %s", path)
        root_dir.joinpath(path.lstrip("/")).unlink()


def del_empty_dir_recurse(path: Path) -> None:
    """Delete all empty directory recursively from given :param:`paths`."""
    for p in path.rglob("*"):
        if p.is_dir() and not any(p.iterdir()):
            log.debug("Deleting directory in the path %s", p)
            p.rmdir()


def diff_two_set(first: Set, second: Set) -> Tuple[set, set]:
    """Get two set difference tuple.

    :return: Tuple[(first - second), (second - first)]
    """
    return first.difference(second), second.difference(first)


def check_diff_img() -> Tuple[set, set]:
    """Check images difference files.

    :return: Tuple[(in_docs - in_img_dir), (in_img_dir - in_docs)]
    """
    img = get_files_recurse(img_dir)
    docs = get_files_recurse(doc_dir)
    img_rel_path = get_paths_rel_path(img, root_dir)
    docs_rel_path = get_docs_img_path(docs)
    return diff_two_set(docs_rel_path, img_rel_path)


def check() -> None:
    """Runner for `check` sub command."""
    img_docs, img_img = check_diff_img()
    assert not img_docs and not img_img, (
        f"Images assert failed: \n"
        f"* Some images use in documents but do not exists in `img` directory, please add them: "
        f"{img_docs if img_docs else 'None'}\n"
        f"* Some images not use in documents but exists in `img` directory, please delete them: "
        f"{img_img if img_img else 'None'}\n"
    )


def prune() -> None:
    """Runner for `prune` sub command."""
    _, img_img = check_diff_img()
    del_rel_path(img_img)
    del_empty_dir_recurse(img_dir)


def dev_syntax() -> None:
    """Check whether directory development contain do not support syntax or not.

    * It should not ref document from other document in `docs` directory
    """
    pattern = re.compile("(\\(\\.\\.[\\w./-]+\\.md\\))")
    dev_files_path = get_files_recurse(dev_en_dir) | get_files_recurse(dev_zh_dir)
    get_files_recurse(dev_en_dir)
    for path in dev_files_path:
        content = path.read_text()
        find = pattern.findall(content)
        assert (
            not find
        ), f"File {str(path)} contain temporary not support syntax: {find}."


def build_argparse() -> argparse.ArgumentParser:
    """Build argparse.ArgumentParser with specific configuration."""
    parser = argparse.ArgumentParser(prog="img_utils")
    parser.add_argument(
        "-v",
        "--verbose",
        dest="log_level",
        action="store_const",
        const=logging.DEBUG,
        default=logging.INFO,
        help="Show verbose or not.",
    )

    subparsers = parser.add_subparsers(
        title="subcommands",
        dest="subcommand",
        help="Choose one of the subcommand you want to run.",
    )
    parser_check = subparsers.add_parser(
        "check", help="Check whether invalid or missing img exists."
    )
    parser_check.set_defaults(func=check)

    parser_prune = subparsers.add_parser(
        "prune", help="Remove img in directory `img` but not use in directory `docs`."
    )
    parser_prune.set_defaults(func=prune)

    parser_prune = subparsers.add_parser(
        "dev-syntax",
        help="Check whether temporary does not support syntax in development directory.",
    )
    parser_prune.set_defaults(func=dev_syntax)

    # TODO Add subcommand `reorder`
    return parser


if __name__ == "__main__":
    arg_parser = build_argparse()
    args = arg_parser.parse_args()

    # args = arg_parser.parse_args(["check"])
    log.setLevel(args.log_level)
    if args.log_level <= logging.DEBUG:
        print("All args is:", args)
    args.func()
