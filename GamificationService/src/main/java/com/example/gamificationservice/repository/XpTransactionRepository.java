package com.example.gamificationservice.repository;

import com.example.gamificationservice.entity.XpTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XpTransactionRepository extends MongoRepository<XpTransaction, String> {
}
