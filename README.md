JNR Release Project
===================

This project serves as an aggregate repository for releasing jnr-* subprojects.

It is not a parent project in the Maven sense; that would be [jnr-parent](https://github.com/jnr/parent.git) which provides a parent POM file and default build configurations for the rest of the JNR stack.

In this project you will find submodules for each repository in the JNR stack and a top-level POM file used for releasing them as a group.

This project was created to ease the release process for JNR projects, which are frequently released at the same time using the Sonatype Maven Central release process. This process does not provide a mechanism for incrementally releasing projects in separate builds, so we use this new aggregate POM to do them all at once.

Prerequisites
-------------

The release project requires a few steps to be usable:

* Ensure you have at least one JDK 17 and one JDK 8 JVM installed and configured in ~/.m2/toolchains.xml based on the structure described at https://maven.apache.org/guides/mini/guide-using-toolchains.html
* Fetch all submodules using `git submodule update --init`. This will fetch the most recent release version of each module.

Releasing
---------

Releases of the JNR stack follow this rough process:

* Ensure that all PRs are merged, issues closed, etc.
* Update all submodules using `git submodule update --remote`
* Use `mvn release:update-versions` to set release versions for all projects