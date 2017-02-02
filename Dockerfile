FROM ubuntu:16.04

MAINTAINER Hugo Matalonga <hmatalonga@gmail.com>

# Docker Android image for development environment

ENV ANDROID_HOME /opt/android-sdk-linux
ENV ANDROID_SDK_URL https://dl.google.com/android/repository/tools_r25.2.3-linux.zip

RUN dpkg --add-architecture i386
RUN apt-get update -qq
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y curl openjdk-8-jdk libc6:i386 libstdc++6:i386 libgcc1:i386 libncurses5:i386 libz1:i386

# Download Android SDK tools
RUN mkdir -p /opt && curl -sL ${ANDROID_SDK_URL} | tar xz -C /opt

ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

# platform tools
RUN echo y | android update sdk --no-ui --all --filter platform-tools | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter extra-android-support | grep 'package installed'

# SDK
RUN echo y | android update sdk --no-ui --all --filter android-25 | grep 'package installed'

# build tools
RUN echo y | android update sdk --no-ui --all --filter build-tools-25.0.2 | grep 'package installed'

# For the moment an Android image for emulator is not necessary
# RUN echo y | android update sdk --no-ui --all --filter sys-img-armeabi-v7a-android-25 | grep 'package installed'

# extras
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'

# Cleaning
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
