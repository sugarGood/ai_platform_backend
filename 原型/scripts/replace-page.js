const fs = require('fs');

const args = process.argv.slice(2);
if (args.length < 3) {
  console.error('Usage: node replace-page.js <file> <pageName> <contentFile>');
  process.exit(1);
}

const [file, pageName, contentFile] = args;
let content = fs.readFileSync(file, 'utf8');
const newHTML = fs.readFileSync(contentFile, 'utf8');

const key = `"${pageName}":`;
const keyStart = content.indexOf(key);
if (keyStart === -1) {
  console.error(`Page "${pageName}" not found in ${file}`);
  process.exit(1);
}

const valStart = content.indexOf('"', keyStart + key.length);
let pos = valStart + 1, esc = false;
for (; pos < content.length; pos++) {
  if (esc) { esc = false; continue; }
  if (content[pos] === '\\') { esc = true; continue; }
  if (content[pos] === '"') break;
}

const before = content.substring(0, valStart);
const after = content.substring(pos + 1);
const newContent = before + JSON.stringify(newHTML) + after;
fs.writeFileSync(file, newContent, 'utf8');
console.log(`[OK] Replaced "${pageName}" (${newHTML.length} chars)`);

// Verify
const vc = fs.readFileSync(file, 'utf8');
const ks2 = vc.indexOf(key);
const vs2 = vc.indexOf('"', ks2 + key.length);
let p2 = vs2 + 1; esc = false;
for (; p2 < vc.length; p2++) {
  if (esc) { esc = false; continue; }
  if (vc[p2] === '\\') { esc = true; continue; }
  if (vc[p2] === '"') break;
}
try {
  JSON.parse(vc.substring(vs2, p2 + 1));
  console.log('✅ Verification passed');
} catch(e) {
  console.error('❌ Verification failed:', e.message);
}
