# How to contribute

Want to contribute to BatteryHub? Great! We're always happy to have more contributors. Before you start, please take a look at this document. 

The project is under **development**. We’re still working out the kinks to make contributing to this project as easy and transparent as possible, but we’re not quite there yet. Hopefully this document makes the process for contributing clear and answers some questions that you may have.


## Code of Conduct

The GreenHub project has adopted a Code of Conduct that we expect project participants to adhere to. Please read [the full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.

## Branch Organization

We will do our best to keep the [`master` branch](https://github.com/greenhub-project/batteryhub/tree/master) in good shape, with tests passing at all times. It represents the latest version released in the Google Play Store.

- If you send a pull request, please do it against the `master` branch. We maintain stable branches for major versions separately but we don't accept pull requests to them directly. Instead, we cherry-pick non-breaking changes from master to the latest stable major version.

- `dev` branch represents the cutting edge version, which will become the next major release (v2.*). It is not advisable to fork this branch at the moment since it will be subject to a complete rewrite of some components.

- `1.2` branch represents the current maintained version. This is probably the one you want to fork from and base your patch on.

- Fix or feature branches. Proposed new features and bug fixes should live in their own branch. Use the following naming convention: 
    - `fix-name` for fixes
    - `feature-name` for features
    
  Where `name` is a short description of the fix/feature of the respective branch.

## Semantic Versioning

BatteryHub follows [semantic versioning](http://semver.org/). We release patch versions for bugfixes, minor versions for new features, and major versions for any breaking changes. When we make breaking changes.

We release new patch versions every few weeks, minor versions every few months, and major versions are unplanned.

You can read about the changes in the [releases page](https://github.com/greenhub-project/batteryhub/releases).

## Bugs

### Where to Find Known Issues

We are using [GitHub Issues](https://github.com/greenhub-project/batteryhub/issues) for our public bugs. We keep a close eye on this and try to make it clear when we have an internal fix in progress. Before filing a new task, try to make sure your problem doesn't already exist.

### Reporting New Issues

The best way to get your bug fixed is to provide a description with the steps to reproduce the problem and copy the print stack tree output from the logs. You can fill a new issue [here](https://github.com/greenhub-project/batteryhub/issues/new).

## How to Get in Touch

- Email: [dev@hmatalonga.com](mailto:dev@hmatalonga.com)

## Style Guide

You can find more information about the code style convention [here](CODESTYLE.md).

## License

By contributing to BatteryHub, you agree that your contributions will be licensed under its [Apache 2.0 License](https://opensource.org/licenses/Apache-2.0).

## What Next?

Read the [documentation](https://docs.greenhubproject.org) to learn how the project codebase is organized.