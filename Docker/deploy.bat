@echo off
setlocal

:: Get the directory of the script
set "SCRIPT_DIR=%~dp0"

:: Change directory to the script's directory
cd /d "%SCRIPT_DIR%" || exit /b

:: Define the name of the Docker network
set "NETWORK_NAME=transfer-hub-network"

:: Define function to create the network
:CreateNetwork
echo Creating the network '%NETWORK_NAME%'.
docker network inspect %NETWORK_NAME% >nul 2>nul
if %errorlevel% equ 0 (
    echo The Docker network '%NETWORK_NAME%' exists.
) else (
    echo The Docker network '%NETWORK_NAME%' does not exist.
    docker network create %NETWORK_NAME%
)

:: Define function to create reverse-proxy
:CreateReverseProxy
docker ps -a -q -f name=transfer-hub-reverse-proxy >nul 2>nul
if errorlevel 1 (
    :: Container does not exist and needs to be created.
    :: First create the database as this is the base of it all.
    pushd reverse-proxy
    docker build -t transfer-hub-reverse-proxy:spring-docker .
    docker run -d --name transfer-hub-reverse-proxy -p 9096:80 --network %NETWORK_NAME% transfer-hub-reverse-proxy:spring-docker
    popd
)

:: Define database function
:CreateDatabase
docker ps -a -q -f name=transfer-hub-database >nul 2>nul
if errorlevel 1 (
    :: Container does not exist and needs to be created.
    :: First create the database as this is the base of it all.
    docker build -t transfer-hub-database:spring-docker .
    docker run -d --name transfer-hub-database -p 9095:3306 --network %NETWORK_NAME% transfer-hub-database:spring-docker
)

:: Define the core
:CreateCore
set "LOGSDIR=%SCRIPT_DIR%\transfer-hub-core\logs"
docker ps -a -q -f name=transfer-hub-core >nul 2>nul
if errorlevel 1 (
    :: Container does not exist and needs to be created.
    pushd transfer-hub-core
    docker build -t transfer-hub-core:spring-docker .
    docker run -d --name transfer-hub-core -v %LOGSDIR%:/logs --link transfer-hub-database:transfer-hub-core --network %NETWORK_NAME% -p 9092:8080 transfer-hub-core:spring-docker
    popd
) else (
    :: Container does exist, needs to be removed to be updated.
    docker container stop transfer-hub-core
    docker container rm transfer-hub-core
    docker image rm transfer-hub-core:spring-docker --force

    pushd transfer-hub-core
    docker build -t transfer-hub-core:spring-docker .
    docker run -d --name transfer-hub-core -v %LOGSDIR%:/logs --link transfer-hub-database:transfer-hub-core --network %NETWORK_NAME% -p 9092:8080 transfer-hub-core:spring-docker
    popd
)

:: Define the front-end
:CreateFront
set "GENERAL_JS_PATH=%SCRIPT_DIR%\transfer-hub-front\htdocs\assets\js\general.js"
findstr /C:"http://localhost:9090" "%GENERAL_JS_PATH%" >nul 2>nul
if %errorlevel% equ 0 (
    (for /f "delims=" %%i in ('type "%GENERAL_JS_PATH%" ^& break ^> "%GENERAL_JS_PATH%"') do (
        set "line=%%i"
        setlocal enabledelayedexpansion
        echo(!line:http://localhost:9090=!
        endlocal
    )) > "%GENERAL_JS_PATH%"
)
docker ps -a -q -f name=transfer-hub-front >nul 2>nul
if errorlevel 1 (
    :: Container does not exist and needs to be created.
    pushd transfer-hub-front
    docker build -t transfer-hub-front:spring-docker .
    docker run -d --name transfer-hub-front --network %NETWORK_NAME% -p 9093:80 transfer-hub-front:spring-docker
    popd
) else (
    :: Container does exist, needs to be removed to be updated.
    docker container stop transfer-hub-front
    docker container rm transfer-hub-front
    docker image rm transfer-hub-front:spring-docker --force

    pushd transfer-hub-front
    docker build -t transfer-hub-front:spring-docker .
    docker run -d --name transfer-hub-front --network %NETWORK_NAME% -p 9093:80 transfer-hub-front:spring-docker
    popd
)

:: Define Splunk
:CreateSplunk
docker volume create splunk >nul 2>nul
docker run -d --name splunk -p 8000:8000 -p 8088:8088 --network %NETWORK_NAME% -e "SPLUNK_START_ARGS=--accept-license" -e "SPLUNK_PASSWORD=Wachtwoord1" -v splunk:/opt/splunk/etc -v splunk:/opt/splunk/var -v splunk:/opt/splunk/var/lib -v splunk:/opt/splunk/var/log splunk/splunk:latest

:: Main function to handle script execution
:Main
if "%~1" equ "network" (
    goto :CreateNetwork
) elseif "%~1" equ "reverse_proxy" (
    goto :CreateReverseProxy
) elseif "%~1" equ "core" (
    goto :CreateCore
) elseif "%~1" equ "front" (
    goto :CreateFront
) elseif "%~1" equ "database" (
    goto :CreateDatabase
) elseif "%~1" equ "splunk" (
    goto :CreateSplunk
) elseif "%~1" equ "complete" (
    goto :CreateNetwork
    goto :CreateReverseProxy
    goto :CreateDatabase
    goto :CreateCore
    goto :CreateFront
    goto :CreateSplunk
) else (
    echo This script requires a parameter to define whether the complete transfer-hub needs to be installed or that you want to replace parts of it.
    echo You can use complete to install the whole package completely.
    echo Advised installation order on separate packages is network > reverse_proxy > database > core > front > splunk.
    echo Please make a choice based on the list below.
    echo list of options: %~nx0 [network^|reverse_proxy^|core^|front^|database^|splunk^|complete]
)
