"""
Translate all Markdown files under `docs/docs/en` into Korean copies under `docs/docs/ko`,
preserving the directory structure.

## Translation backends

### A) Google Cloud Translation API (recommended)

1) Install dependency:
   - `pip install google-cloud-translate`

2) Set environment variables (one of the following auth methods):
   - Service account (recommended):
     - `set GOOGLE_APPLICATION_CREDENTIALS=C:\\path\\to\\service-account.json`
     - `set GOOGLE_CLOUD_PROJECT=your-gcp-project-id`
   - Or otherwise ensure ADC works and `GOOGLE_CLOUD_PROJECT` is set.

3) Run:
   - `python docs\\tools\\translate_en_to_ko.py`

### B) googletrans (unofficial; may be rate-limited / unstable)

1) Install dependency:
   - `pip install googletrans==4.0.0-rc1`

2) Run:
   - `python docs\\tools\\translate_en_to_ko.py --backend googletrans`

## Safety

By default the script **does not overwrite** existing `docs/docs/ko/**/*.md` files
(to avoid losing manual translations). Use `--overwrite` to force regeneration.
"""

from __future__ import annotations

import argparse
import os
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Iterator, Literal, Sequence


Backend = Literal["gcp", "googletrans"]


def iter_markdown_files(root: Path) -> Iterable[Path]:
    return root.rglob("*.md")


def _split_keep_fences(markdown: str) -> list[tuple[str, bool]]:
    """
    Split markdown into blocks while preserving fenced code blocks.
    Returns list of (text, should_translate) blocks.
    """
    lines = markdown.splitlines(keepends=True)
    blocks: list[tuple[str, bool]] = []

    buf: list[str] = []
    in_fence = False
    fence = "```"

    def flush(should_translate: bool) -> None:
        nonlocal buf
        if not buf:
            return
        blocks.append(("".join(buf), should_translate))
        buf = []

    for line in lines:
        stripped = line.lstrip()
        if stripped.startswith(fence):
            # include the fence line in the "do not translate" part
            flush(should_translate=not in_fence)
            buf.append(line)
            in_fence = not in_fence
            continue

        buf.append(line)

    flush(should_translate=not in_fence)
    return blocks


def _chunk_text(text: str, max_chars: int) -> Iterator[str]:
    """
    Yield chunks <= max_chars, attempting to split on paragraph boundaries.
    """
    if len(text) <= max_chars:
        yield text
        return

    parts = text.split("\n\n")
    buf: list[str] = []
    size = 0
    sep = "\n\n"

    def flush() -> None:
        nonlocal buf, size
        if buf:
            yield_text = sep.join(buf)
            buf = []
            size = 0
            return yield_text
        return ""

    for p in parts:
        add_len = len(p) + (len(sep) if buf else 0)
        if add_len > max_chars and not buf:
            # paragraph itself too large -> hard split
            start = 0
            while start < len(p):
                yield p[start : start + max_chars]
                start += max_chars
            continue

        if size + add_len > max_chars:
            out = flush()
            if out:
                yield out
        buf.append(p)
        size += add_len

    out = flush()
    if out:
        yield out


@dataclass(frozen=True)
class TranslateConfig:
    backend: Backend
    project_id: str | None = None
    location: str = "global"
    max_chars_per_request: int = 4000
    sleep_seconds_googletrans: float = 0.2


def _translate_chunks_gcp(chunks: Sequence[str], cfg: TranslateConfig) -> list[str]:
    try:
        from google.cloud import translate_v3 as translate  # type: ignore
    except Exception as e:  # pragma: no cover
        raise SystemExit(
            "google-cloud-translate is not installed. Install it with:\n"
            "  pip install google-cloud-translate\n"
            f"Original error: {e}"
        )

    project_id = cfg.project_id or os.environ.get("GOOGLE_CLOUD_PROJECT")
    if not project_id:
        raise SystemExit(
            "Missing GCP project id. Set env var GOOGLE_CLOUD_PROJECT or pass --project-id."
        )

    client = translate.TranslationServiceClient()
    parent = f"projects/{project_id}/locations/{cfg.location}"

    response = client.translate_text(
        request={
            "parent": parent,
            "contents": list(chunks),
            "mime_type": "text/plain",
            "source_language_code": "en",
            "target_language_code": "ko",
        }
    )
    return [t.translated_text for t in response.translations]


