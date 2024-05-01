# Get the directory of the script
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

# Change directory to the script's directory
Set-Location -Path $ScriptDir

# Define the name of the Docker network
$NetworkName = "transfer-hub-network"

# Define function to create the network
function Create-Network {
    Write-Output "Creating the network '$NetworkName'."
    # Check if the Docker network exists
    if (docker network inspect $NetworkName -ErrorAction SilentlyContinue) {
        Write-Output "The Docker network '$NetworkName' exists."
    }
    else {
        Write-Output "The Docker network '$NetworkName' does not exist."
        docker network create $NetworkName
    }
}

# Define function to create reverse-proxy
function Create-ReverseProxy {
    if (!(docker ps -a -q -f name=transfer-hub-reverse-proxy)) {
        # Container does not exist and needs to be created.
        # First create the database as this is the base of it all.
        Set-Location -Path "reverse-proxy"
        docker build -t transfer-hub-reverse-proxy:spring-docker .
        docker run -d --name transfer-hub-reverse-proxy -p 9096:80 --network $NetworkName transfer-hub-reverse-proxy:spring-docker
        Set-Location -Path $ScriptDir
    }
}

# Define database function
function Create-Database {
    if (!(docker ps -a -q -f name=transfer-hub-database)) {
        # Container does not exist and needs to be created.
        # First create the database as this is the base of it all.
        docker build -t transfer-hub-database:spring-docker .
        docker run -d --name transfer-hub-database -p 9095:3306 --network $NetworkName transfer-hub-database:spring-docker
    }
}

# Define the core
function Create-Core {
    $LogsDir = Join-Path -Path $ScriptDir -ChildPath "transfer-hub-core/logs"
    if (!(docker ps -a -q -f name=transfer-hub-core)) {
        # Container does not exist and needs to be created.
        Set-Location -Path "transfer-hub-core"
        docker build -t transfer-hub-core:spring-docker .
        docker run -d --name transfer-hub-core -v $LogsDir:/logs --link transfer-hub-database:transfer-hub-core --network $NetworkName -p 9092:8080 transfer-hub-core:spring-docker
        Set-Location -Path $ScriptDir
    }
    else {
        # Container does exist, needs to be removed to be updated.
        docker container stop transfer-hub-core
        docker container rm transfer-hub-core
        docker image rm transfer-hub-core:spring-docker --force

        Set-Location -Path "transfer-hub-core"
        docker build -t transfer-hub-core:spring-docker .
        docker run -d --name transfer-hub-core -v $LogsDir:/logs --link transfer-hub-database:transfer-hub-core --network $NetworkName -p 9092:8080 transfer-hub-core:spring-docker
        Set-Location -Path $ScriptDir
    }
}

# Define the front-end
function Create-Front {
    $GeneralJSPath = Join-Path -Path $ScriptDir -ChildPath "transfer-hub-front/htdocs/assets/js/general.js"
    (Get-Content $GeneralJSPath) -replace "http://localhost:9090", "" | Set-Content $GeneralJSPath
    if (!(docker ps -a -q -f name=transfer-hub-front)) {
        # Container does not exist and needs to be created.
        Set-Location -Path "transfer-hub-front"
        docker build -t transfer-hub-front:spring-docker .
        docker run -d --name transfer-hub-front --network $NetworkName -p 9093:80 transfer-hub-front:spring-docker
        Set-Location -Path $ScriptDir
    }
    else {
        # Container does exist, needs to be removed to be updated.
        docker container stop transfer-hub-front
        docker container rm transfer-hub-front
        docker image rm transfer-hub-front:spring-docker --force

        Set-Location -Path "transfer-hub-front"
        docker build -t transfer-hub-front:spring-docker .
        docker run -d --name transfer-hub-front --network $NetworkName -p 9093:80 transfer-hub-front:spring-docker
        Set-Location -Path $ScriptDir
    }
}

# Define Splunk
function Create-Splunk {
    docker volume create splunk
    docker run -d --name splunk -p 8000:8000 -p 8088:8088 --network $NetworkName -e "SPLUNK_START_ARGS=--accept-license" -e "SPLUNK_PASSWORD=Wachtwoord1" -v splunk:/opt/splunk/etc -v splunk:/opt/splunk/var -v splunk:/opt/splunk/var/lib -v splunk:/opt/splunk/var/log splunk/splunk:latest
}

# Main function to handle script execution
function Main {
    param(
        [string]$Action
    )

    switch ($Action) {
        "network" {
            Create-Network
        }
        "reverse_proxy" {
            Create-ReverseProxy
        }
        "core" {
            Create-Core
        }
        "front" {
            Create-Front
        }
        "database" {
            Create-Database
        }
        "splunk" {
            Create-Splunk
        }
        "complete" {
            Create-Network
            Create-ReverseProxy
            Create-Database
            Create-Core
            Create-Front
            Create-Splunk
        }
        default {
            Write-Output "This script requires a parameter to define whether the complete transfer-hub needs to be installed or that you want to replace parts of it."
            Write-Output "You can use complete to install the whole package completely."
            Write-Output "Adviced installation order on separate packages is network > reverse_proxy > database > core > front > splunk."
            Write-Output "Please make a choice based on the list below."
            Write-Output "list of options: $MyInvocation.MyCommand.Name [network|reverse_proxy|core|front|database|splunk|complete]"
        }
    }
}

Main $args[0]