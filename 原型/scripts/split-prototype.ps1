$ErrorActionPreference = 'Stop'

function Write-Utf8File {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)][string]$Content
  )

  $directory = Split-Path -Parent $Path
  if ($directory -and -not (Test-Path $directory)) {
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
  }

  [System.IO.File]::WriteAllText($Path, $Content, [System.Text.UTF8Encoding]::new($false))
}

function Get-TopLevelDivBlocks {
  param(
    [Parameter(Mandatory = $true)][string]$Html,
    [Parameter(Mandatory = $true)][string]$StartPattern
  )

  $blocks = @()
  $cursor = 0

  while ($true) {
    $match = [regex]::Match($Html.Substring($cursor), $StartPattern)
    if (-not $match.Success) {
      break
    }

    $start = $cursor + $match.Index
    $id = $match.Groups[1].Value
    $depth = 0
    $position = $start

    while ($position -lt $Html.Length) {
      $openIndex = $Html.IndexOf('<div', $position, [System.StringComparison]::OrdinalIgnoreCase)
      $closeIndex = $Html.IndexOf('</div>', $position, [System.StringComparison]::OrdinalIgnoreCase)

      if ($closeIndex -lt 0) {
        throw "Unbalanced div block for $id"
      }

      if ($openIndex -ge 0 -and $openIndex -lt $closeIndex) {
        $depth += 1
        $position = $openIndex + 4
        continue
      }

      $depth -= 1
      $position = $closeIndex + 6

      if ($depth -eq 0) {
        $blocks += [pscustomobject]@{
          Id = $id
          Html = $Html.Substring($start, $position - $start).Trim()
        }
        $cursor = $position
        break
      }
    }
  }

  return $blocks
}

function Convert-BlocksToJsObject {
  param(
    [Parameter(Mandatory = $true)][System.Collections.IEnumerable]$Blocks
  )

  $ordered = [ordered]@{}
  foreach ($block in $Blocks) {
    $ordered[$block.Id] = $block.Html
  }

  return ($ordered | ConvertTo-Json -Depth 8)
}

$root = Split-Path -Parent $PSScriptRoot
$sourceFile = Join-Path $root 'ai-platform-prototype.html'
$archiveDir = Join-Path $root 'archive'
$archiveFile = Join-Path $archiveDir 'ai-platform-prototype.single-file.html'

if (-not (Test-Path $sourceFile)) {
  throw "Source file not found: $sourceFile"
}

$content = [System.IO.File]::ReadAllText($sourceFile, [System.Text.UTF8Encoding]::new($false))

$titleMatch = [regex]::Match($content, '(?s)<title>(.*?)</title>')
$styleMatch = [regex]::Match($content, '(?s)<style>\s*(.*?)\s*</style>')
$bodyMatch = [regex]::Match($content, '(?s)<body>\s*(.*?)\s*<script>')
$scriptMatch = [regex]::Match($content, '(?s)<script>\s*(.*?)\s*</script>\s*</body>')

if (-not $titleMatch.Success -or -not $styleMatch.Success -or -not $bodyMatch.Success -or -not $scriptMatch.Success) {
  throw 'Could not extract title/style/body/script from the original prototype.'
}

$title = $titleMatch.Groups[1].Value.Trim()
$styles = $styleMatch.Groups[1].Value.Trim()
$body = $bodyMatch.Groups[1].Value
$script = $scriptMatch.Groups[1].Value.Trim()

$mainStart = $body.IndexOf('<div class="main">', [System.StringComparison]::OrdinalIgnoreCase)
$firstPageStart = $body.IndexOf('<!-- ========== DASHBOARD ========== -->', [System.StringComparison]::OrdinalIgnoreCase)
$firstModalStart = $body.IndexOf('<div class="modal"', [System.StringComparison]::OrdinalIgnoreCase)
$toastStart = $body.IndexOf('<div class="toast-stack"', [System.StringComparison]::OrdinalIgnoreCase)

if ($mainStart -lt 0 -or $firstPageStart -lt 0 -or $firstModalStart -lt 0 -or $toastStart -lt 0) {
  throw 'Could not identify the main shell, pages, or modal regions.'
}

$globalSidebar = $body.Substring(
  $body.IndexOf('<!-- Global Sidebar -->', [System.StringComparison]::OrdinalIgnoreCase),
  $body.IndexOf('<!-- Project Sidebar', [System.StringComparison]::OrdinalIgnoreCase) - $body.IndexOf('<!-- Global Sidebar -->', [System.StringComparison]::OrdinalIgnoreCase)
).Trim()

