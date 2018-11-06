# Implementation of the MicroProfile Reactive Streams Operator specification


**Documentation:** https://www.smallrye.io/smallrye-reactive-streams-operators/

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