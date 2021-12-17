#!/bin/bash

set -x

function derive_package_repo() {

  PACKAGE_REPO=stable
  if [[ "$1" == *"SNAPSHOT"* ]]; then
    PACKAGE_REPO=snapshot
  fi
  if [[ "$1" == *"DR"* ]]; then
    PACKAGE_REPO=devel
  fi
  if [[ "$1" == *"BETA"* ]]; then
    PACKAGE_REPO=beta
  fi


}