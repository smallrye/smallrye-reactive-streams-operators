# Implementation of the MicroProfile Reactive Streams Operator specification

-----

**IMPORTANT**: This repository is in _maintenance_ mode. 
No new features will be implemented. 
               
Another implementation of MicroProfile Reactive Streams Operators is available in [Mutiny](https://smallrye.io/smallrye-mutiny).
It is recommended to switch to this implementation.
               
Reactive Converters have been migrated to https://github.com/smallrye/smallrye-reactive-utils.
               
If you have any questions, send a message to https://groups.google.com/forum/#!forum/smallrye.

-----

**Documentation:** https://www.smallrye.io/smallrye-reactive-streams-operators/

[![Build Status](https://github.com/smallrye/smallrye-reactive-streams-operators/workflows/SmallRye%20Build/badge.svg?branch=master)]( https://github.com/smallrye/smallrye-reactive-streams-operators/actions?query=workflow%3A%22SmallRye+Build%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=smallrye_smallrye-reactive-streams-operators&metric=alert_status)](https://sonarcloud.io/dashboard?id=smallrye_smallrye-reactive-streams-operators)
[![License](https://img.shields.io/github/license/smallrye/smallrye-fault-tolerance.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven](https://img.shields.io/maven-central/v/io.smallrye.reactive/smallrye-reactive-streams-operators?color=green)]()

## Getting started

Check the [Getting Started section](https://www.smallrye.io/smallrye-reactive-streams-operators/#_getting_started) 
from the documentation.

You can also look at the:
 
* [Quickstart project](examples/quickstart)
* [Quickstart project with Vert.x](examples/quickstart-vertx)
* [Quickstart project with Camel](examples/quickstart-camel)


## Built With

* RX Java 2
* Eclipse Vert.x (optional)


## How to build

```bash
mvn clean install
```

To collect the code coverage:

```bash
mvn clean verify -Pcoverage
# to generate the report
cd implementation
mvn jacoco:report -Djacoco.dataFile=target/jacoco.exec -Pcoverage
```

The code coverage combines unit tests and TCK. The report is generated in the `target/site/jacoco/index.html`

## How to contribute

Just open a pull request. Makes sure to run the tests and the TCK before opening the PR. Don't forget that documentation 
and tests are as important as the code (if not more).

Please read [the contribution guidelines](CONTRIBUTING.md) for details, and the process for submitting pull requests. 

## Sponsors

The project is sponsored by Red Hat.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

