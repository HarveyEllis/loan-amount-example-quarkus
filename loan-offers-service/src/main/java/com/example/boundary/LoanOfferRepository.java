package com.example.boundary;

import com.example.entity.LoanOffer;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

/**
 * Use a repository pattern so that we don't burden the entity POJO class with concerns of saving to the database
 */
public class LoanOfferRepository implements ReactivePanacheMongoRepository<LoanOffer> {

}
