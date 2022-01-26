# Contributing to Vividus
Thank you for considering a contribution to Vividus! This guide explains how to:
* maximize the chance of your changes being accepted
* work on the Vividus code base
* get help if you encounter trouble


## Follow the Code of Conduct
Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md).
By participating in this project you agree to abide by its terms.

## Making Changes
* Fork the repository on GitHub if you haven't done it before.
* Create a topic branch from where you want to base your work (this is usually the `master` branch).
* Respect the original code style: we use [Checkstyle](http://checkstyle.sourceforge.net/), [SpotBugs](https://spotbugs.github.io/), [Spotless](https://github.com/diffplug/spotless) to enforce a common code style:
  * Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.
  * Check for unnecessary whitespace with `git diff --check` before committing.
* Follow [the code conventions](#code-conventions)
* The commit messages that accompany your code changes are an important piece of documentation, follow these guidelines when creating commits:
  * Keep commits discrete: avoid including multiple unrelated changes in a single commit
  * Keep commits self-contained: avoid spreading a single change across multiple commits. A single commit should make sense in isolation
* Make sure you have added the necessary unit, intergration and/or system tests for your changes.
* If your changes introduce new functionlity which will be available for end users or affects already existing features, do not forget to add or update the corresponding sections in the documentation.
* Run all the checks and tests with `./gradlew clean build` to assure nothing else was accidentally broken.

## Submitting Changes
* Push your changes to a topic branch in your fork of the repository.
* Submit a pull request to the original repository. We will rebase/merge it to the maintenance branches, if necessary.
* After you submit your pull request, a Vividus core developer will review it. It is normal that this takes several iterations, so don't get discouraged by change requests. They ensure the high quality that we all enjoy.

## Code Conventions
### Unit tests

* Business logic that is related to unit tests must not be placed within methods annotated with `@BeforeEach` and `@AfterEach` annotations, such methods can only be used for common logic such as initializing mock service for testing, clean up filesystem resources etc.

### Dependency Injection

* Use [Constructor-based Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection) for beans (actions, factories, etc.).
* Use [Constructor-based Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection) or [Setter-based Dependency Injection](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-setter-injection) to inject properties.
* Use XML-based configuration to configure beans, the configuration file should be named `spring.xml` and placed within `org.vividus` package or at the package root.
* The declaration order of properties and beans in constructor should be as follows:
  1. properties
  1. beans like actions, factories, managers etc.
  1. validation beans

## Bug reports

We use the issue tracker on Github. Please report new bugs at <https://github.com/vividus-framework/vividus/issues/new/choose>.
When filing a bug report, please provide as much information as possible, so that we can reproduce the issue.

## Questions

There are various channels, on which you can ask questions:
* On [Slack](https://vividus-support.herokuapp.com/)
* Create an issue for your question at <https://github.com/vividus-framework/vividus/issues/new/choose>.
