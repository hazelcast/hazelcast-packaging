#!/bin/bash

repoName="$1"
packageVersion='stable'
if [ "$2" ]; then
  packageVersion="$2"
fi

wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian ${packageVersion} main" | sudo tee -a /etc/apt/sources.list
sudo apt --assume-yes update
