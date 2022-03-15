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

. "$SCRIPT_DIR"/../tests-common/assert.sh/assert.sh
. "$SCRIPT_DIR"/functions.sh

TESTS_RESULT=0

function assertAlphanumCamelCase {
  local testValue=$1
  local expected=$2
  local actual=$(alphanumCamelCase "$testValue")
  assert_eq "$expected" "$actual" "Alphanumeric camel case of $testValue should be equal to $expected " || TESTS_RESULT=$?
}

log_header "Tests for alphanumCamelCase"
assertAlphanumCamelCase "5.2-SNAPSHOT" "52Snapshot"
assertAlphanumCamelCase "5.2-BETA-1" "52Beta1"
assertAlphanumCamelCase "5.1-DEVEL-8" "51Devel8"
assertAlphanumCamelCase "5.2" "52"
assertAlphanumCamelCase "5.2.1" "521"
assertAlphanumCamelCase "" ""
assertAlphanumCamelCase "snapshot" "Snapshot"
assertAlphanumCamelCase "beta" "Beta"
assertAlphanumCamelCase "devel" "Devel"

function assertBrewClass {
  local distribution=$1
  local version=$2
  local expected=$3
  local actual=$(brewClass "$distribution" "$version")
  assert_eq "$expected" "$actual" "Brew class of $distribution $version should be equal to $expected " || TESTS_RESULT=$?
}

log_header "Tests for brewClass"
assertBrewClass "hazelcast" "5.2-SNAPSHOT" "HazelcastAT52Snapshot"
assertBrewClass "hazelcast-enterprise" "5.2-BETA-1" "HazelcastEnterpriseAT52Beta1"
assertBrewClass "hazelcast" "5.2-DEVEL-3" "HazelcastAT52Devel3"
assertBrewClass "hazelcast" "5.2" "HazelcastAT52"
assertBrewClass "hazelcast" "" "Hazelcast"
assertBrewClass "hazelcast-enterprise" "" "HazelcastEnterprise"

assert_eq 0 "$TESTS_RESULT" "All tests should pass"
