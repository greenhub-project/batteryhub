FROM openjdk:8-jdk-alpine

MAINTAINER Hugo Matalonga <hmatalonga@gmail.com>

ENV ANDROID_HOME /opt/android-sdk-linux
ENV ANDROID_SDK_URL https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz

RUN apk add --no-cache curl ca-certificates bash

# Download Android SDK tools
RUN mkdir -p /opt && curl -sL ${ANDROID_SDK_URL} | tar xz -C /opt

ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

# platform tools
RUN echo y | android update sdk --no-ui --all --filter platform-tools | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter extra-android-support | grep 'package installed'

# SDK
RUN echo y | android update sdk --no-ui --all --filter android-24 | grep 'package installed'

# build tools
RUN echo y | android update sdk --no-ui --all --filter build-tools-24.0.3 | grep 'package installed'

# Android image for emulator
RUN echo y | android update sdk --no-ui --all --filter sys-img-armeabi-v7a-android-24 | grep 'package installed'

# extras
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'
