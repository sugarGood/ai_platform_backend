"""Remove duplicate manual getters/setters where @Data already generates them."""
import re
from pathlib import Path


def decapitalize(s: str) -> str:
    if not s:
        return s
    return s[0].lower() + s[1:]


# One-line or braced body: return <ident>;
# Do not consume blank lines after "}" or the next getter loses its leading "\n" and won't match.
GETTER_RE = re.compile(
    r"(?:^|\r?\n)[ \t]*public[ \t]+[^{\r\n]+?[ \t]+get(\w+)\([ \t]*\)[ \t]*\{[ \t\r\n]*return[ \t]+(\w+)[ \t]*;[ \t\r\n]*\}",
    re.MULTILINE,
)

SETTER_RE = re.compile(
    r"(?:^|\r?\n)[ \t]*public[ \t]+void[ \t]+(set\w+)\([^)]*\)[ \t]*\{[ \t\r\n]*this\.(\w+)[ \t]*=[ \t]*(\w+)[ \t]*;[ \t\r\n]*\}",
    re.MULTILINE,
)


def strip_duplicate_accessors(text: str) -> str:
    def repl_get(m):
        # Regex group is the suffix after "get", e.g. "Id" / "FullName"
        suffix = m.group(1)
        ret_field = m.group(2)
        prop = decapitalize(suffix)
        if ret_field == prop:
            return "\n"
        return m.group(0)

    prev = None
    while prev != text:
        prev = text
        text = GETTER_RE.sub(repl_get, text)

    def repl_set(m):
        method = m.group(1)
        lhs = m.group(2)
        rhs = m.group(3)
        prop = decapitalize(method[3:]) if method.startswith("set") and len(method) > 3 else None
        if prop is not None and lhs == prop and rhs == prop:
            return "\n"
        return m.group(0)

    prev = None
    while prev != text:
        prev = text
        text = SETTER_RE.sub(repl_set, text)

    return re.sub(r"\n{3,}", "\n\n", text)


def main():
    roots = [
        Path("ai-platform-server/src/main/java/com/aiplatform/backend/entity"),
        Path("ai-platform-agent/src/main/java/com/aiplatform/agent/gateway/entity"),
    ]
    for root in roots:
        for path in sorted(root.glob("*.java")):
            text = path.read_text(encoding="utf-8")
            if "lombok.Data" not in text:
                continue
            new_text = strip_duplicate_accessors(text)
            if new_text != text:
                path.write_text(new_text, encoding="utf-8")
                print("fixed", path)


if __name__ == "__main__":
    main()
