"""
Builds docs/workshop-guide.html from docs/workshop-guide.md.

    pip install markdown pygments
    python docs/generator/build_guide.py

The markdown file is the source of truth and renders fine on GitHub as-is.
This script wraps it in the self-contained HTML shell (template.html):
sidebar navigation generated from the headings, step badges, GitHub-style
alerts turned into callouts, syntax-highlighted code blocks, image containers
with captions and a lightbox.
"""
import html
import re
from pathlib import Path

import markdown
from pygments import lex
from pygments.lexers import get_lexer_by_name
from pygments.token import Token
from pygments.util import ClassNotFound

DOCS = Path(__file__).resolve().parent.parent
SOURCE = DOCS / "workshop-guide.md"
TEMPLATE = DOCS / "generator" / "template.html"
OUTPUT = DOCS / "workshop-guide.html"

TITLE = "Caching in Java"
SUBTITLE = "From HashMap to Redis - SKG Java Meetup, Thessaloniki"
DESCRIPTION = ("Hands-on workshop guide: caching in Java from a HashMap to Caffeine, "
               "Redis and Spring Cache (@Cacheable), with invalidation and failure modes.")

# Short sidebar labels per step (the h2 text itself is longer)
STEP_LABELS = {
    0: "Scaffold",
    1: "Naive Cache",
    2: "Caffeine",
    3: "Redis",
    4: "Spring Cache",
    5: "Invalidation",
    6: "Beyond the Demo",
}
H2_IDS = {"Introduction": "intro", "Wrap-up": "wrapup"}

# Short sidebar labels for long h3 headings (key: the heading's plain text)
H3_LABELS = {
    "The Domain: One Product, One Slow Repository": "The Domain",
    "What Is Wrong With the HashMap Cache?": "What Is Wrong?",
    "A Slightly Better Naive Cache: ConcurrentHashMap": "ConcurrentHashMap",
    "Add the Dependency": "Dependency",
    "Caffeine Instead of ConcurrentHashMap": "Basic Usage",
    "Eviction Listener and Statistics": "Eviction & Stats",
    "LoadingCache and refreshAfterWrite": "LoadingCache & Refresh",
    "Shared Cache: Advantages and Costs": "Advantages & Costs",
    "Start Redis with Docker Compose": "Docker Compose",
    "Redis Needs a Serializer": "Serializer",
    "A Realistic Repository": "Realistic Repository",
    "Redis Operations Cheat Sheet": "Cheat Sheet",
    "Do We Really Want This Boilerplate?": "Boilerplate?",
    "Enable Caching and Prove Which Provider Is Active": "Enable Caching",
    "The Reveal: The Service Is One Line Again": "The Reveal",
    "What Happens Behind @Cacheable": "Behind @Cacheable",
    "Same Annotation, Three Providers": "Three Providers",
    "Spring Boot 4 Gotchas": "Boot 4 Gotchas",
    "Optional: Seeing Every Hit and Miss": "Hits & Misses (optional)",
    "An Update Endpoint Without Cache Annotations": "Update Endpoint",
    "The Self-Invocation Trap": "Self-Invocation Trap",
}

CALLOUTS = {
    "NOTE": ("note", "&#128221;", "Note"),
    "TIP": ("tip", "&#128161;", "Tip"),
    "WARNING": ("warning", "&#9888;&#65039;", "Warning"),
    "IMPORTANT": ("important", "&#9888;&#65039;", "Important"),
}

# ---------------------------------------------------------------- code


def token_class(ttype):
    if ttype in Token.Comment:
        return "cm"
    if ttype in Token.String:
        return "str"
    if ttype in Token.Name.Decorator:
        return "ann"
    if ttype in Token.Keyword.Type or ttype in Token.Name.Class:
        return "typ"
    if ttype in Token.Keyword or ttype in Token.Name.Builtin:
        return "kw"
    if ttype in Token.Name.Function:
        return "fn"
    if ttype in Token.Number:
        return "num"
    if ttype in Token.Name.Tag:
        return "tag"
    if ttype in Token.Name.Attribute:
        return "attr"
    if ttype in Token.Name.Variable:
        return "prop"
    return None


