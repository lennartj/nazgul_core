# [1. Nazgul Framework: Core](https://lennartj.github.io/nazgul_core)

> "[Nazgul Core](https://lennartj.github.io/nazgul_core) contains libraries helping me build
> applications quicker and better."

The Nazgul Framework project holds a collection of best-pracises and sensible configurations enabling you to start
projects quickly and scale them considerably without having to change the development or deployment model.
Moreover, the Nazgul Framework strives to increase code quality, maintainability and usability for the developers
and architects working on a project, as well as reduce complexity/tanglement and increase productivity.

The Nazgul Framework consists of software components split between two reactors:

1. **[Nazgul Framework: Tools](https://github.com/lennartj/nazgul_tools)**. The Nazgul Tools project (another reactor)
    aims to use best-of-breed tools to achieve a usable, well-composed and simple mode of development and deployment.
    It defines codestyle, IDE integration, Maven plugins and a commonly usable way to automatically validate
    state for any class in any project.

2. **[Nazgul Framework: Core](https://github.com/lennartj/nazgul_core)**. The Nazgul Core project (this reactor)
    provides a set of libraries (i.e. "JARs"), built using the Nazgul Tools codestyle. These JARs have small footprints
    provide well-defined tasks and are ready for immediate use in any project.

## 1.1. Release Documentation

Release documentation (including Maven site documentation) can be found
at [The Nazgul Framework: Core Documentation Site](https://lennartj.github.io/nazgul_core).
Select the release version you are interested in, to find its full Maven site documentation.

# 2. Getting and building nazgul_core

The nazgul_core is a normal Git-based Maven project, which is simple to clone and quick to build.

## 2.1. Getting the repository

Clone the repository, and fetch all tags:

```
git clone https://github.com/lennartj/nazgul_core.git

cd nazgul_core

git fetch --tags
```

## 2.2. Building the Nazgul Core project

For the latest development build, simply run the build against the latest master branch revision:

```
mvn clean install
```

For a particular version, checkout its release tag and build normally:

```
git checkout nazgul-core-2.0.0

mvn clean install
```

All tags (and hence also all release versions) are visible using the command

```
git tag -l
```

### 2.2.1. Building with different Maven versions

For building the project with another Maven version, simply run the following
script, where the `${MAVEN_VERSION}` should be substituted for a version number
such as `3.3.3`:

```
mvn -N io.takari:maven:wrapper -Dmaven=${MAVEN_VERSION}

./mvnw --show-version --errors --batch-mode validate dependency:go-offline

./mvnw --show-version --errors --batch-mode clean verify site
```

In the windows operating system, use `mvnw.bat` instead.