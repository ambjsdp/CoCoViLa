# CoCoViLa (Compiler Compiler for Visual Languages) 
is a framework for design and implementation of visual specification languages, where the visual specification could be directly translated into executable code.

Homepage: http://www.cs.ioc.ee/cocovila

### Building:
(requires Apache Maven http://maven.apache.org/)

`cd <path to CoCoViLa>/`

`mvn clean install`

### Running CoCoViLa Scheme Editor:

`mvn exec:exec -Prun-se`

### Running CoCoViLa Class Editor:

`mvn exec:exec -Prun-ce`


### Continuous Integration
[![Build Status](https://travis-ci.org/CoCoViLa/CoCoViLa.svg?branch=master)](https://travis-ci.org/CoCoViLa/CoCoViLa)
