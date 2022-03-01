#!/usr/bin/env bash

function isReleaseVersion {
   local version=$1
   if [[ ! ( ${version} =~ ^.*(SNAPSHOT|BETA|DR).*$ ) ]]; then
     return
   fi
   false
}
