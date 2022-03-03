#!/usr/bin/env bash

function alphanumCamelCase {
  echo "$1"|  sed -r 's/(-)/\./g' | tr '[:upper:]' '[:lower:]' | sed "s/\b\(.\)/\u\1/g" | sed 's+\.++g'
}

# The class name used in formula must not have dots nor hyphens and must be alphanumCamelCased
function brewClass {
  basename=$(alphanumCamelCase "$1")
  version=$(alphanumCamelCase "$2")
  if [ -n "${version}" ]; then
    version="AT${version}"
  fi
  echo "${basename}${version}"
}
