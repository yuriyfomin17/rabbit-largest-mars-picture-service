package com.example.rabbitlargestmarspictureservice.service;

import com.example.rabbitlargestmarspictureservice.dto.Command;
import com.example.rabbitlargestmarspictureservice.dto.Image;
import com.example.rabbitlargestmarspictureservice.dto.ImageSrc;
import com.example.rabbitlargestmarspictureservice.dto.Photos;
import com.example.rabbitlargestmarspictureservice.model.LargestPicture;
import com.example.rabbitlargestmarspictureservice.repository.LargestPictureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LargestPictureService {
    private final LargestPictureRepository largestPictureRepository;
    @Value("${nasa.api.url}")
    private String nasaUrl;
    @Value("${nasa.api.key}")
    private String nasaKey;

    public void handleCommand(Command command) {
        String cameraFilter = command.camera();
        String id = getId(command);
        long sol = command.sol();
        if (!largestPictureRepository.existsById(id)) {
            String url = buildUriBySol(sol);
            Image largestImageBySol = WebClient.create(url)
                    .get().accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(resp -> resp.bodyToMono(Photos.class))
                    .flatMapMany(photos -> Flux.fromIterable(photos.photos()))
                    .filter(imageSrc -> imageSrc.camera().name().equals(cameraFilter))
                    .flatMap(LargestPictureService::getImage)
                    .reduce(((image1, image2) -> image1.size() > image2.size() ? image1 : image2)).block();
            Objects.requireNonNull(largestImageBySol);

            byte[] largestImageBytes = WebClient.create(largestImageBySol.url())
                    .mutate()
                    .codecs(configs -> configs.defaultCodecs().maxInMemorySize(10_000_000))
                    .build()
                    .get()
                    .accept(MediaType.IMAGE_JPEG)
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(byte[].class)).block();

            LargestPicture largestPicture = new LargestPicture();
            largestPicture.setId(id);
            String largestPictureUrl = largestImageBySol.url();
            largestPicture.setLargestPictureUrl(largestPictureUrl);
            largestPicture.setPicture(largestImageBytes);

            largestPictureRepository.save(largestPicture);
        } else {
            log.info("Command {} was already executed, skipping it", command);
        }

    }

    public String getId(Command command) {
        return command.sol() + "-" + command.camera();
    }

    private String buildUriBySol(long sol) {
        return UriComponentsBuilder.fromHttpUrl(nasaUrl)
                .queryParam("api_key", nasaKey)
                .queryParam("sol", sol)
                .build().toUriString();
    }

    private static Mono<Image> getImage(ImageSrc imageSrc) {
        return WebClient.create(imageSrc.img_src())
                .head()
                .exchangeToMono(ClientResponse::toBodilessEntity)
                .map(HttpEntity::getHeaders)
                .mapNotNull(HttpHeaders::getLocation)
                .map(URI::toString)
                .flatMap(LargestPictureService::getImageLocation);
    }

    private static Mono<Image> getImageLocation(String redirectedUrl) {
        return WebClient.create(redirectedUrl)
                .head()
                .exchangeToMono(ClientResponse::toBodilessEntity)
                .map(HttpEntity::getHeaders)
                .map(HttpHeaders::getContentLength)
                .map(size -> new Image(redirectedUrl, size));
    }

    public Optional<LargestPicture> findLargestPicture(String id) {
        return largestPictureRepository.findById(id);
    }
}