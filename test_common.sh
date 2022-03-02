#!/usr/bin/env bash

function findScriptDir() {
  CURRENT=$PWD

  DIR=$(dirname "$0")
  cd "$DIR" || exit
  TARGET_FILE=$(basename "$0")

  # Iterate down a (possible) chain of symlinks
  while [ -L "$TARGET_FILE" ]
  do
      TARGET_FILE=$(readlink "$TARGET_FILE")
      DIR=$(dirname "$TARGET_FILE")
      cd "$DIR" || exit
      TARGET_FILE=$(basename "$TARGET_FILE")
  done

  SCRIPT_DIR=$(pwd -P)
  # Restore current directory
  cd "$CURRENT" || exit
}

findScriptDir

. "$SCRIPT_DIR"/packages/tests-common/assert.sh/assert.sh
. "$SCRIPT_DIR"/common.sh

TESTS_RESULT=0

function assertReleaseType {
  export HZ_VERSION=$1
  local expected=$2
  . "$SCRIPT_DIR"/common.sh
  assert_eq $expected $RELEASE_TYPE "Version $HZ_VERSION should not a $expected release" || TESTS_RESULT=$?
}

log_header "Tests for RELEASE_TYPE"
assertReleaseType "5.2-SNAPSHOT" "snapshot"
assertReleaseType "5.2-BETA-1" "beta"
assertReleaseType "5.1-DR8" "devel"
assertReleaseType "5.0" "stable"
assertReleaseType "5.1" "stable"
assertReleaseType "5.1.1" "stable"

function assertPackageVersions {
  export HZ_VERSION=$1
  export PACKAGE_VERSION=$2
  local expectedDebVersion=$3
  local expectedRpmVersion=$4
  . "$SCRIPT_DIR"/common.sh
  assert_eq "$expectedDebVersion" "$DEB_PACKAGE_VERSION" "DEB_PACKAGE_VERSION for (HZ_VERSION=$HZ_VERSION, PACKAGE_VERSION=$PACKAGE_VERSION) should be $expectedDebVersion" || TESTS_RESULT=$?
  assert_eq "$expectedRpmVersion" "$RPM_PACKAGE_VERSION" "RPM_PACKAGE_VERSION for (HZ_VERSION=$HZ_VERSION, PACKAGE_VERSION=$PACKAGE_VERSION) should be $expectedRpmVersion" || TESTS_RESULT=$?
}

log_header "Tests for DEB_PACKAGE_VERSION and RPM_PACKAGE_VERSION"
assertPackageVersions "5.0.2"        "5.0.2"         "5.0.2-1"           "5.0.2-1"
assertPackageVersions "5.0.2"        "5.0.2-1"       "5.0.2-1"           "5.0.2-1"
assertPackageVersions "5.1"          "5.1"           "5.1-1"             "5.1-1"
assertPackageVersions "5.1"          "5.1-1"         "5.1-1"             "5.1-1"
assertPackageVersions "5.1-SNAPSHOT" "5.1-SNAPSHOT"  "5.1-SNAPSHOT-1"    "5.1.SNAPSHOT-1"
assertPackageVersions "5.1-BETA-1"   "5.1-BETA-1"    "5.1-BETA-1-1"      "5.1.BETA.1-1"
assertPackageVersions "5.1-BETA-1"   "5.1-BETA-1-2"  "5.1-BETA-1-2"      "5.1.BETA.1-2"

assert_eq 0 "$TESTS_RESULT" "All tests should pass"
