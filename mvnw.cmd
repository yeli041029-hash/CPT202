@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
set "WRAPPER_PROPS=%SCRIPT_DIR%\.mvn\wrapper\maven-wrapper.properties"

if not exist "%WRAPPER_PROPS%" (
  echo Missing wrapper properties: "%WRAPPER_PROPS%"
  exit /b 1
)

for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "$line = Get-Content -LiteralPath '%WRAPPER_PROPS%' | Where-Object { $_ -match '^\s*distributionUrl\s*=' } | Select-Object -First 1; if (-not $line) { exit 1 }; [Console]::Out.Write(($line -replace '^\s*distributionUrl\s*=\s*', ''))"`) do set "DISTRIBUTION_URL=%%I"

if not defined DISTRIBUTION_URL (
  echo distributionUrl is not configured in "%WRAPPER_PROPS%"
  exit /b 1
)

for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "[Console]::Out.Write([System.IO.Path]::GetFileName(([uri]'%DISTRIBUTION_URL%').AbsolutePath))"`) do set "DIST_ZIP=%%I"
for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "[Console]::Out.Write([System.IO.Path]::GetFileNameWithoutExtension('%DIST_ZIP%'))"`) do set "DIST_NAME=%%I"

set "CACHE_DIR=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%"
set "ZIP_FILE=%CACHE_DIR%\%DIST_ZIP%"
set "MVN_CMD="

if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%" >nul 2>&1

for /f "delims=" %%F in ('dir /s /b "%CACHE_DIR%\mvn.cmd" 2^>nul') do set "MVN_CMD=%%F"

if not defined MVN_CMD (
  echo Downloading Maven from %DISTRIBUTION_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $cacheDir='%CACHE_DIR%'; $zipFile='%ZIP_FILE%'; $distributionUrl='%DISTRIBUTION_URL%'; if (-not (Test-Path -LiteralPath $cacheDir)) { New-Item -ItemType Directory -Path $cacheDir | Out-Null }; Invoke-WebRequest -UseBasicParsing -Uri $distributionUrl -OutFile $zipFile; Expand-Archive -LiteralPath $zipFile -DestinationPath $cacheDir -Force"
  if errorlevel 1 exit /b 1
  for /f "delims=" %%F in ('dir /s /b "%CACHE_DIR%\mvn.cmd" 2^>nul') do set "MVN_CMD=%%F"
)

if not defined MVN_CMD (
  echo Unable to locate mvn.cmd after extraction.
  exit /b 1
)

call "%MVN_CMD%" %*
exit /b %ERRORLEVEL%
