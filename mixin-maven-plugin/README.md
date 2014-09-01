# Mixin Maven Plugin

## Introduction

Mixin Maven Plugin is a Maven Extension that allows including multiple pom plugin Management sections without the need to inherit them from a single root parent.
By having this ability, SDK developers can provide mixin pom files that define the build behaviour. 
This feature let you organize your build behaviour project in a much more modular way. 

## What is a Mixin
A Mixin is a regular pom file that concentrate only on a single build aspect of a project. This means that it defines only the default properties and the pluginManagement of a specific aspect of the build. After defining a Mixin pom, you can embed it in any module build section, without copy/paste or defining it in a parent pom.

## Usage
In order to use this plugin, you first need to create a mixin pom or use an existing one.
An example for a mixin pom can be found [here](../../../blob/master/examples/mixin-maven-plugin/mixin-example/mixin1/pom.xml)

Next step would be to consume this mixin in your consumer pom.
An example for a mixin consumer can be found [here](../../../blob/master/examples/mixin-maven-plugin/mixin-example/mixin-consumer/pom.xml)

The full example (including a base parent pom) can be downloaded from [here](../../../tree/master/examples/mixin-maven-plugin/mixin-example/)

## Notes
- Please pay attention to the fact that plugin versions must be versioned in the consumer pom. You can achieve that by using a parent pom that defines pluginManagement section that only defines plugin versions. Alternatively, you can define the version within the plugin declaration in the consumer pom.

## A Word About Mixin Merging Strategy
When merging mixins with a pom, the following merging order is taking place:
- The mixin consumer is the first one in the chain
- The parent pom of the consumer is next
- The mixin pom files by their order
- Recursive mixin pom files with precedence to their location in the tree 

## A Word About Profile Activation
Mixin pom files can contribute profiles to the mixin consumer. The following logic is taking place when merging profiles:
- If a profile is declared and activated within the mixin consumer, then the mixin profile with the same id is also activated regardless the activation condition declared within the mixin pom file
- If a profile is only declared within the mixin consumer, without activation tag and the mixin pom contains activation condition that is evaluated as true, then the profile is being activated within the mixin consumer
- If a profile is not declared within the mixin consumer, and the mixin pom contains a profile that its activation condition is true, then the profile will be activated within the consumner