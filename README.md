# Implementation of Generators with JDK21

This repositories contains code discussed in an article I wrote. The article can be found [here](https://www.linkedin.com/in/sekaiser/).

## What's in it
The package `loom.generators.mvysny` contains the generator as implemented in [mvysny/vaadin-loom](https://github.com/mvysny/vaadin-loom/tree/master). The developer also wrote a nice [article](https://mvysny.github.io/java-generators/) explaining how it works.

The package `loom.generators.robaho` contains the generator as implemented in [robaho/generators](https://github.com/robaho/generators). He also provides a [synchronous version](https://github.com/robaho/generators/tree/synchronous). 

The package `loom.generators.sk4is3r` contains a generator implemented by myself. This generator is based on `jdk.internal.vm.Continuation`. Later, I figured out that someone else already had this idea and provided a decent implementation of continuations based generators in JDK21. Therefore, I highly recommend that you check out his [pedrolamarao/generators-jvm](https://github.com/pedrolamarao/generators-jvm).

Although not explicitly mentioned in my article, the package `loom.generators.kelemen` contains another custom implementation of a queue based generator. The article does not cover it, because another queue based generator is already discussed and the implementation depends on a couple of custom 3rd party dependencies. Discussing them is out of scope of the article. 

## Setup

You will need JDK21 to run this project.
I recommend using sdkman, e.g. `sdk install java  21.ea.31-open`

This project uses gradle 8.2.1.
You can install gradle via homebrew, e.g. `brew install gradle`

## Build

Just run `gradle build`.

Note: The *loom.generators* module requires JDK version 21 with preview features enabled.

## Results of performance test
The code is located in `loom.generators.perf`.

| Implementation           | avg time  |
|--------------------------|-----------|
| No generator             | 3.1 ms    |
| loom.generators.robaho   | 1258.3 ms |
| loom.generators.mvysny   | 432.5 ms  |
| loom.generators.sk4is3r  | 156.7 ms  |
| loom.generators.kelemen  | 3400.6 ms |
