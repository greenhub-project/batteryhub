#!/bin/bash

# Exit on error
set -e

# Work off travis
if [[ ! -z TRAVIS_PULL_REQUEST ]]; then
  echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
else
  echo "TRAVIS_PULL_REQUEST: unset, setting to false"
  TRAVIS_PULL_REQUEST=false
fi

echo "Building GreenHub"

# Copy mock google-services file if necessary
if [ ! -f ./app/google-services.json ]; then
  echo "Using mock google-services.json"
  cp ./mock-google-services.json ./app/google-services.json
fi

# Build
if [ ! $TRAVIS_PULL_REQUEST ]; then
  # For a merged commit, build all configurations.
  GRADLE_OPTS=./gradlew clean build check
else
  # On a pull request, just build debug which is much faster and catches
  # obvious errors.
  GRADLE_OPTS=./gradlew clean assembleDebug check
fi