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

# The class name used in formula must not have dots nor hyphens and must be alphanumCamelCased
function brewClass {
  basename=$1
  version=$2
  if [ -n "${version}" ]; then
    version="AT${version}"
  fi
  echo "$(alphanumCamelCase $basename)$(alphanumCamelCase $version)"
}