$projectSidebar = $body.Substring(
  $body.IndexOf('<!-- Project Sidebar', [System.StringComparison]::OrdinalIgnoreCase),
  $body.IndexOf('<!-- Main Content -->', [System.StringComparison]::OrdinalIgnoreCase) - $body.IndexOf('<!-- Project Sidebar', [System.StringComparison]::OrdinalIgnoreCase)
).Trim()

$mainOpen = $body.Substring($mainStart, $firstPageStart - $mainStart).TrimEnd()
$modalsAndToast = $body.Substring($firstModalStart, ($toastStart - $firstModalStart)).TrimEnd() + "`r`n`r`n" + $body.Substring($toastStart).Trim()
$pagesRegion = $body.Substring($firstPageStart, $firstModalStart - $firstPageStart)

$pageBlocks = Get-TopLevelDivBlocks -Html $pagesRegion -StartPattern '<div class="page(?: active)?" id="([^"]+)">'

if (-not $pageBlocks.Count) {
  throw 'No page blocks were extracted from the original prototype.'
}

$platformPages = @()
$projectPages = @()
$personalPages = @()

foreach ($page in $pageBlocks) {
  if ($page.Id -like 'page-my-*') {
    $personalPages += $page
    continue
  }

  if ($page.Id -like 'page-proj-*' -or $page.Id -eq 'page-svc-detail') {
    $projectPages += $page
    continue
  }

  $platformPages += $page
}

$pagesDir = Join-Path $root 'assets\js\templates'
$cssDir = Join-Path $root 'assets\css'
$appDir = Join-Path $root 'assets\js'

if (-not (Test-Path $archiveDir)) {
  New-Item -ItemType Directory -Path $archiveDir -Force | Out-Null
}

if (-not (Test-Path $archiveFile)) {
  [System.IO.File]::Copy($sourceFile, $archiveFile)
}

$platformJson = Convert-BlocksToJsObject -Blocks $platformPages
$projectJson = Convert-BlocksToJsObject -Blocks $projectPages
$personalJson = Convert-BlocksToJsObject -Blocks $personalPages

$platformJs = @"
window.PrototypePages = Object.assign(window.PrototypePages || {}, $platformJson);
"@.Trim() + "`r`n"

$projectJs = @"
window.PrototypePages = Object.assign(window.PrototypePages || {}, $projectJson);
"@.Trim() + "`r`n"

$personalJs = @"
window.PrototypePages = Object.assign(window.PrototypePages || {}, $personalJson);
"@.Trim() + "`r`n"

$appJs = @"
(function () {
  var pageRoot = document.getElementById('page-root');
  var pages = window.PrototypePages || {};

  if (pageRoot) {
    pageRoot.innerHTML = Object.keys(pages).map(function (key) {
      return pages[key];
    }).join('\n\n');
  }

$script
})();
"@.Trim() + "`r`n"

$indexHtml = @"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$title</title>
  <link rel="stylesheet" href="assets/css/prototype.css">
</head>
<body>
$globalSidebar

$projectSidebar

$mainOpen
    <div id="page-root"></div>
  </div>
</div>

$modalsAndToast

<script src="assets/js/templates/personal-pages.js"></script>
<script src="assets/js/templates/platform-pages.js"></script>
<script src="assets/js/templates/project-pages.js"></script>
<script src="assets/js/app.js"></script>
</body>
</html>
"@.Trim() + "`r`n"

$guide = @(
  '# Prototype Structure',
  '',
  '## Files',
  '- `ai-platform-prototype.html`: shell only',
  '- `assets/css/prototype.css`: all styles',
  '- `assets/js/templates/platform-pages.js`: platform pages',
  '- `assets/js/templates/project-pages.js`: project pages',
  '- `assets/js/templates/personal-pages.js`: personal pages',
  '- `assets/js/app.js`: interactions and mock data',
  '- `archive/ai-platform-prototype.single-file.html`: original backup',
  '',
  '## Edit Guide',
  '- Update page structure in `assets/js/templates/*.js`',
  '- Update behaviors in `assets/js/app.js`',
  '- Update styles in `assets/css/prototype.css`'
) -join "`r`n"
$guide += "`r`n"

Write-Utf8File -Path (Join-Path $cssDir 'prototype.css') -Content $styles
Write-Utf8File -Path (Join-Path $pagesDir 'platform-pages.js') -Content $platformJs
Write-Utf8File -Path (Join-Path $pagesDir 'project-pages.js') -Content $projectJs
Write-Utf8File -Path (Join-Path $pagesDir 'personal-pages.js') -Content $personalJs
Write-Utf8File -Path (Join-Path $appDir 'app.js') -Content $appJs
Write-Utf8File -Path $sourceFile -Content $indexHtml
Write-Utf8File -Path (Join-Path $root 'README-prototype-structure.md') -Content $guide

Write-Output 'Prototype split complete.'
