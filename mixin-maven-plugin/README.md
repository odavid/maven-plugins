# Maven Mixin Plugin

## Introduction

Mixin Maven Plugin is a Maven Extension that allows including multiple pom plugin Management sections without the need to inherit them from a single root parent.
By having this ability, SDK developers can provide mixin pom files that defines the build behaviour. 
This feature let you organize your build behaviour project in a much more modular way. 

## What is a Mixin
A Mixin is a regular pom file that concentrate only on a single build aspect of a project. This means that it defines only the default properties and the pluginManagement of a specific aspect of the build. After defining a Mixin pom, you can embed it in any module build section, without copy/paste or defining it in a parent pom.