def _translate_chunks_googletrans(
    chunks: Sequence[str], cfg: TranslateConfig
) -> list[str]:
    try:
        from googletrans import Translator  # type: ignore
    except Exception as e:  # pragma: no cover
        raise SystemExit(
            "googletrans is not installed. Install it with:\n"
            "  pip install googletrans==4.0.0-rc1\n"
            f"Original error: {e}"
        )

    translator = Translator()
    out: list[str] = []
    for c in chunks:
        if not c.strip():
            out.append(c)
            continue
        # Be gentle to avoid temporary bans / 429s
        time.sleep(cfg.sleep_seconds_googletrans)
        # out.append(translator.translate(c, src="en", dest="ko").text)
        try:
            result = translator.translate(c, src="en", dest="ko")
            text = getattr(result, "text", None)
            if not isinstance(text, str):
                raise TypeError("googletrans returned empty text")
            out.append(text)
        except Exception as e:  # pragma: no cover
            # googletrans가 종종 응답 포맷을 바꾸거나 None을 반환해서
            # JSON 파싱 오류가 날 수 있으므로, 오류 시에는 원문을 그대로 유지한다.
            print(f"[googletrans] 번역 실패, 원문 유지: {e}")
            out.append(c)
    return out


def translate_markdown_en_to_ko(markdown: str, cfg: TranslateConfig) -> str:
    blocks = _split_keep_fences(markdown)
    rendered: list[str] = []

    for text, should_translate in blocks:
        if not should_translate or not text.strip():
            rendered.append(text)
            continue

        chunks = list(_chunk_text(text, max_chars=cfg.max_chars_per_request))
        if cfg.backend == "gcp":
            translated_chunks = _translate_chunks_gcp(chunks, cfg)
        else:
            translated_chunks = _translate_chunks_googletrans(chunks, cfg)
        rendered.append("".join(translated_chunks))

    return "".join(rendered)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--backend",
        choices=["gcp", "googletrans"],
        default="gcp",
        help="Translation backend: gcp (Cloud Translation API) or googletrans.",
    )
    parser.add_argument(
        "--project-id",
        default=None,
        help="GCP project id (if using --backend gcp). Otherwise uses GOOGLE_CLOUD_PROJECT.",
    )
    parser.add_argument(
        "--location",
        default="global",
        help="GCP location (default: global).",
    )
    parser.add_argument(
        "--overwrite",
        action="store_true",
        help="Overwrite existing ko files (default: skip if destination exists).",
    )
    parser.add_argument(
        "--max-chars",
        type=int,
        default=4000,
        help="Max chars per translation request chunk.",
    )
    args = parser.parse_args()

    cfg = TranslateConfig(
        backend=args.backend,
        project_id=args.project_id,
        location=args.location,
        max_chars_per_request=args.max_chars,
    )

    script_path = Path(__file__).resolve()
    docs_root = script_path.parents[1]  # <repo_root>/docs

    src_root = docs_root / "docs" / "en"
    dst_root = docs_root / "docs" / "ko"

    if not src_root.exists():
        raise SystemExit(f"Source docs directory not found: {src_root}")

    translated_count = 0
    skipped_count = 0

    for src in iter_markdown_files(src_root):
        rel = src.relative_to(src_root)
        dst = dst_root / rel
        dst.parent.mkdir(parents=True, exist_ok=True)

        if dst.exists() and not args.overwrite:
            skipped_count += 1
            continue

        src_text = src.read_text(encoding="utf-8")
        ko_text = translate_markdown_en_to_ko(src_text, cfg)

        dst.write_text(ko_text, encoding="utf-8")
        translated_count += 1
        print(f"Translated {src} -> {dst}")

    print(f"Done. Translated: {translated_count}, skipped(existing): {skipped_count}")


if __name__ == "__main__":
    main()

