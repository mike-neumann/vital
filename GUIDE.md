# Guide

To use Vital, you will need to clone its GitHub repository and install via Gradle.

When using IntelliJ IDE you can use Vital's shipped run configuration to install to your local m2.

When _**NOT**_ using IntelliJ, you can also run `./gradlew clean publishToMavenLocal` inside the root project folder to
build and install to local m2.

Vital requires the following dependencies:

- essentia-inject :: me.xra1ny.essentia:essentia-inject >> github project