def highlight(code, lang):
    code = code.rstrip("\n")
    lexer = None
    if lang and lang not in ("text", "txt", ""):
        try:
            lexer = get_lexer_by_name(lang, stripnl=False)
        except ClassNotFound:
            lexer = None
    if lexer is None:
        return html.escape(code)
    out = []
    for ttype, value in lex(code, lexer):
        cls = token_class(ttype)
        escaped = html.escape(value)
        out.append(f'<span class="{cls}">{escaped}</span>' if cls else escaped)
    return "".join(out).rstrip("\n")


# ---------------------------------------------------------------- pipeline

class Converter:
    def __init__(self):
        self.blocks = {}   # placeholder -> html

    def _placeholder(self, kind, content):
        key = f"%%{kind}_{len(self.blocks)}%%"
        self.blocks[key] = content
        return key

    def extract_callouts(self, text):
        lines = text.split("\n")
        out, i = [], 0
        while i < len(lines):
            m = re.match(r"^> \[!(NOTE|TIP|WARNING|IMPORTANT)\]\s*$", lines[i])
            if not m:
                out.append(lines[i])
                i += 1
                continue
            kind = m.group(1)
            body = []
            i += 1
            while i < len(lines) and lines[i].startswith(">"):
                body.append(re.sub(r"^> ?", "", lines[i]))
                i += 1
            cls, icon, default_label = CALLOUTS[kind]
            label = default_label
            # "**Title.** rest" on the first line becomes the callout label
            if body:
                t = re.match(r"^\*\*(.+?)\.?\*\*\s*(.*)$", body[0])
                if t:
                    label = t.group(1)
                    body[0] = t.group(2)
            inner = self.convert("\n".join(body).strip("\n"))
            block = (f'<div class="callout {cls}">\n'
                     f'  <div class="callout-label">{icon} {html.escape(label)}</div>\n'
                     f'{inner}\n</div>')
            out.append("")
            out.append(self._placeholder("CALLOUT", block))
            out.append("")
        return "\n".join(out)

    def extract_fences(self, text):
        def repl(m):
            lang = (m.group(1) or "").strip().lower()
            block = f"<pre><code>{highlight(m.group(2), lang)}</code></pre>"
            return "\n" + self._placeholder("CODE", block) + "\n"
        return re.sub(r"^```([\w+-]*)[ \t]*\n(.*?)^```[ \t]*$", repl, text,
                      flags=re.S | re.M)

    def convert(self, text):
        text = self.extract_callouts(text)
        text = self.extract_fences(text)
        md = markdown.Markdown(extensions=["tables", "smarty"],
                               extension_configs={"smarty": {"smart_quotes": False,
                                                             "smart_angled_quotes": False}})
        out = md.convert(text)
        return out

    def resolve(self, out):
        # placeholders may be wrapped in <p> by markdown
        pattern = re.compile(r"(?:<p>)?(%%(?:CODE|CALLOUT)_\d+%%)(?:</p>)?")
        while pattern.search(out):
            out = pattern.sub(lambda m: self.blocks[m.group(1)], out)
        return out


def slug(text):
    text = re.sub(r"<[^>]+>", "", text)
    text = re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")
    return text


def short_label(text):
    text = html.unescape(re.sub(r"<[^>]+>", "", text)).strip()
    if text in H3_LABELS:
        return H3_LABELS[text]
    text = re.split(r"\s+--\s+|\s+–\s+|\s\(", text)[0]
    return text.strip()


