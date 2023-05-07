package com.example.rabbitlargestmarspictureservice.service;

import com.example.rabbitlargestmarspictureservice.dto.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LargestPictureListener {
    private final LargestPictureService largestPictureService;

    @RabbitListener(queues = {"largest-picture-command-queue"})
    public void listenCommand(Command command) {
        log.info("Received Command:" + command);
        largestPictureService.handleCommand(command);
    }

}
