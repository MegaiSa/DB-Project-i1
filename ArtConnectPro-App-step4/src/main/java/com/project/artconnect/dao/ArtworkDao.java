package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Artwork;

public interface ArtworkDao {

    List<Artwork> findAll();

    Optional<Artwork> findByTitle(String title);

    List<Artwork> findByArtistName(String artistName);

    void save(Artwork artwork);

    void update(Artwork artwork);

    void delete(String title);
}
