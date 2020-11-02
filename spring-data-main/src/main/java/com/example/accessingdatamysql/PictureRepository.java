package com.example.accessingdatamysql;

import org.springframework.data.repository.CrudRepository;

public interface PictureRepository extends CrudRepository<ProfilePic, Integer> {

    ProfilePic findByName(String url);
}
