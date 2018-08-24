# Implementation of the MicroProfile Reactive Streams Operator specification


## How to use

Add the artifact into your classpath. For Maven, use:

```xml
<dependency>
  <groupId>io.smallrye</groupId>
  <artifactId>smallrye-reactive-streams-operators</artifactId>
  <version>${VERSION}</version>
</dependency>
```

Then, in your code, just use it:

```java
CompletionStage<Integer> stage = ReactiveStreams.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .filter(i -> i % 2 == 0)
      .collect(Collectors.summingInt(i -> i)).run();
```

## Threading model

This implementation is based on RX Java 2 and Eclipse Vert.x. If you are using it in a Vert.x application it enforces the 
Vert.x threading model automatically. So operations triggered from a Vert.x context (event loop or worker) are always 
handled in the same context, even if stream emissions are done in another thread. 

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