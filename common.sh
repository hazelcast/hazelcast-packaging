#!/bin/bash

set -x

export PACKAGE_REPO=stable
if [[ "$HZ_VERSION" == *"SNAPSHOT"* ]]; then
  export PACKAGE_REPO=snapshot
fi
if [[ "$HZ_VERSION" == *"DR"* ]]; then
  export PACKAGE_REPO=devel
fi
if [[ "$HZ_VERSION" == *"BETA"* ]]; then
  export PACKAGE_REPO=beta
fi

BREW_PACKAGE_VERSION=$(echo $PACKAGE_VERSION | tr '[:upper:]' '[:lower:]' | sed -r -r 's/(-)/\./g')
export BREW_PACKAGE_VERSION

RPM_PACKAGE_VERSION=$(echo $PACKAGE_VERSION | sed -r -r 's/(-)/\./g')
export RPM_PACKAGE_VERSION

if [ "${EVENT_NAME}" == "pull_request" ]; then
  export DEBIAN_REPO_BASE_URL="https://repository.hazelcast.com/debian-test-local"
  export RPM_REPO_BASE_URL="https://repository.hazelcast.com/rpm-test-local"

  # TODO change when the access is setup
  export BREW_GIT_REPO_NAME="frant-hartm/homebrew-hz"
  export BREW_REPO_NAME="frant-hartm/hz"
else
  export DEBIAN_REPO_BASE_URL="https://repository.hazelcast.com/debian-local"
  export RPM_REPO_BASE_URL="https://repository.hazelcast.com/rpm-local"

  export BREW_GIT_REPO_NAME="hazelcast/homebrew-hz"
  export BREW_REPO_NAME="hazelcast/hz"
fi
