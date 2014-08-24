# Mixin Maven Plugin

## Introduction

Mixin Maven Plugin is a Maven Extension that allows including multiple pom plugin Management sections without the need to inherit them from a single root parent.
By having this ability, SDK developers can provide mixin pom files that define the build behaviour. 
This feature let you organize your build behaviour project in a much more modular way. 

## What is a Mixin
A Mixin is a regular pom file that concentrate only on a single build aspect of a project. This means that it defines only the default properties and the pluginManagement of a specific aspect of the build. After defining a Mixin pom, you can embed it in any module build section, without copy/paste or defining it in a parent pom.

## Usage
In order to use this plugin, you first need to create a mixin pom or use existing one.
An example for a mixin pom can be located in [mixin example pom](../../../blob/master/examples/mixin-maven-plugin/mixin-example/mixin1/pom.xml)

Next step would be to consume this mixin in your consumer pom.
An example for a mixin consumer can be located in [mixin consumer example pom](../../../blob/master/examples/mixin-maven-plugin/mixin-example/mixin-consumer/pom.xml)

## Notes
Please pay attention to the fact that plugin versions must be versioned in the consumer pom. You can achieve that by using a parent pom that defines pluginManagement section that only defines plugin versions. Alternatively, you can define the version within the plugin declaration in the consumer pom.