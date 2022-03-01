#!/usr/bin/env bash

function isReleaseVersion {
   local version=$1
   if [[ ! ( ${version} =~ ^.*(SNAPSHOT|BETA|DR).*$ ) ]]; then
     return
   fi
   false
}

function alphanumCamelCase {
  echo "$1"|  sed -r 's/(-)/\./g' | sed -r 's/(^|\.)(\w)/\U\2/g' | sed 's+\.++g'
}
