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

# For brew the version is lowercase and with `.` instead of `-`
BREW_PACKAGE_VERSION=$(echo $PACKAGE_VERSION | tr '[:upper:]' '[:lower:]' | sed -r -r 's/(-)/\./g')
export BREW_PACKAGE_VERSION

# For RPM the version is with `.` instead of `-`
RPM_PACKAGE_VERSION=$(echo $PACKAGE_VERSION | sed -r -r 's/(-)/\./g')
export RPM_PACKAGE_VERSION

if [ "${EVENT_NAME}" == "pull_request" ]; then
  # PRs publish to test repositories and install the packages from there
  export DEBIAN_REPO=debian-test-local
  export DEBIAN_REPO_BASE_URL="https://repository.hazelcast.com/${DEBIAN_REPO}"
  export RPM_REPO=rpm-test-local
  export RPM_REPO_BASE_URL="https://repository.hazelcast.com/${RPM_REPO}"

  # This is a clone of the hazelcast/homebrew-hz repository
  export BREW_GIT_REPO_NAME="hazelcast/homebrew-hz-test"
  export BREW_TAP_NAME="hazelcast/hz-test"
else
  export DEBIAN_REPO=debian-local
  export DEBIAN_REPO_BASE_URL="https://repository.hazelcast.com/${DEBIAN_REPO}"
  export RPM_REPO=rpm-local
  export RPM_REPO_BASE_URL="https://repository.hazelcast.com/${RPM_REPO}"

  export BREW_GIT_REPO_NAME="hazelcast/homebrew-hz"
  export BREW_TAP_NAME="hazelcast/hz"
fi
