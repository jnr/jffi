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
* Update all submodules (unsure of correct command)
* Use `git submodule foreach mvn versions:set -DremoveSnapshot` to remove snapshots from all versions.
  * Alternatively, only run this against the modules to be released (untested)
* Install all artifacts now with release versions using `mvn install`
* Update all artifacts to latest release versions of dependencies using `git submodule foreach mvn versions:use-release-versions`
* Commit and tag the pom changes
  * `git submodule foreach mvn versions:commit` which clears the backup version file
  * `git submodule foreach git add pom.xml` to add the pom changes
  * `git submodule foreach git commit -m 'Update version for release'` to commit the pom changes
  * `git submodule foreach mvn scm:tag -Dtag='${project.version}' -DpushChanges=false` to create tags for the release
* Bump version of the `release` project using the same process as above and add latest commits for all submodules.
  * 
* Release artifacts to Maven Central using `mvn deploy -Prelease`