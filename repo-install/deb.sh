#!/bin/bash

branch="${1:-stable}"
version="${2:-main}"

# docker doesn't have sudo - it's running as root by default
SUDO=""
if (( $EUID != 0 )); then
    SUDO="sudo"
fi

# install tools used: wget, gpg, tee (coreutils)
# do not upgrade if already installed
$SUDO apt-get install --assume-yes --no-upgrade wget gpg coreutils

wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | $SUDO tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian ${branch} ${version}" | $SUDO tee -a /etc/apt/sources.list
$SUDO apt-get update