def add_ids_and_sidebar(body):
    """Assign ids to h2/h3, add step badges, build the sidebar HTML."""
    nav = []          # list of (h2_id, label, [(h3_id, label), ...])
    current = None
    used = set()

    def unique(i):
        base, n = i, 2
        while i in used:
            i = f"{base}-{n}"
            n += 1
        used.add(i)
        return i

    def h2(m):
        nonlocal current
        text = m.group(2)
        plain = html.unescape(re.sub(r"<[^>]+>", "", text))
        step = re.match(r"Step (\d+):\s*(.*)", plain)
        if step:
            n = int(step.group(1))
            hid = unique(f"step{n}")
            label = f"Step {n} - {STEP_LABELS.get(n, short_label(step.group(2)))}"
            rest = text.split(":", 1)[1].strip()
            heading = f'<h2 id="{hid}"><span class="step-badge">Step {n}</span> {rest}</h2>'
        else:
            hid = unique(H2_IDS.get(plain, slug(plain)))
            label = plain
            heading = f'<h2 id="{hid}">{text}</h2>'
        current = (hid, label, [])
        nav.append(current)
        return heading

    def h3(m):
        text = m.group(2)
        plain = html.unescape(re.sub(r"<[^>]+>", "", text))
        base = current[0] if current else "s"
        hid = unique(f"{base}-{slug(short_label(plain))}")
        if current:
            current[2].append((hid, short_label(plain)))
        return f'<h3 id="{hid}">{text}</h3>'

    def heading(m):
        return h2(m) if m.group(1) == "2" else h3(m)

    # one pass, in document order, so h3 ids are prefixed with their own h2
    body = re.sub(r"<h([23])>(.*?)</h\1>", heading, body)

    lines = ["    <ul>"]
    for hid, label, subs in nav:
        if subs:
            lines.append("      <li>")
            lines.append(f'        <a class="nav-link" data-toggle="{hid}-sub">{html.escape(label)} '
                         f'<span class="expand-arrow">&#9654;</span></a>')
            lines.append(f'        <ul class="sub-nav" id="{hid}-sub">')
            lines.append(f'          <li><a href="#{hid}" class="nav-link">Overview</a></li>')
            for sid, slabel in subs:
                lines.append(f'          <li><a href="#{sid}" class="nav-link">{html.escape(slabel)}</a></li>')
            lines.append("        </ul>")
            lines.append("      </li>")
        else:
            lines.append(f'      <li><a href="#{hid}" class="nav-link">{html.escape(label)}</a></li>')
    lines.append('      <li><a href="#community" class="nav-link">Get Involved</a></li>')
    lines.append("    </ul>")
    return body, "\n".join(lines)


def post_process(body):
    # images -> figure container with caption (alt text), lightbox-ready
    def img(m):
        alt, src = m.group(1), m.group(2)
        small = ' small' if 'icon' in src or 'small' in src else ''
        return (f'<div class="img-container{small}">\n'
                f'  <img src="{src}" alt="{alt}">\n'
                f'  <div class="img-caption">{alt}</div>\n</div>')
    body = re.sub(r'<p><img alt="([^"]*)" src="([^"]*)"\s*/?></p>', img, body)
    # tables -> horizontally scrollable wrapper
    body = body.replace("<table>", '<div class="table-wrap"><table>').replace("</table>", "</table></div>")
    # markdown 'smarty' leaves "--" inside <code>; plain text gets an en dash, which we want as " - "
    body = body.replace("&ndash;", "-")
    return body


def main():
    text = SOURCE.read_text(encoding="utf-8")
    # drop the top-level title and the "SKG Java Meetup" line: the header carries them
    text = re.sub(r"^# .*\n+\*\*SKG Java Meetup[^\n]*\n+---\n", "", text, count=1)

    conv = Converter()
    body = conv.resolve(conv.convert(text))
    body, sidebar = add_ids_and_sidebar(body)
    body = post_process(body)

    page = TEMPLATE.read_text(encoding="utf-8")
    page = (page.replace("{{TITLE}}", TITLE)
                .replace("{{SUBTITLE}}", SUBTITLE)
                .replace("{{DESCRIPTION}}", DESCRIPTION)
                .replace("{{SIDEBAR}}", sidebar)
                .replace("{{CONTENT}}", body))
    OUTPUT.write_text(page, encoding="utf-8", newline="\n")

    images = sorted(set(re.findall(r'src="(images/[^"]+)"', page)))
    missing = [i for i in images if not (DOCS / i).exists()]
    print(f"wrote {OUTPUT.relative_to(DOCS.parent)} ({page.count(chr(10))} lines)")
    print(f"images referenced: {len(images)}, missing: {len(missing)}")
    for i in missing:
        print("  missing:", i)


if __name__ == "__main__":
    main()
