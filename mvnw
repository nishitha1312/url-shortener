#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

# If JAVA_HOME is set, use it; otherwise just use java
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="$(command -v java)"
    if [ -z "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found."
    fi
fi

# Locate the script directory
MAVEN_PROJECTBASEDIR="${BASH_SOURCE:-$0}"
MAVEN_PROJECTBASEDIR="$(dirname "$MAVEN_PROJECTBASEDIR")"
MAVEN_PROJECTBASEDIR="$(cd "$MAVEN_PROJECTBASEDIR" && pwd)"

MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

# Read distribution URL from properties
DISTRIBUTION_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2-)
MAVEN_HOME_PARENT="$HOME/.m2/wrapper/dists"
DISTRIBUTION_ID=$(echo "$DISTRIBUTION_URL" | sed 's/.*\///;s/\.zip$//')
MAVEN_HOME="$MAVEN_HOME_PARENT/$DISTRIBUTION_ID"

# Download Maven if not already present
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven..."
    mkdir -p "$MAVEN_HOME_PARENT"
    TMP_ZIP="$MAVEN_HOME_PARENT/$DISTRIBUTION_ID.zip"
    
    if command -v curl > /dev/null; then
        curl -fsSL -o "$TMP_ZIP" "$DISTRIBUTION_URL"
    elif command -v wget > /dev/null; then
        wget -q -O "$TMP_ZIP" "$DISTRIBUTION_URL"
    else
        echo "ERROR: Neither curl nor wget is available." >&2
        exit 1
    fi
    
    unzip -q "$TMP_ZIP" -d "$MAVEN_HOME_PARENT"
    rm -f "$TMP_ZIP"
    # The unzipped folder is named apache-maven-X.X.X
    UNZIPPED=$(find "$MAVEN_HOME_PARENT" -maxdepth 1 -name "apache-maven-*" -type d | head -1)
    mv "$UNZIPPED" "$MAVEN_HOME"
fi

MAVEN_CMD="$MAVEN_HOME/bin/mvn"

exec "$MAVEN_CMD" "$@"
