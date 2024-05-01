#! /bin/sh

# Get the directory of the script
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Change directory to the script's directory
cd "$SCRIPT_DIR" || exit



# Removing old docker instances if there are any. This includes the database.

# Define the name of the Docker network
NETWORK_NAME="transfer-hub-network"

# Define function to create the network
create_network() {
  echo "Creating the network '$NETWORK_NAME'."
  # Check if the Docker network exists
  if docker network inspect "$NETWORK_NAME" &>/dev/null; then
      echo "The Docker network '$NETWORK_NAME' exists."
  else
      echo "The Docker network '$NETWORK_NAME' does not exist."
      sudo docker network create transfer-hub-network
  fi
}

# Define function to create reverse-proxy
create_reverse_proxy() {
  if [ ! "$(docker ps -a -q -f name=transfer-hub-reverse-proxy)" ]; then
      #Container does not exist and needs to be created.
      # First create the database as this is the base of it all.
      cd reverse-proxy
      sudo docker build -t transfer-hub-reverse-proxy:spring-docker .
      sudo docker run -d --name transfer-hub-reverse-proxy -p 9096:80 --network transfer-hub-network transfer-hub-reverse-proxy:spring-docker
      cd ..
  fi
}

# Define database function
create_database() {
  if [ ! "$(docker ps -a -q -f name=transfer-hub-database)" ]; then
      #Container does not exist and needs to be created.
      # First create the database as this is the base of it all.
      sudo docker build -t transfer-hub-database:spring-docker .
      sudo docker run -d --name transfer-hub-database -p 9095:3306 --network transfer-hub-network transfer-hub-database:spring-docker
  fi
}

# Define the core
create_core() {
  LOGSDIR=$SCRIPT_DIR + "/" + "transfer-hub-core/logs"
  if [ ! "$(docker ps -a -q -f name=transfer-hub-core)" ]; then
        #Container does not exist and needs to be created.
        # shellcheck disable=SC2164
        cd transfer-hub-core
        sudo docker build -t transfer-hub-core:spring-docker .
        sudo docker run -d --name transfer-hub-core -v "$LOGSDIR:/logs" --link transfer-hub-database:transfer-hub-core --network transfer-hub-network -p 9092:8080 transfer-hub-core:spring-docker
        # shellcheck disable=SC2103
        cd ..
      else
        # Container does exist, needs to be removed to be updated.
        sudo docker container stop transfer-hub-core
        sudo docker container rm transfer-hub-core
        sudo docker image rm transfer-hub-core:spring-docker --force

        # shellcheck disable=SC2164
        cd transfer-hub-core
        sudo docker build -t transfer-hub-core:spring-docker .
        sudo docker run -d --name transfer-hub-core -v "$LOGSDIR:/logs" --link transfer-hub-database:transfer-hub-core --network transfer-hub-network -p 9092:8080 transfer-hub-core:spring-docker
        # shellcheck disable=SC2103
        cd ..
  fi
}

#Define the front-end
create_front() {
  sed -i 's/http:\/\/localhost:9090//g' transfer-hub-front/htdocs/assets/js/general.js
  if [ ! "$(docker ps -a -q -f name=transfer-hub-front)" ]; then
        #Container does not exist and needs to be created.
        # shellcheck disable=SC2164
        cd transfer-hub-front
        sudo docker build -t transfer-hub-front:spring-docker .
        sudo docker run -d --name transfer-hub-front --network transfer-hub-network -p 9093:80 transfer-hub-front:spring-docker
      else
        # Container does exist, needs to be removed to be updated.
        sudo docker container stop transfer-hub-front
        sudo docker container rm transfer-hub-front
        sudo docker image rm transfer-hub-front:spring-docker --force

        # shellcheck disable=SC2164
        cd transfer-hub-front
        sudo docker build -t transfer-hub-front:spring-docker .
        sudo docker run -d --name transfer-hub-front --network transfer-hub-network -p 9093:80 transfer-hub-front:spring-docker
  fi
}

#Define Splunk
create_splunk() {
  docker volume create splunk
  docker run -d --name splunk -p 8000:8000 -p 8088:8088 --network transfer-hub-network -e "SPLUNK_START_ARGS=--accept-license" -e "SPLUNK_PASSWORD=Wachtwoord1" -v splunk:/opt/splunk/etc -v splunk:/opt/splunk/var -v splunk:/opt/splunk/var/lib -v splunk:/opt/splunk/var/log splunk/splunk:latest
}

# Main function to handle script execution
main() {
    if [ "$1" = "network" ]; then
            create_network
    elif [ "$1" = "reverse_proxy" ]; then
            create_reverse_proxy
    elif [ "$1" = "core" ]; then
            create_core
    elif [ "$1" = "front" ]; then
            create_front
    elif [ "$1" = "database" ]; then
            create_database
    elif [ "$1" = "splunk" ]; then
            create_splunk
    elif [ "$1" = "complete" ]; then
            create_network
            create_reverse_proxy
            create_database
            create_core
            create_front
    else
        echo "This script requires a parameter to define whether the complete transfer-hub needs to be installed or that you want to replace parts of it."
        echo "You can use complete to install the whole package completely".
        echo "Adviced installation order on seperate packages is network > reverse_proxy > database > core > front > splunk"
        echo "Please make a choice based on the list below.."
        echo "list of options: $0 [network|reverse_proxy|core|front|database|splunk|complete]"
        exit 1
    fi
}

main "$@"

