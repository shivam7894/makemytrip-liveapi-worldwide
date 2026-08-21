@echo off
cd /d "%~dp0frontend"
if not exist node_modules (
  echo Installing frontend dependencies...
  npm install
)
echo Starting MakeMyTrip frontend...
npm run dev
pause
