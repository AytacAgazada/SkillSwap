package com.example.chatservice.repository;

import com.example.chatservice.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySwapIdOrderByTimestampAsc(String swapId);
}
