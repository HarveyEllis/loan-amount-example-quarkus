# Loan example amount quarkus

The purpose of the project is to take a list of loan offers with rates and amounts, and then validate that a loan
request can be fulfilled based on that list of offers.

This could of course be done using a simple command line interface, and in a few hundred lines of code, but that would
not be enterprisey! On a more serious note it would also not be useable as a service deployed to a company's
infrastructure.

The other purpose of this project is to demonstrate and test out a bunch of modern java based tooling and see how that
might create a better workflow. These tools include [quarkus](https://quarkus.io/) and its integration
with [kafka](http://kafka.apache.org/).

## Project structure

This project contains a number of services each of which performs a specific purpose. A depiction of the full
architecture can be seen below:

![diagram](./docs/img/Loan%20amount%20example.png)

### `core-service`

- receives loan offer requests on a `/loan-offer` endpoint, parses them and puts them on a kafka topic, `loan-offers-in`
- receives loan request requests on a `/loan-request` endpoint, parses them and puts them on a kafka
  topic, `loan-requests-in`
- receives `loan-available-events` from the kafka topic `loans-available` and stores them in an in-memory map
- receives requests to get loans, either as a list of those that currently exist, or as a stream of server-sent events

### `loan-amount-domain`

- Contains models that would otherwise be duplicated across the other modules

## Developing

### Requirements

There are a number of requirements for developing the project. These include:

- java 11+ (used adoptopenkjdk 11.0.11+9, note that there is an error with the spotless plugin when using java
  16, [see here](https://github.com/diffplug/spotless/issues/834))
- docker with docker-compose (used docker-desktop 3.3.3)
- maven 3+ (optional - can use warpper instead, used 3.8.3)
- mongosh (optional - only for validating operation within mongo database, used 0.12.1)

### Building

The project can be built using the following command:

```shell
./mvnw clean install
```

### Running

Two docker compose files

> Note that when running in quarkus:dev mode some of the kafka functionality can be a lot slower - on the order of 500ms rather than around 30-50ms. This is especially the case when using WSL.

## architecture

- members service
-

## events

member created account added payment made loan offered - gets stored in loan offers service loan requested loan accepted
by offerer loan accepted by requestor loan fulfilled - how would you go about doing this?

loans/offer loans/request

Uses convention of "in" for commands and "out" for things that would notify the user

# Caveats

There are a number of caveats

Architecture and design

- obviously this is overly complicated for what it does, but it does show kafka
- would do proper event storming to come up with the events
- would have a proper delete record system
- might not go this granular - have a single loans service?
- you'd probably want to do way more validation on the balances and accounts if you were going to do loans properly -
  want to have some kind double entry bookkeeping system really?
- maybe you'd have libraries for things like the domain model, and not put it in the same repo as the actual services

Deployments

- the properties in each of
- would have a helm chart, jenkinsfile or something else to deploy it wit

Testing

- would do more testing
- would want to decide on tests - cucumber? or just system tests?

Project structure

## Potential future directions

- Fulfilment and payments stubs
- Making offers to people
- Storing those offers

- the rationale for not doing this is that you would have the requests looking in the offers table which is something I
  don't think should really happen.

things like payments would do fulfilment etc

accounts and stuff would be protected

secrets wouldn't just be stored like that

infrastructure setup would be different - you'd deploy it to something like openshift using argocd

might put in separate repos?



What about making offers to multiple people?

- you'd want to send out multiple offers? Or you'd lock some offers for a certain amount of time?

I'd probalby put more builders on classes, and also make some of the accesses private and final in this case

The reason for Strings - makes it easier for conversion to kafka and vice versa but want to use bigdecimal for anything
to do with calculating money, or alternatively it might be viable to use the java currency types

## Testing rationale

Done a bit of testing on the numeric functions, but I haven't done tests for things like nulls, or divices by zeros. In
a real corporate context this would be probably done by an in house library and you wouldn't be doing all te

I've tried to show a few different things for testing: 
- using quarkus test
- using integration tests to run a whole lot of the app at once, and for spinning up kafka
- using various standard 'enterprisey' libraries such as `assertj`, `equalsVerifier` and `mockito`.

## Requires

maven v3+ java 11+ docker-compose docker

## Caveats and deviations


## Additional future directions

## Inspecting operation

```shell
docker-compose -f setup/docker-compose-dev.yaml up --remove-orphans --force-recreate --build
```
Clean up afterwards:
```shell
docker-compose -f setup/docker-compose-dev.yaml down --remove-orphans
```

../mvnw quarkus:dev -Dquarkus.http.host=0.0.0.0

### database

When the database is running, login with:

```shell
mongosh -u admin -p some-pw
```

Then use the following commands to inspect the objects that are stored in there.

```shell
use loans 
db.auth("db-user", "some-password")
show collections 
db.LoanOffer.find()
```

### Building a native loan client executable

```shell
cd loan-client
../mvnw package -Dnative -Dquarkus.native.container-build=true
```

This will take a few minutes. It also requires being able to pull the required graalvm docker image from dockerhub. See
the [quarkus docs](https://quarkus.io/guides/maven-tooling) for more information on providing a specific docker image to
use when building.

### Kafka

Exec into the kafka container:

```shell
docker exec -it setup_kafka_1 /bin/bash
```

```shell
cd bin
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic loans-available --from-beginning
```

## PLan?

- create multimodule so that it can run all at once
- use jbang for creating a ./zopa-loans script and compiling all at once
- cucumber tests?
- jacoco coverage
- hibernate bean validation for their requirements