#!/usr/bin/env bash

set -x

if [ -z "${HZ_DISTRIBUTION}" ]; then
  echo "Variable HZ_DISTRIBUTION is not set. It must be set to 'hazelcast' for OS, 'hazelcast-enterprise' for EE"
  exit 1
fi

if [ -z "${HZ_VERSION}" ]; then
  echo "Variable HZ_VERSION is not set. This is the version of Hazelcast used to build the package."
  exit 1
fi

if [ -z "${PACKAGE_VERSION}" ]; then
  echo "Variable PACKAGE_VERSION is not set. This is the version of the built package."
  exit 1
fi

export HZ_DISTRIBUTION_FILE=${HZ_DISTRIBUTION}-distribution-${HZ_VERSION}.tar.gz

if [ ! -f "${HZ_DISTRIBUTION_FILE}" ]; then
  echo "File ${HZ_DISTRIBUTION_FILE} doesn't exits in current directory."
  exit 1;
fi

# With Homebrew we actually don't upload the artifact anywhere, but use the base tar.gz artifact url.
# The package manager then downloads it from there.
# The HZ_DISTRIBUTION_FILE is required to compute the hash.
if [ -z "${HZ_PACKAGE_URL}" ]; then
  echo "Variable HZ_PACKAGE_URL is not set. This is url pointing to hazelcast distribution tar.gz file"
  exit 1;
fi

source common.sh
source packages/brew/functions.sh

echo "Building Homebrew package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${PACKAGE_VERSION}"

ASSET_SHASUM=$(sha256sum "${HZ_DISTRIBUTION_FILE}" | cut -d ' ' -f 1)

# If this is 'hazelcast' package it conflicts with 'hazelcast-enterprise' and vice versa
export CONFLICTS=hazelcast-enterprise
if [ ${HZ_DISTRIBUTION} == "hazelcast-enterprise" ]; then
  export CONFLICTS=hazelcast
fi

TEMPLATE_FILE="$(pwd)/packages/brew/hazelcast-template.rb"
cd ../homebrew-hz || exit 1


# The class name used in formula must not have dots nor hyphens and must be alphanumCamelCased
function brewClass {
  basename=$1
  version=$2
  if [ -n "${version}" ]; then
    version="AT${version}"
  fi
  echo "$(alphanumCamelCase $basename)$(alphanumCamelCase $version)"
}

function updateClassName {
  class=$1
  file=$2
  sed -i "s+class HazelcastAT5X <\(.*$\)+class $class <\1+g" "$file"
}

function generateFormula {
  class=$1
  file=$2
  cp "$TEMPLATE_FILE" "$file"
  updateClassName "$class" "$file"
  sed -i "s+url.*$+url \"${HZ_PACKAGE_URL}\"+g" "$file"
  sed -i "s+sha256.*$+sha256 \"${ASSET_SHASUM}\"+g" "$file"
  sed -i "s+conflicts_with \".*\"$+conflicts_with \"$CONFLICTS\"+g" "$file"
}

BREW_CLASS=$(brewClass "${HZ_DISTRIBUTION}" "${BREW_PACKAGE_VERSION}")
generateFormula "$BREW_CLASS" "${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb"

# Update hazelcast and hazelcast-x.y aliases only if the version is release (not SNAPSHOT/DR/BETA)
if isReleaseVersion "$HZ_VERSION"; then
  HZ_MINOR_VERSION=$(echo "${HZ_VERSION}" | cut -c -3)

  rm -f "Aliases/${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}" #migrate incrementally from symlinks to regular files
  BREW_CLASS=$(brewClass "${HZ_DISTRIBUTION}${HZ_MINOR_VERSION}")
  generateFormula "$BREW_CLASS" "${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}.rb"

  # Update 'hazelcast' or 'hazelcast-enterprise' alias
  # only if the version is greater than (new release) or equal to highest version
  UPDATE_LATEST="true"
  versions=("${HZ_DISTRIBUTION}"-[0-9]*\.rb)
  for version in "${versions[@]}"
  do
    if [[ "$version" > "${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}.rb" ]]; then
      UPDATE_LATEST="false"
    fi
  done

  if [ "${UPDATE_LATEST}" == "true" ]; then
    rm -f "Aliases/${HZ_DISTRIBUTION}" #migrate incrementally from symlinks to regular files
    generateFormula "$(alphanumCamelCase ${HZ_DISTRIBUTION})" "${HZ_DISTRIBUTION}.rb"
  fi
fi

echo "Homebrew repository updated"
