# One-off: add @Data @Builder @NoArgsConstructor @AllArgsConstructor; remove standard accessors.
import re
from pathlib import Path


def decapitalize(s: str) -> str:
    if not s:
        return s
    return s[0].lower() + s[1:]


def property_from_getter(method_name: str):
    if method_name.startswith("get") and len(method_name) > 3:
        return decapitalize(method_name[3:])
    return None


LOMBOK_BLOCK = """import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

"""


def strip_standard_accessors(text: str) -> str:
    def repl_get(m):
        method = m.group(2)
        body = m.group(3).strip()
        prop = property_from_getter(method)
        if prop is None:
            return m.group(0)
        rm = re.match(r"return\s+(\w+)\s*;", body)
        if not rm:
            return m.group(0)
        if rm.group(1) == prop:
            return "\n"
        return m.group(0)

    text = re.sub(
        r"\n    public ([^{]+) get(\w+)\(\)\s*\{\s*([^}]*)\}",
        repl_get,
        text,
    )

    def repl_set(m):
        method = m.group(1)
        prop = (
            decapitalize(method[3:])
            if method.startswith("set") and len(method) > 3
            else None
        )
        if prop is None:
            return m.group(0)
        body = m.group(3).strip()
        if body == f"this.{prop} = {prop};":
            return "\n"
        return m.group(0)

    text = re.sub(
        r"\n    public void (set\w+)\(([^)]*)\)\s*\{\s*([^}]*)\}",
        repl_set,
        text,
    )
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text


def transform_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    if "import lombok.Data;" in text:
        return False
    if "public class" not in text:
        return False

    lines = text.split("\n")
    last_imp = -1
    for i, ln in enumerate(lines):
        if ln.strip().startswith("import "):
            last_imp = i
    if last_imp < 0:
        return False
    lines.insert(last_imp + 1, LOMBOK_BLOCK.rstrip("\n"))
    text = "\n".join(lines)

    if "@TableName" in text:
        text = re.sub(
            r"(\n)(@TableName\()",
            r"\1@Data\n@Builder\n@NoArgsConstructor\n@AllArgsConstructor\n\2",
            text,
            count=1,
        )
    else:
        text = re.sub(
            r"(\n)(public class \w+)",
            r"\1@Data\n@Builder\n@NoArgsConstructor\n@AllArgsConstructor\n\2",
            text,
            count=1,
        )

    text = strip_standard_accessors(text)
    path.write_text(text, encoding="utf-8")
    return True


def main():
    roots = [
        Path("ai-platform-server/src/main/java/com/aiplatform/backend/entity"),
        Path("ai-platform-agent/src/main/java/com/aiplatform/agent/gateway/entity"),
    ]
    n = 0
    for root in roots:
        for p in sorted(root.glob("*.java")):
            if transform_file(p):
                n += 1
                print("OK", p)
    print("Total", n)


if __name__ == "__main__":
    main()
