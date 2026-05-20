package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;

public interface GalleryDao {

    List<Gallery> findAll();

    Optional<Gallery> findByName(String name);

    /** Returns all exhibitions held by the given gallery. */
    List<Exhibition> findExhibitionsByGallery(Gallery gallery);
}
