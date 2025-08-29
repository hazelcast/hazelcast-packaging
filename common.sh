#!/bin/bash

# Prints the given message to stderr
function echoerr() {
  # https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/workflow-commands-for-github-actions#setting-an-error-message
  # Support multi-line strings by replacing line separator with GitHub Actions compatible one
  echo "::error::ERROR - ${*//$'\n'/%0A}" 1>&2;
}

if [ -z "${ENVIRONMENT}" ]; then
  echoerr "Variable ENVIRONMENT is not set."
  exit 1
fi

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

case "${ENVIRONMENT}" in
test)
  # PRs publish to test repositories and install the packages from there
  export DEBIAN_REPO=debian-test-local
  export DEBIAN_REPO_BASE_URL="https://${JFROG_USERNAME}:${JFROG_TOKEN}@repository.hazelcast.com/${DEBIAN_REPO}"
  export RPM_REPO=rpm-test-local

  # This is a clone of the hazelcast/homebrew-hz repository
  export BREW_GIT_REPO_NAME="hazelcast/homebrew-hz-test"
  export BREW_TAP_NAME="hazelcast/hz-test"
  ;;
sandbox)
  export DEBIAN_REPO=sandbox-deb-prod
  export DEBIAN_REPO_BASE_URL="https://${JFROG_USERNAME}:${JFROG_TOKEN}@repository.hazelcast.com/${DEBIAN_REPO}"
  export RPM_REPO=sandbox-rpm-prod
  export BREW_GIT_REPO_NAME="hazelcast/homebrew-sandbox-hz"
  export BREW_TAP_NAME="hazelcast/sandbox-hz"
  ;;
live)
  export DEBIAN_REPO=debian-local
  export DEBIAN_REPO_BASE_URL="https://repository.hazelcast.com/${DEBIAN_REPO}"
  export RPM_REPO=rpm-local
  export BREW_GIT_REPO_NAME="hazelcast/homebrew-hz"
  export BREW_TAP_NAME="hazelcast/hz"
  ;;
*)
  echoerr "Unknown ENVIRONMENT: ${ENVIRONMENT}. Must be one of: test, sandbox, live."
  exit 1
  ;;
esac

export RPM_REPO_BASE_URL="https://repository.hazelcast.com/${RPM_REPO}"
export HZ_DISTRIBUTION_FILE=distribution.tar.gz
