package com.example.skillswapservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "notification-service",path = "/api/notifications",url = "${notification-service.url}")
public interface NotoficationClient {

}
