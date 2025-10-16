package com.example.chatservice;

import com.example.chatservice.client.SkillUserClient;
import com.example.chatservice.mapper.MessageMapper;
import com.example.chatservice.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ChatServiceApplicationTests {

	@MockBean
	private MessageRepository messageRepository;

	@MockBean
	private MessageMapper messageMapper;

	@MockBean
	private SkillUserClient skillUserClient;

	@Test
	void contextLoads() {
	}

}
