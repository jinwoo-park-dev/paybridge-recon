$ErrorActionPreference = "Stop"

$RootDir = Resolve-Path (Join-Path $PSScriptRoot "../..")
Set-Location $RootDir

$Mode = if ($args.Length -gt 0) { $args[0] } else { "db" }

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "[bootstrap] .env file was created from .env.example"
}

switch ($Mode) {
    "db" {
        Write-Host "[bootstrap] Starting local postgres only..."
        docker compose -f compose.yml up -d postgres
        Write-Host "[bootstrap] Done. Start the app with .\gradlew.bat bootRun --args='--spring.profiles.active=local'"
    }
    "full" {
        Write-Host "[bootstrap] Starting postgres + paybridge-recon container..."
        docker compose -f compose.yml --profile full up --build -d postgres paybridge-recon
        Write-Host "[bootstrap] Done. App should be available at http://localhost:8081"
    }
    "down" {
        Write-Host "[bootstrap] Stopping compose stack..."
        docker compose -f compose.yml down
    }
    "reset-db" {
        Write-Host "[bootstrap] Resetting postgres volume..."
        docker compose -f compose.yml down -v
        docker compose -f compose.yml up -d postgres
    }
    default {
        Write-Host "Usage: scripts/dev/bootstrap.ps1 [db|full|down|reset-db]"
        exit 1
    }
}
