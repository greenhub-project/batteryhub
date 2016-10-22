# GreenHub

[![Build Status](https://travis-ci.org/hmatalonga/greenhub.svg?branch=master)](https://travis-ci.org/hmatalonga/greenhub)

GreenHub is a collaborative approach to power consumption analysis of mobile devices.

GreenHub is a fork of the Carat Project.

Learn more at [hmatalonga.github.io/greenhub](hmatalonga.github.io/greenhub).

The Android app is available [here](https://github.com/hmatalonga/greenhub/releases/download/v1.0-alpha.1/greenhub-v1.0-alpha.1.apk).

**Disclamer:** The project is still under heavy development, at the moment is in alpha stage. Currently no data is being uploaded since the web server is not online yet.

## Build Instructions

### Gradle
#### Linux and Mac OS:
Make sure `gradlew` is executable, by typing this command:
```shell
$ chmod +x gradlew
```

You can now build and test the project:
```shell
$ ./gradlew assembleDebug
$ ./gradlew check
```

#### Windows:
```shell
$ gradlew.bat assembleDebug
$ gradlew.bat check
```


### Docker
If it is the first time building the Android app with Docker, it is necessary to build a local Docker image before running it. Afterwards just run a container:
```shell
$ docker build -t hmatalonga/greenhub-android . # Only necessary for first build
$ docker run -it --name container-name \        # Choose a container name
-v `pwd`:/usr/src/app \                         # DO NOT change this src path
hmatalonga/greenhub-android \
/bin/sh -c \
"./gradlew assembleDebug; ./gradlew check"
```

To execute another gradle task, simply create a new container:
```shell
$ docker run -it --name container-name \        # Choose another container name
-v `pwd`:/usr/src/app \                         # DO NOT change this src path
hmatalonga/greenhub-android \
/bin/sh -c \
"./gradlew task-name"
```

To start an existing container, type:
```shell
$ docker start -i container-name
```

One line build, for copy-paste:
```shell
docker build -t hmatalonga/greenhub-android . && docker run -it --name greenhub-app -v `pwd`:/usr/src/app hmatalonga/greenhub-android /bin/sh -c "./gradlew assembleDebug; ./gradlew check"
```

To list all available gradle tasks run `./gradlew tasks`.

#### Need help?
Having problems building GreenHub? Please see the [Troubleshooting guide](https://github.com/hmatalonga/greenhub/wiki/Troubleshooting).

For more details, please check our [wiki](https://github.com/hmatalonga/greenhub/wiki).

## Issues
If you think you have found a bug or have a feature request, refer to the [issues page](https://github.com/hmatalonga/greenhub/issues), proper labels are provided.
Before opening a new issue, be sure to search existing ones to avoid duplicates. Please try to include steps to reproduce the problem.

### Known issues
- The Android app can't communicate with the web server using mobile data Internet connection;

## Contributing
Please read through our [contributing guidelines](CONTRIBUTING.md). Included are directions for opening issues, coding standards and development process.

### Code of Conduct
GreenHub has adopted a code of conduct that we expect project participants to adhere to.
Please read the [full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.

## License
Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes.

The code is available under the [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0) unless otherwise stated in the file or by a dependency's license file.

### Acknowledgments
GreenHub was originally inspired by and has used data definitions from:

- [https://github.com/carat-project/carat-android](https://github.com/carat-project/carat-android) - Copyright (c) 2011-2016, AMP Lab and University
of Helsinki All rights reserved.
