#!/usr/bin/env bash

GIT_BRANCH=$1
FUSION_HOME=$2
FUSION_VERSION=$3
TARGET_PATH=$4

if [ -z "$1"  ] || [ -z "$2"  ] || [ -z "$3"  ]; then
    echo "WARN: Please provide arguments in the following order GIT_BRANCH FUSION_HOME FUSION_VERSION TARGET_PATH"
    echo "INFO: The fourth argument TARGET_PATH is optional"
    exit 1
fi

if [ ! -d "$FUSION_HOME" ]; then
    echo "WARN: Path provided to FUSION_HOME does not exist. Provide a valid path"
    exit 1
fi

git checkout "$GIT_BRANCH"
cd java-sdk/connectors/
./gradlew clean assemblePlugins -PfusionHome="$FUSION_HOME" -PfusionVersion="$FUSION_VERSION"

if [ -z "$TARGET_PATH" ] || [ ! -d "$TARGET_PATH" ]; then
    echo 'INFO: Fourth argument TARGET_PATH was not set or path does not exist. Not copying the demo zip to a target path'
    exit 1
fi

cp build/plugins/random-connector-0.1.1.zip "$TARGET_PATH"

