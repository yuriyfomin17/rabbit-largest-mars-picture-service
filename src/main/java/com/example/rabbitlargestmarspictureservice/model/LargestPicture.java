package com.example.rabbitlargestmarspictureservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@RequiredArgsConstructor
@RedisHash("LargestPicture")
@ToString
public class LargestPicture {

    @Id
    private String id;

    private String largestPictureUrl;

    private byte[] picture;
}