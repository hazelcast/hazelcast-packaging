#!/bin/bash

local packageVersion='stable'
if [ -z "${PACKAGE_VERSION}" ]; then
  packageVersion="$PACKAGE_VERSION"
fi

wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian ${packageVersion} main" | sudo tee -a /etc/apt/sources.list
sudo apt --assume-yes update