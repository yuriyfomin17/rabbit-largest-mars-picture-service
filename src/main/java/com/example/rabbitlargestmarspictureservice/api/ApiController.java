package com.example.rabbitlargestmarspictureservice.api;

import com.example.rabbitlargestmarspictureservice.dto.Command;
import com.example.rabbitlargestmarspictureservice.model.LargestPicture;
import com.example.rabbitlargestmarspictureservice.service.LargestPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ApiController {
    private final RabbitTemplate rabbitTemplate;
    private final LargestPictureService largestPictureService;
    @Value("${rabbit.exchange.name}")
    private String exchange;
    @Value("${rabbit.key.name}")
    private String routingKey;

    @PostMapping("/mars/pictures/largest")
    public ResponseEntity<?> postCommand(@RequestBody Command command) {
        rabbitTemplate.convertAndSend(exchange, routingKey, command);
        String id = largestPictureService.getId(command);
        String responseUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .pathSegment(id)
                .build()
                .toUriString();
        return ResponseEntity.created(URI.create(responseUri)).build();
    }

    @GetMapping(value = "/mars/pictures/largest/{commandId}", produces = {MediaType.IMAGE_JPEG_VALUE})
    public ResponseEntity<?> getLargestPicture(@PathVariable String commandId) {
        Optional<LargestPicture> optionalLargestPicture = largestPictureService.findLargestPicture(commandId);
        if (optionalLargestPicture.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>("No pictures found", headers, HttpStatus.BAD_REQUEST);
        }
        return optionalLargestPicture.map(this::getRedirectResponse).orElseThrow();
    }

    private ResponseEntity<?> getRedirectResponse(LargestPicture largestPicture) {
        return new ResponseEntity<>(largestPicture.getPicture(), HttpStatus.OK);
    }
}