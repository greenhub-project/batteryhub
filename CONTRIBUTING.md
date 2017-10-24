# How to contribute

Want to contribute to BatteryHub? Excellent! We're always happy to have more contributors. Before you start, please take a look at this document. 

The project is under *very active development*. We’re still working out the kinks to make contributing to this project as easy and transparent as possible, but we’re not quite there yet. Hopefully this document makes the process for contributing clear and answers some questions that you may have.


### [Code of Conduct](CODE_OF_CONDUCT.md)

The GreenHub project has adopted a Code of Conduct that we expect project participants to adhere to. Please read [the full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.

Here's a quick guide to create a pull request for your BatteryHub patch:

1. Fork the github project by visiting this URL: https://github.com/greenhub-project/battery-hub/fork

2. Clone the git repository

        $ git clone git@github.com:YOUR-GITHUB-USERNAME/batteryhub.git

3. Create a new branch in your git repository (branched from `develop` - see [Notes about branching](#branching) below).

        $ cd batteryhub/
        $ git checkout develop
        $ git checkout -b issue/123-fix-for-123 # use a better title

4. Setup your build environment (see [build instructions in our README](README.md#build-instructions)) and start hacking the project. You must follow our [code style guidelines](CODESTYLE.md), write good commit messages, comment your code and write automated tests.

5. When your patch is ready, [submit a pull request](https://github.com/greenhub-project/batteryhub/compare/). Add some comments or screen shots to help us.

6. Wait for us to review your pull request. If something is wrong or if we want you to make some changes before the merge, we'll let you know through commit comments or pull request comments.

### Branch Organization

We will do our best to keep the [`master` branch](https://github.com/greenhub-project/batteryhub/tree/master) in good shape, with tests passing at all times. represents latest version released in the Google Play Store.

- If you send a pull request, please do it against the `master` branch. We maintain stable branches for major versions separately but we don't accept pull requests to them directly. Instead, we cherry-pick non-breaking changes from master to the latest stable major version.

- `dev` branch represents the cutting edge version. This is probably the one you want to fork from and base your patch on.

- Fix or feature branches. Proposed new features and bug fixes should live in their own branch. Use the following naming convention: `fix-name-of-fix` for fix branches and `feature-name-of-feature` for features.

### Semantic Versioning

BatteryHub follows [semantic versioning](http://semver.org/). We release patch versions for bugfixes, minor versions for new features, and major versions for any breaking changes. When we make breaking changes.

We release new patch versions every few weeks, minor versions every few months, and major versions are unplanned.

You can read about the changes in the [releases page](https://github.com/greenhub-project/batteryhub/releases).

### Bugs

#### Where to Find Known Issues

We are using [GitHub Issues](https://github.com/greenhub-project/batteryhub/issues) for our public bugs. We keep a close eye on this and try to make it clear when we have an internal fix in progress. Before filing a new task, try to make sure your problem doesn't already exist.

### License

By contributing to BatteryHub, you agree that your contributions will be licensed under its Apache 2.0 license.

### What Next?

Read the [documentation](https://docs.greenhubproject.org) to learn how the project codebase is organized.