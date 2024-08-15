#!/usr/bin/env bash

set -euo pipefail ${RUNNER_DEBUG:+-x}

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


# With Homebrew we actually don't upload the artifact anywhere, but use the base tar.gz artifact url.
# The package manager then downloads it from there.
# The HZ_DISTRIBUTION_FILE is required to compute the hash.
if [ -z "${HZ_PACKAGE_URL}" ]; then
  echo "Variable HZ_PACKAGE_URL is not set. This is url pointing to hazelcast distribution tar.gz file"
  exit 1;
fi

source common.sh
source packages/brew/brew_functions.sh

if [ ! -f "${HZ_DISTRIBUTION_FILE}" ]; then
  echo "File ${HZ_DISTRIBUTION_FILE} doesn't exits in current directory."
  exit 1;
fi

echo "Building Homebrew package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${PACKAGE_VERSION}"

ASSET_SHASUM=$(sha256sum "${HZ_DISTRIBUTION_FILE}" | cut -d ' ' -f 1)

TEMPLATE_FILE="$(pwd)/packages/brew/hazelcast-template.rb"
cd homebrew-hz || exit 1

function updateClassName {
  class=$1
  file=$2
  sed -i "s+class HazelcastAT5X <\(.*$\)+class $class <\1+g" "$file"
}

function generateFormula {
  class=$1
  file=$2
  echo "Generating $file formula"
  cp "$TEMPLATE_FILE" "$file"
  updateClassName "$class" "$file"
  sed -i "s+url.*$+url \"${HZ_PACKAGE_URL}\"+g" "$file"
  sed -i "s+sha256.*$+sha256 \"${ASSET_SHASUM}\"+g" "$file"
  all_hz_versions=({hazelcast.rb,hazelcast?[0-9]*\.rb,hazelcast-enterprise*\.rb})
  for version in "${all_hz_versions[@]}"
  do
    if [[ "$version" != "$file" && ! "$version" =~ .*beta.* ]] ; then
      sed -i "/sha256.*$/a \ \ \ \ conflicts_with \"${version%.rb}\", because: \"you can install only a single hazelcast or hazelcast-enterprise package\"" "$file"
    fi
  done
}

BREW_CLASS=$(brewClass "${HZ_DISTRIBUTION}" "${BREW_PACKAGE_VERSION}")
generateFormula "$BREW_CLASS" "${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb"

# Update hazelcast and hazelcast-x.y aliases only if the version is a stable release (not SNAPSHOT/BETA)
if [[ "$RELEASE_TYPE" = "stable" ]]; then
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
      generateFormula "$(alphanumCamelCase "${HZ_DISTRIBUTION}")" "${HZ_DISTRIBUTION}.rb"
    fi
else
    # Update 'hazelcast-snapshot/beta/dr'
    # only if the version is greater than (new release) or equal to the highest version
    UPDATE_LATEST="true"
    versions=("${HZ_DISTRIBUTION}"-[0-9]*\.rb)
    for version in "${versions[@]}"
    do
      if [[ "$version" > "${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}.rb" ]]; then
        UPDATE_LATEST="false"
      fi
    done

    if [ "${UPDATE_LATEST}" == "true" ]; then
      BREW_CLASS=$(brewClass "${HZ_DISTRIBUTION}-$RELEASE_TYPE")
      generateFormula "$BREW_CLASS" "${HZ_DISTRIBUTION}-${RELEASE_TYPE}.rb"
    fi
fi

echo "Homebrew repository updated"
