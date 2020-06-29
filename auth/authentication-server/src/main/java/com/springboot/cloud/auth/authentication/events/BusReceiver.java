package com.springboot.cloud.auth.authentication.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BusReceiver {

    public void handleMessage(String message) {
        log.info("Received Message:<{}>", message);
    }
}
