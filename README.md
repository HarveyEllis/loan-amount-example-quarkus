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

## Requires

maven v3+ java 11+ (note that there is an error with spotless when using java
16, [see here](https://github.com/diffplug/spotless/issues/834))

### database

use loans db.auth("db-user", "some-password")
show collections db.LoanOffer.find()