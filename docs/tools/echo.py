from __future__ import annotations

import argparse
import os
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Iterator, Literal, Sequence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("text", type=str, help="Text to echo")
    args = parser.parse_args()
    print(args.text)


if __name__ == "__main__":
    main()