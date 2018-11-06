# Implementation of the MicroProfile Reactive Streams Operator specification


**Documentation:** https://www.smallrye.io/smallrye-reactive-streams-operators/


## Getting started

Check the [Getting Started section](https://www.smallrye.io/smallrye-reactive-streams-operators/#_getting_started) 
from the documentation.

You can also look at the:
 
* [Quickstart project](examples/quickstart)
* [Quickstart project with Vert.x](examples/quickstart-vertx)
* [Quickstart project with Camel](examples/quickstart-camel)


## Built With

* RX Java 2
* Apache Vert.x (optional)


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

