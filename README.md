# jffi

Java bindings for libffi

## Building

ant jar && ant archive-platform-jar && mvn package

## Build notes for MacOS

Building the library for MacOS requires a version of Xcode that supports 32-bit configurations. As of this writing,
the most recent version that supports 32-bit is Xcode 9.4.1.

The 9.4.1 version of Xcode is available at the following URL (requires Apple developer login):

https://developer.apple.com/services-account/download?path=/Developer_Tools/Xcode_9.4.1/Xcode_9.4.1.xip
