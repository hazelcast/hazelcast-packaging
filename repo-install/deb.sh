#!/bin/bash

branch="${1:-stable}"
version="${2:-main}"

# install tools used: wget, gpg, tee (coreutils)
# do not upgrade if already installed
sudo apt-get install -y --no-upgrade wget gpg coreutils

wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian $branch $version" | sudo tee -a /etc/apt/sources.list
sudo apt-get update
