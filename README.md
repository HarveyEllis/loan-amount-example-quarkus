# Plan

- create multimodule so that it can run all at once
- use jbang for creating a ./zopa-loans script and compiling all at once
- cucumber tests?
- jacoco coverage
- hibernate bean validation for their requirements

## Building

## Running/developing

Two docker compose files

Note that when running in quarkus:dev mode some of the kafka functionality can be a lot slower - on the order of 500ms
rather than around 30-50ms.

## architecture

- members service
-

## events

member created account added payment made loan offered - gets stored in loan offers service loan requested loan accepted
by offerer loan accepted by requestor loan fulfilled - how would you go about doing this?

loans/offer loans/request

Uses convention of "in" for commands and "out" for things that would notify the user

# Caveats

- would do proper event storming
- would make helm more variable
- would want to decide on tests - cucumber? or just system tests?

might not go this granular - have a single loans service?

- the rationale for not doing this is that you would have the requests looking in the offers table which is something I
don't think should really happen.

things like payments would do fulfilment etc

accounts and stuff would be protected

secrets wouldn't just be stored like that

infrastructure setup would be different - you'd deploy it to something like openshift using argocd

might put in separate repos?

you'd probably want to do way more validation on the balances and accounts if you were going to do loans properly - want
to have some kind double entry bookkeeping system really?

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
- using various standard 'enterprisey' libraries such as `assertj` and `equalsVerifier`.  

## Requires

maven v3+ java 11+ (note that there is an error with spotless when using java
16, [see here](https://github.com/diffplug/spotless/issues/834))
docker-compose docker

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