package com.example.rabbitlargestmarspictureservice.repository;

import com.example.rabbitlargestmarspictureservice.model.LargestPicture;
import org.springframework.data.repository.CrudRepository;

public interface LargestPictureRepository extends CrudRepository<LargestPicture, String> {
}
