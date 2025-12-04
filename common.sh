#!/bin/bash

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

source "$SCRIPT_DIR/logging.functions.sh"

export RELEASE_TYPE=stable
if [[ "$HZ_VERSION" == *"SNAPSHOT"* ]]; then
  export RELEASE_TYPE=snapshot
fi
if [[ "$HZ_VERSION" == *"BETA"* ]]; then
  export RELEASE_TYPE=beta
fi
export PACKAGE_REPO=$RELEASE_TYPE

if [[ "$HZ_VERSION" == *"-"* ]]; then
  HZ_MINOR_VERSION="${HZ_VERSION}"
else
  # Extract minor version from HZ_VERSION, works also for 5.10 etc..
  # -d'.' splits by delimiter, -f selects first two components
  HZ_MINOR_VERSION=$(echo "${HZ_VERSION}" | cut -f1,2 -d'.')
fi
export HZ_MINOR_VERSION

# Extract release version from package version - release version is the version part specific to the package

# Remove HZ_VERSION prefix from PACKAGE_VERSION,
# e.g. HZ_VERSION=5.1-DR8,PACKAGE_VERSION=5.1-DR8-1 -> -1
RELEASE_VERSION=${PACKAGE_VERSION#"$HZ_VERSION"}

# Remove '-' from RELEASE_VERSION
RELEASE_VERSION=${RELEASE_VERSION#-}

if [ -z "$RELEASE_VERSION" ]; then
  # Default to 1 if not set
  RELEASE_VERSION=1
fi
export RELEASE_VERSION

# See https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
# "if it (= debian revision) isn’t present then the upstream_version must not contain a hyphen"
# So if our upstream version contains hyphen, e.g. 5.2-DR8, the deb package version should be 5.2-DR8-1
# For simplicity we add it in all cases, default to 1 when not specified in PACKAGE_VERSION
DEB_PACKAGE_VERSION="$HZ_VERSION"-$RELEASE_VERSION
export DEB_PACKAGE_VERSION

# For RPM the upstream version part must not contain -, use `.` instead of `-`
RPM_HZ_VERSION=$(echo $HZ_VERSION | sed -r -r 's/(-)/\./g')
export RPM_HZ_VERSION
RPM_PACKAGE_VERSION=$RPM_HZ_VERSION-$RELEASE_VERSION
export RPM_PACKAGE_VERSION

# For brew the version is lowercase and with `.` instead of `-`
BREW_PACKAGE_VERSION=$(echo $PACKAGE_VERSION | tr '[:upper:]' '[:lower:]' | sed -r -r 's/(-)/\./g')
export BREW_PACKAGE_VERSION

export HZ_DISTRIBUTION_FILE=distribution.tar.gz
