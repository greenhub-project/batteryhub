<p align="center">
<img src="https://raw.githubusercontent.com/greenhub-project/greenhub-assets/master/github_header.png" title="GreenHub BatteryHub">
</p>
<p align="center">
<a href="https://travis-ci.org/greenhub-project/batteryhub"><img src="https://travis-ci.org/greenhub-project/batteryhub.svg?branch=master" alt="Build Status"></a>
<a class="badge-align" href="https://www.codacy.com/app/hmatalonga/batteryhub"><img src="https://api.codacy.com/project/badge/Grade/c87e12f6973248e3bf66f2d34185bdf8"/></a>
<a href="https://github.com/greenhub-project/batteryhub/releases/latest"><img src="https://img.shields.io/github/release/greenhub-project/batteryhub.svg" alt="Release"></a>
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"></a>
</p>

## About GreenHub BatteryHub

BatteryHub is a simple way to keep up with your device's battery details. The app works as a battery monitor compiling all useful information necessary for you to identify any energy anomalies. Keep in mind that the app by itself is NOT a battery saver, at least for now. It simply aggregates data to keep you informed of the battery details.

Use BatteryHub to:
- Monitor your battery details in real-time such as instant consumption and battery temperature and more.
- Check your device specifications, network information, storage details and memory usage.
- Find out what apps are running and how much memory they are using.
- Keep track of your battery state history with interactive charts with different time intervals.

**Disclaimer:** This app is still under development and it is NOT finished. Updates will be released over time with new content.

BatteryHub is a fork of the Carat Project and is part of the project *GreenHub*.

## What is GreenHub?

 > A collaborative approach to power consumption analysis of Android devices.
 
 It collects anonymous data about your devices's power consumption and uploads them to our server.

Learn more at [greenhubproject.org](https://greenhubproject.org).

## Ecosystem

| Project | Status | Description |
|---------|--------|-------------|
| [greenhub-farmer]     | [![greenhub-farmer-status]][greenhub-farmer-package]         | Backend Web application + REST API                            |
| [greenhub-lumberjack] | [![greenhub-lumberjack-status]][greenhub-lumberjack-package] | A command line app for interacting with the GreenHub REST API |

[greenhub-farmer]: https://github.com/greenhub-project/farmer
[greenhub-lumberjack]: https://github.com/greenhub-project/lumberjack

[greenhub-farmer-status]: https://img.shields.io/github/release/greenhub-project/farmer.svg
[greenhub-lumberjack-status]: https://img.shields.io/npm/v/greenhub-cli.svg

[greenhub-farmer-package]: https://greenhub.di.ubi.pt
[greenhub-lumberjack-package]: https://npmjs.com/package/greenhub-cli

## Build Instructions

### Gradle

#### Linux and Mac OS:

Make sure `gradlew` is executable, by typing this command:
```shell
$ chmod +x gradlew
```

You can now build and test the project:
```shell
$ ./gradlew clean check assembleDebug
```

#### Windows:

```shell
$ gradlew.bat clean check assembleDebug
```

### Docker

If it is the first time building the Android app with Docker, it is necessary to build a local Docker image before running it. Afterwards just run a container:

**Note:** Make sure you have at least 2GB free storage on your workstation to build the docker image.

```shell
$ curl https://raw.githubusercontent.com/hmatalonga/docker-android/master/Dockerfile -sSf | docker build -t greenhub-project/batteryhub -f - .
$ docker run -it --name container-name \         # Choose a container name
-v `pwd`:/usr/src/app \                          # DO NOT change this src path
greenhub-project/batteryhub \
/bin/bash
```
Inside the container you can now run gradle tasks, for example:

```shell
./gradlew clean check assembleDebug  # This will create a debug build after passing all checks
```

To start an existing container, type:
```shell
$ docker start -i container-name
```

One line build, for copy-paste:
```shell
curl https://raw.githubusercontent.com/hmatalonga/docker-android/master/Dockerfile -sSf | docker build -t greenhub-project/batteryhub -f - . && docker run -it --name batteryhub-app -v `pwd`:/usr/src/app greenhub-project/batteryhub /bin/bash
```

To list all available gradle tasks run `./gradlew tasks`.

### Need help?

Having problems building BatteryHub? Please see the [Troubleshooting guide](https://github.com/greenhub-project/batteryhub/wiki/Troubleshooting).

For more details, please check our [documentation](https://docs.greenhubproject.org).

## Issues

If you think you have found a bug or have a feature request, refer to the [issues page](https://github.com/greenhub-project/batteryhub/issues), proper labels are provided.

Before opening a new issue, be sure to search existing ones to avoid duplicates. Please try to include steps to reproduce the problem and copy the print stack tree output from the logs if necessary.

### Known issues

- Some Android devices don't support current measurement with BatteryManager API. Legacy support is being developed
- Power Indicator is not working properly, see [issue](https://github.com/greenhub-project/batteryhub/issues/40)

## Contributing

Please read through our [contributing guidelines](.github/CONTRIBUTING.md) before making a pull request. Included are directions for coding standards and development process.

## Changelog

Detailed changes for each release are documented in the [release notes](https://github.com/greenhub-project/batteryhub/releases).

## License

Copyright (c) 2016-present Hugo Matalonga & João Paulo Fernandes.

The code is available under the [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0) unless otherwise stated in the file or by a dependency's license file.

## Acknowledgments

GreenHub was originally inspired by and has used data definitions from:

- [https://github.com/carat-project/carat-android](https://github.com/carat-project/carat-android) - Copyright (c) 2011-2016, AMP Lab and University
of Helsinki All rights reserved.

It uses battery percentage icons from the project:

- [https://github.com/thuetz/Energize](https://github.com/thuetz/Energize).
