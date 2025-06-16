#!/usr/bin/env bash

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

export USE_TEST_REPO=true

# Source the latest version of assert.sh unit testing library and include in current shell
source /dev/stdin <<< "$(curl --silent https://raw.githubusercontent.com/hazelcast/assert.sh/main/assert.sh)"

TESTS_RESULT=0

function assertReleaseType {
  export HZ_VERSION=$1
  local expected=$2
  . "$SCRIPT_DIR"/common.sh
  local msg="Version $HZ_VERSION should be a $expected release"
  assert_eq $expected $RELEASE_TYPE "$msg" && log_success "$msg" || TESTS_RESULT=$?
}

log_header "Tests for RELEASE_TYPE"
assertReleaseType "5.2.0-SNAPSHOT" "snapshot"
assertReleaseType "5.2-SNAPSHOT" "snapshot"
assertReleaseType "5.2.0-BETA-1" "beta"
assertReleaseType "5.2-BETA-1" "beta"
assertReleaseType "5.0" "stable"
assertReleaseType "5.1" "stable"
assertReleaseType "5.1.1" "stable"
assertReleaseType "5.2.0" "stable"

function assertPackageVersions {
  export HZ_VERSION=$1
  export PACKAGE_VERSION=$2
  local expectedDebVersion=$3
  local expectedRpmVersion=$4
  . "$SCRIPT_DIR"/common.sh
  local msg="DEB_PACKAGE_VERSION for (HZ_VERSION=$HZ_VERSION, PACKAGE_VERSION=$PACKAGE_VERSION) should be $expectedDebVersion"
  assert_eq "$expectedDebVersion" "$DEB_PACKAGE_VERSION" "$msg" && log_success "$msg" || TESTS_RESULT=$?
  msg="RPM_PACKAGE_VERSION for (HZ_VERSION=$HZ_VERSION, PACKAGE_VERSION=$PACKAGE_VERSION) should be $expectedRpmVersion"
  assert_eq "$expectedRpmVersion" "$RPM_PACKAGE_VERSION" "$msg" && log_success "$msg" || TESTS_RESULT=$?
}

log_header "Tests for DEB_PACKAGE_VERSION and RPM_PACKAGE_VERSION"
assertPackageVersions "5.0.2"        "5.0.2"         "5.0.2-1"           "5.0.2-1"
assertPackageVersions "5.0.2"        "5.0.2-1"       "5.0.2-1"           "5.0.2-1"
assertPackageVersions "5.1"          "5.1"           "5.1-1"             "5.1-1"
assertPackageVersions "5.1"          "5.1-1"         "5.1-1"             "5.1-1"
assertPackageVersions "5.1-SNAPSHOT" "5.1-SNAPSHOT"  "5.1-SNAPSHOT-1"    "5.1.SNAPSHOT-1"
assertPackageVersions "5.1-BETA-1"   "5.1-BETA-1"    "5.1-BETA-1-1"      "5.1.BETA.1-1"
assertPackageVersions "5.1-BETA-1"   "5.1-BETA-1-2"  "5.1-BETA-1-2"      "5.1.BETA.1-2"
assertPackageVersions "5.2.0-SNAPSHOT" "5.2.0-SNAPSHOT"  "5.2.0-SNAPSHOT-1"    "5.2.0.SNAPSHOT-1"

function assertMinorVersion {
  export HZ_VERSION=$1
  local expected=$2
  . "$SCRIPT_DIR"/common.sh
  local msg="Version $HZ_VERSION should be mapped to $expected minor version"
  assert_eq "$expected" "$HZ_MINOR_VERSION" "$msg" && log_success "$msg" || TESTS_RESULT=$?
}

log_header "Tests for HZ_MINOR_VERSION"
assertMinorVersion "5.2-SNAPSHOT" "5.2-SNAPSHOT"
assertMinorVersion "5.2.0-SNAPSHOT" "5.2.0-SNAPSHOT"
assertMinorVersion "5.2.0-BETA-1" "5.2.0-BETA-1"
assertMinorVersion "5.10" "5.10"
assertMinorVersion "5.10.1" "5.10"
assertMinorVersion "5.0" "5.0"
assertMinorVersion "5.1.1" "5.1"
assertMinorVersion "5.2.0" "5.2"

assert_eq 0 "$TESTS_RESULT" "All tests should pass"
