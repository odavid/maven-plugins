# maven-plugins

General Purpose Maven Plugins

## Plugin list

- **antrun-maven-plugin** - An modified version of [maven-antrun-plugin](http://maven.apache.org/plugins/maven-antrun-plugin/), that uses Maven Logging as output instead of stdout
- **attach-report-maven-plugin** - A report plugin that let you use pre-created html files, such as surefire report instead of regenerating them in site phase
- **maven-logging-extension** - A Maven extension that adds slf4j MDC eye catcher (module-groupId:module-artifactId) to the log message for every log line. This helps to identify issues in a multithreaded build 
- **mojo-descriptor-maven-plugin** - A plugin descriptor generator for ant based mojos, that uses a simplified xml for defining ant based mojos
- **mixin-maven-plugin** - A Maven extension that let a pom developer import other pom files properties and pluginManagement sections without inherting them or copy/paste. See [mixin-maven-plugin](../../blob/master/mixin-maven-plugin/README.md)

## License
All code is licensed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

