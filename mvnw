#!/bin/sh
# Maven wrapper — downloads Maven 3.9.9 on first run if not already cached.
set -e

MAVEN_VERSION="3.9.9"
MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

if [ ! -f "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Downloading Apache Maven ${MAVEN_VERSION}..."
    mkdir -p "${MAVEN_HOME}"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "${MAVEN_DOWNLOAD_URL}" | tar -xz -C "${MAVEN_HOME}" --strip-components=1
    elif command -v wget >/dev/null 2>&1; then
        wget -qO- "${MAVEN_DOWNLOAD_URL}" | tar -xz -C "${MAVEN_HOME}" --strip-components=1
    else
        echo "ERROR: Neither curl nor wget is available." >&2
        exit 1
    fi
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
