# Loan example amount quarkus

The purpose of the project is to take a list of loan offers with rates and amounts, and then validate that a loan
request can be fulfilled based on that list of offers.

This could of course be done using a simple command line interface, and in a few hundred lines of code, but that would
not be enterprisey! On a more serious note it would also not be useable as a service deployed to a company's
infrastructure.

The other purpose of this project is to demonstrate and test out a bunch of modern java based tooling and see how that
might create a better workflow. These tools include [quarkus](https://quarkus.io/) and its integration
with [kafka](http://kafka.apache.org/). The project makes quite heavy use of reactive programming (which is offered by
quarkus) including [microprofile](https://microprofile.io/) and [mutiny](https://smallrye.io/smallrye-mutiny).

The project has been heavily influenced
by [red hats coffee shop demo - quarkus-cafe-demo](https://github.com/jeremyrdavis/quarkus-cafe-demo) and draws on much
of the [quarkus documentation](quarkus.io/guides/).

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

> NB: This project uses convention of "in" for commands. If there were events that were going explicitly for user
> consumption then the stream would be called out. It is not called that because `loans-available` would actually be an
> internal stream in a real world application, and instead you'd have something like `loans-notifications-out` for updating
> the user.

### `loan-offers-service`

- receives loan offer commands from a kafka topic `loan-offers-in` and stores them in the mongodb database
- receives loan request commands from a kafka topic `loan-requests-in`. Upon receiving this command the service reads
the loans database, calculates availability and puts an `loan-available-event` on the `loans-available` kafka topic.
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

### `test-utils`

- Some utilities used for starting kafka and getting producers and consumers for it for use in tests

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

This will build the project and run all unit tests.

> NB: Please be aware that for the services in this project the jar is not an uber-jar (except for the `loan-client`)
> and so is not runnable by itself, and instead must be run using the quarkus run jar.

Jacoco coverage reports are available in each of the modules that have tests, in the `target/jacoco-report` folder.

#### Integration tests

You can also run the integration tests during the build using:

```shell
./mvnw clean install -Pintegration
```

Or run afterwards separately:

```shell
./mvnw verify -Pintegration
```

> NB: The integration tests use testcontainers which 1) take a while to pull from dockerhub,
> and 2) cause a problem if you have to use an internal mirror. Plus the fact they take 40s each means they are too slow
> to be in every build

#### Linting

Linting is provided by `spotless` ([link](https://github.com/diffplug/spotless)). This is not built into the ordinary
build process as a step. Rather it must be running using:

```shell
./mvnw spotless:apply
```

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
the `./requests/loan_amount_example-endpoints.http` file.

#### Viewing received messages from server sent events in the browser

There is also a web page available at `http://localhost:8080` which listens for server sent events - loan available
events.

To see this in operation, open the web page and then send a few requests using the `loan-client`, you should see the
requests come in, albeit unformatted.

#### Viewing messages as they get sent over kafka

It is possible to view the logs for the containers using `docker logs <container_name`, or looking at the service's
stdouts when running in dev mode. (Each request is logged).

However, what if we want to see messages as they go across kafka? Well do this we first need to dxec into the kafka
container:

```shell
docker exec -it setup_kafka_1 /bin/bash
```

> NB: This should be the name of the kafka container if you have used the commands above. If not find the name of the
> container with the strimzi/kafka image, using `docker ps` and replace the name above.

We can the `cd` into the `bin` folder, and run the `kafka-console-consumer` as shown below:

```shell
cd bin
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic $TOPIC_NAME --from-beginning
```

Replace $TOPIC_NAME with any of the below:

- loans-available
- loan-requests-in
- loan-offers-in

> NB: Descriptions of each of the topics and what they are used for is above in the
> [Project structure](#Project-structure) section.

#### Viewing records in the database

You may also want to see what is going in the database. When the database is running, login with:

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

## Caveats and considerations

There are a great many caveats and things that could have been done better in the construction of this project. These
will be explained here.

### Architecture and design

There a number of points to be made on the architecture and design of this project:

- Obbviously this is overly complicated for what it does, using kafka and a database and server and all. However, I
believe this is justified because:
	- It shows the way that a real service might be structured, using real modern enterprise components - you wouldn't
	have a CLI alone to do your business logic - how would your customers use that? You'd want it on a service so you
	can easily connect to it from multiple locations and so that you have the application state all in one place.
	- It shows how you might go about doing request/response with kafka using an id (borrowerId/requesterId in this
	case, though this would be on a per-request basis in a real app)
	- It hints at CQRS a little - with commands and queries being separate, albeit with only an in memory aggregate/view
	and only for loan-available events
- In terms of design, I would do probably do proper event storming to come up with the events.
- For doing a command line app, you'd probably want more validation of the file, the data, and the file types.
- The current way of deleting all records from the table is obviously a bit of a cop out because in reality you probably
wouldn't want to do this delete all at once, rather you'd delete single records when a member revokes a loan-offer,
and you'd want to have events for that too.
- Strings have been used almost throughout for currencies. The reason for this is because the can store an arbitrary
amount of rounding with accuracy (compared with floating point) and it is easier for conversion to kafka and vice
versa. `BigDecimal` is used for anything to do with calculating money, or alternatively it might be viable to use the
java currency types, but `BigDecimal` allows for a higher degree of precision.
- With more time I'd probably put more builders on classes, and also make some of the accesses private and final. In
this case this wasn't done because it makes json serialisation more faffy.

### Deployments

- The way that the project is currently structured means that it is only really deployable locally. This would not be
the case in reality. Rather you'd have something like a helm chart, jenkinsfile or something else to deploy it with,
most likely to kubernetes using gitops with something like argocd.
- Secrets wouldn't just be stored in the properties files like that, they'd be stored in a secrets manager, either cloud
based or something like hashicorp vault. Those secrets would then be pulled on app startup.

### Testing rationale

- The level of testing is not appropriate for a project of this type. The testing that has been done is somewhat
indicative of what would be done, in terms of showing a bit of everything (including using various standard '
enterprisey' libraries such as `assertj`, `equalsVerifier` and `mockito`), but not in terms scope and coverage of the
whole project.
	- Basically the only module that has been tested significantly is the `loan-offers-service`and, for unit tests,
	the `loan-amount-domain`.
- For unit testing on the numeric functions the main routes through the functions have been tested, but I haven't done
tests for things like nulls, divides by zeros or various other kind of edge cases/exceptions.
	- In a real corporate context this would be probably done by an in house library and you wouldn't be doing all of
	these calculations yourself.
- For integration tests I've tried to show some of quarkus' functionality with quarkus test and test resources, as well
as using test containers. This allows spinning up of each module
- There is an additional type of testing, which might be termed "acceptance" testing, "end-user" testing, or "system"
testing. This kind of test would use something like rest-assured, or cucumber. This type of testing has not been done,
though it might be argued that it is partially done by the fact that the `loan-client` exists.

## Additional future directions

There are a number of additional directions that this project could be taken in.

- You'd want to do way more validation on the balances and accounts if you were going to do loans properly. You might
want to have some kind double entry bookkeeping system when taking payments from one member
	- On a more simple note you'd probably want to do hibernate bean validation for certain input and output
	requirements.
	- Maybe you'd have libraries for things like the domain model, and not put it in the same repo as the actual
	services. You'd most likely have libraries to do any sort of calculation and validation with money - both for
	consistency across the company and for accuracy in implementation.
- The rest of the updates would likely be adding additional functionality which would likely invole more services.
	- You might want to do something with the fulfilment of those loans offered, which would most likely involve
	accounts and payments services. You'd have to have a mechanism for a customer making a request, and then have a
	fulfilment flow where the borrower takes up the offer, and the lender who accepts as well. You'd also have to
	figure out other things like whether you offer out the same loans to multiple people at the same time, or when
	they have been offered out do those loans go into some kind of "offer-pending" state? There are many more
	considerations for doing actual fulfilment. You might want to have a separate service itself for doing loan
	fulfilment.
	- The other thing that seems missing is the notion of a member, and all the stuff that surrounds that, like
	authentication. If there was a member service, then you'd be able to say that a specific member has requested a
	loan, and do things like only return loans to members that they have requested (not all loans available as now).
	You'd be able to have sessions and do more on the front end in this regard.
