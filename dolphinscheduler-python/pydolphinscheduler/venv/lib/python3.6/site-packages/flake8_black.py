"""Check Python code passes black style validation via flake8.

This is a plugin for the tool flake8 tool for checking Python
source code using the tool black.
"""

from os import path
from pathlib import Path

import black
import toml

from flake8 import utils as stdin_utils
from flake8 import LOG


__version__ = "0.2.0"

black_prefix = "BLK"


def find_diff_start(old_src, new_src):
    """Find line number and column number where text first differs."""
    old_lines = old_src.split("\n")
    new_lines = new_src.split("\n")

    for line in range(min(len(old_lines), len(new_lines))):
        old = old_lines[line]
        new = new_lines[line]
        if old == new:
            continue
        for col in range(min(len(old), len(new))):
            if old[col] != new[col]:
                return line, col
        # Difference at the end of the line...
        return line, min(len(old), len(new))
    # Difference at the end of the file...
    return min(len(old_lines), len(new_lines)), 0


class BadBlackConfig(ValueError):
    """Bad black TOML configuration file."""

    pass


def load_black_mode(toml_filename=None):
    """Load a black configuration TOML file (or return defaults) as FileMode."""
    if not toml_filename:
        return black.FileMode(
            target_versions=set(),
            line_length=black.DEFAULT_LINE_LENGTH,  # Expect to be 88
            string_normalization=True,
        )

    LOG.info("flake8-black: loading black settings from %s", toml_filename)
    try:
        pyproject_toml = toml.load(str(toml_filename))
    except toml.decoder.TomlDecodeError:
        LOG.info("flake8-black: invalid TOML file %s", toml_filename)
        raise BadBlackConfig(path.relpath(toml_filename))
    config = pyproject_toml.get("tool", {}).get("black", {})
    black_config = {k.replace("--", "").replace("-", "_"): v for k, v in config.items()}

    # Extract the fields we care about:
    return black.FileMode(
        target_versions={
            black.TargetVersion[val.upper()]
            for val in black_config.get("target_version", [])
        },
        line_length=black_config.get("line_length", black.DEFAULT_LINE_LENGTH),
        string_normalization=not black_config.get("skip_string_normalization", False),
    )


black_config = {None: load_black_mode()}  # None key's value is default config


class BlackStyleChecker:
    """Checker of Python code using black."""

    name = "black"
    version = __version__
    override_config = None

    STDIN_NAMES = {"stdin", "-", "(none)", None}

    def __init__(self, tree, filename="(none)"):
        """Initialise."""
        self.tree = tree
        self.filename = filename

    @property
    def _file_mode(self):
        """Return black.FileMode object, using local pyproject.toml as needed."""
        if self.override_config:
            return self.override_config

        # Unless using override, we look for pyproject.toml
        project_root = black.find_project_root(
            ("." if self.filename in self.STDIN_NAMES else self.filename,)
        )
        path = project_root / "pyproject.toml"

        if path in black_config:
            # Already loaded
            LOG.debug("flake8-black: %s using pre-loaded %s", self.filename, path)
            return black_config[path]
        elif path.is_file():
            # Use this pyproject.toml for this python file,
            # (unless configured with global override config)
            # This should be thread safe - does not matter even if
            # two workers load and cache this file at the same time
            black_config[path] = load_black_mode(path)
            LOG.debug("flake8-black: %s using newly loaded %s", self.filename, path)
            return black_config[path]
        else:
            # No project specific file, use default
            LOG.debug("flake8-black: %s using defaults", self.filename)
            return black_config[None]

    @classmethod
    def add_options(cls, parser):
        """Adding black-config option."""
        parser.add_option(
            "--black-config",
            metavar="TOML_FILENAME",
            default=None,
            action="store",
            # type="string",  <- breaks using None as a sentinel
            # normalize_paths=True,  <- broken and breaks None as a sentinel
            # https://gitlab.com/pycqa/flake8/issues/562
            # https://gitlab.com/pycqa/flake8/merge_requests/337
            parse_from_config=True,
            help="Path to black TOML configuration file (overrides the "
            "default 'pyproject.toml' detection; use empty string '' to mean "
            "ignore all 'pyproject.toml' files).",
        )

    @classmethod
    def parse_options(cls, optmanager, options, extra_args):
        """Adding black-config option."""
        # We have one and only one flake8 plugin configuration
        if options.black_config is None:
            LOG.info("flake8-black: No black configuration set")
            cls.override_config = None
            return
        elif not options.black_config:
            LOG.info("flake8-black: Explicitly using no black configuration file")
            cls.override_config = black_config[None]  # explicitly use defaults
            return

        # Validate the path setting - handling relative paths ourselves,
        # see https://gitlab.com/pycqa/flake8/issues/562
        black_config_path = Path(options.black_config)
        if options.config:
            # Assume black config path was via flake8 config file
            base_path = Path(path.dirname(path.abspath(options.config)))
            black_config_path = base_path / black_config_path
        if not black_config_path.is_file():
            # Want flake8 to abort, see:
            # https://gitlab.com/pycqa/flake8/issues/559
            raise ValueError(
                "Plugin flake8-black could not find specified black config file: "
                "--black-config %s" % black_config_path
            )

        # Now load the TOML file, and the black section within it
        # This configuration is to override any local pyproject.toml
        try:
            cls.override_config = black_config[black_config_path] = load_black_mode(
                black_config_path
            )
        except BadBlackConfig:
            # Could raise BLK997, but view this as an abort condition
            raise ValueError(
                "Plugin flake8-black could not parse specified black config file: "
                "--black-config %s" % black_config_path
            )

    def run(self):
        """Use black to check code style."""
        msg = None
        line = 0
        col = 0

        try:
            if self.filename in self.STDIN_NAMES:
                self.filename = "stdin"
                source = stdin_utils.stdin_get_value()
            else:
                with open(self.filename, "rb") as buf:
                    source, _, _ = black.decode_bytes(buf.read())
        except Exception as e:
            source = ""
            msg = "900 Failed to load file: %s" % e

        if not source and not msg:
            # Empty file (good)
            return
        elif source:
            # Call black...
            try:
                new_code = black.format_file_contents(
                    source, mode=self._file_mode, fast=False
                )
            except black.NothingChanged:
                return
            except black.InvalidInput:
                msg = "901 Invalid input."
            except BadBlackConfig as err:
                msg = "997 Invalid TOML file: %s" % err
            except Exception as err:
                msg = "999 Unexpected exception: %s" % err
            else:
                assert (
                    new_code != source
                ), "Black made changes without raising NothingChanged"
                line, col = find_diff_start(source, new_code)
                line += 1  # Strange as col seems to be zero based?
                msg = "100 Black would make changes."
        # If we don't know the line or column numbers, leaving as zero.
        yield line, col, black_prefix + msg, type(self)
