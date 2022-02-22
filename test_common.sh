#!/bin/bash

function test_versions() {
  export HZ_VERSION=$1
  export PACKAGE_VERSION=$2
  echo "Test HZ_VERSION=$HZ_VERSION PACKAGE_VERSION=$PACKAGE_VERSION"

  source ./common.sh

  if [[ $DEB_PACKAGE_VERSION == "$3" && $RPM_PACKAGE_VERSION == "$4" ]]; then
    PASSED=yes
  else
    PASSED=no
  fi
  echo "Result RELEASE_VERSION=$RELEASE_VERSION, DEB=$DEB_PACKAGE_VERSION, RPM=$RPM_PACKAGE_VERSION, PASSED=$PASSED"
}

#             HZ_VERSION     PACKAGE_VERSION DEB_PACKAGE_VERSION RPM_PACKAGE_VERSION
test_versions "5.0.2"        "5.0.2"         "5.0.2-0"           "5.0.2-0"
test_versions "5.0.2"        "5.0.2-1"       "5.0.2-1"           "5.0.2-1"
test_versions "5.1"          "5.1"           "5.1-0"             "5.1-0"
test_versions "5.1"          "5.1-1"         "5.1-1"             "5.1-1"
test_versions "5.1-SNAPSHOT" "5.1-SNAPSHOT"  "5.1-SNAPSHOT-0"    "5.1.SNAPSHOT-0"
test_versions "5.1-BETA-1"   "5.1-BETA-1"    "5.1-BETA-1-0"      "5.1.BETA.1-0"
test_versions "5.1-BETA-1"   "5.1-BETA-1-2"  "5.1-BETA-1-2"      "5.1.BETA.1-2"
