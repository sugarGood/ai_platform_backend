param(
    [ValidateSet("B", "A", "C", "D", "ALL")]
    [string]$Stage = "ALL"
)

$ErrorActionPreference = "Stop"
$RepoRoot = "c:\Users\76741\Desktop\ai_platform_backend"

function Run-Step($name, $command) {
    Write-Host "`n=== $name ===" -ForegroundColor Cyan
    Push-Location $RepoRoot
    try {
        Invoke-Expression $command
    }
    finally {
        Pop-Location
    }
}

function Run-AgentB {
    Run-Step "Agent B (RBAC foundation)" "mvn -B -pl ai-platform-server -Dtest=*Role*,*Permission*,*ProjectMember*,*Rbac* -Dsurefire.failIfNoSpecifiedTests=false test"
}

function Run-AgentA {
    Run-Step "Agent A (Me & Credential APIs)" "mvn -B -pl ai-platform-server -Dtest=*Me*,*Credential*,*UserClientBinding* -Dsurefire.failIfNoSpecifiedTests=false test"
}

function Run-AgentC {
    Run-Step "Agent C (AI capability config)" "mvn -B -pl ai-platform-server -Dtest=*Knowledge*,*Skill*,*Tool*,*Mcp* -Dsurefire.failIfNoSpecifiedTests=false test"
}

function Run-AgentD {
    Run-Step "Agent D (Gateway & Quota)" "mvn -B -pl ai-platform-server,ai-platform-agent -Dtest=*Usage*,*Quota*,*Gateway*,*ProjectContext*,*CredentialAuth* -Dsurefire.failIfNoSpecifiedTests=false test"
    Run-Step "Final Full Verify" "mvn -B verify"
}

switch ($Stage) {
    "B" { Run-AgentB }
    "A" { Run-AgentA }
    "C" { Run-AgentC }
    "D" { Run-AgentD }
    "ALL" {
        Run-AgentB
        Run-AgentA
        Run-AgentC
        Run-AgentD
    }
}

Write-Host "`nWorkflow finished: $Stage" -ForegroundColor Green
