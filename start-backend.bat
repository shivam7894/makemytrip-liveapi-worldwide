@echo off
cd /d "%~dp0backend"
echo Starting MakeMyTrip backend on http://localhost:8080 ...
mvn spring-boot:run
pause
