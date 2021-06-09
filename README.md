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

The project is made up of 4 modules, 2 of which are services, 1 is a client, and 1 is a library.

There is also a set of three docker containers, 2 for running kafka (kafka and zookeeper) and 1 for a mongodb database.

### `core-service`

- receives loan offer requests on a `/loan-offer` endpoint, parses them and puts them on a kafka topic, `loan-offers-in`
- receives loan request requests on a `/loan-request` endpoint, parses them and puts them on a kafka
  topic, `loan-requests-in`
- receives `loan-available-events` from the kafka topic `loans-available` and stores them in an in-memory map
- receives requests to get loans, either as a list of those that currently exist, or as a stream of server-sent events

### `loan-offers-service`

- receives loan offer commands from a kafka topic `loan-offers-in` and stores them in the mongodb database
- receives loan request commands from a kafka topic `loan-requests-in`. Upon receiving this command  
  the service reads the loans database, calculates availability and puts an `loan-available-event` on
  the `loans-available` kafka topic.
  - The way the service gets the lowest offers is by returning queries from the database ordered by rate and picking the
    first n loan offers enough to cover the balance.
- receives delete-record requests on a `/delete-records` endpoint. This deletes all records in the database, thus
  setting it back to fresh.

### `loan-client`

- Contains a `loan-client` that can be used to send requests to the services. See
  the [Operation of loan client CLI](#Operation-of-loan-client-CLI) section below for more information on how to use
  this.

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

The project can be built using the following command from the root of the project

```shell
./mvnw clean install
```

This will build the project and run all tests.

> NB: Please be aware that for the services in this project the jar is not an uber-jar (except for the `loan-client`)
> and so is not runnable by itself, and instead must be run using the quarkus run jar.
>

#### Linting

Linting is provided by `spotless` ([link](https://github.com/diffplug/spotless)). This is not built into the ordinary
build process as a step. Rather it must be running using:

### Running the application in prod mode locally

To run application using docker run:

```shell
docker-compose -f setup/docker-compose.yaml up --remove-orphans --force-recreate --build
```

This will build and start the docker containers. Note that you must have built the jars beforehand
using `mvn clean install`!

When you're done you can clean up the containers. For cleaning up the containers afterwards use:

```shell
docker-compose -f setup/docker-compose.yaml down --remove-orphans
```

### Running the application in dev mode locally

To run the application in dev mode you first need to start the kafka stubs:

```shell
docker-compose -f setup/docker-compose-dev.yaml up --remove-orphans --force-recreate --build
```

You then need to open new terminals/shells and start the services, one in each terminal:

**Core service:**

```shell
cd ./core-service
../mvnw compile quarkus:dev
```

**Loan offers service**

```shell
cd ./loan-offers-service
../mvnw compile quarkus:dev
```

Alternatively, intellij has functionality for running quarkus configurations from 2020.3 onwards.

> **_NOTE:_**  Quarkus has a Dev UI, which is available in dev mode only. This can be found at
> http://localhost:8080/q/dev/ for the `core-service` and http://localhost:8081/q/dev/ for the `loan-offers-service`

When you're done, you can stop the app processes and clean up the containers. For cleaning up the containers afterwards
use:

```shell
docker-compose -f setup/docker-compose-dev.yaml down --remove-orphans
```

### Sending requests and interacting with the services

You can use the loan client to make requests to the services.

To run the client, first `cd` into the `loan-client` directory. Command line instructions and help are available when
you run `./zopa-rate`. The options presented are as follows:

```
Usage: zopa-rate [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  send-offers           Send a loan to the service
  loan-request          Create a request for a loan
  list-available-loans  List loans that are available and have been processed
                          on the service
  reset-records         Make a request to reset the currently stored records
```

#### Operation of loan client CLI

To send a csv file of loan offers, use the following command:

```shell
./zopa-rate send-offers -f offers.csv
```

A file of example loan offers is provided, called `offers.csv`

To send a loan-request run:

```shell
./zopa-rate loan-request -a 1700
```

This command will wait until getting a request back from the services.

> NB: You may have to exit this command with CTRL-C as it sets up a client to listen to a stream of server sent events, and may take up to about 15 seconds to disconnect.

You can view all the loan-available-events recorded by on the service using the `list-available-loans` command:

```shell
./zopa-rate list-available-loans
```

**_IMPORTANT:_** You must run the `reset-records` command in order to reset the loan-offers stored by the mongodb
database. This is so that you can run a different set of loan offers through the service.

#### Using the HTTP request file

There is also an HTTP request file that can be used directly in the `intellij` IDE to run requests. This is
the `./requests/loan_amount_example-endpoints.http` file

#### Viewing received messages from server sent events in the browser

There is also a web page available at `http://localhost:8080` which listens for server sent events - loan available
events.

To see this in operation, open the web page and then send a few requests using the `loan-client`, you should see the
requests come in, albeit unformatted.

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

- obviously this is overly complicated for what it does, but it does show kafka shows request response using an id (
  borrowerId/requesterId in this case, though this would be on a per-request basis in a real app), and hints at CQRS a
  little.
- would do proper event storming to come up with the events
- would have a proper delete record system this is obviously a bit of a cop out because in reality you probably wouldn't
  want to do this delete all at once at all
- doing a command line - you'd want to validate things more
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
- Authentication and sending back only certain events - sessions

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