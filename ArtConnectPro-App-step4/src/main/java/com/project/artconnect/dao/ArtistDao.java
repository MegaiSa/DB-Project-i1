package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;

public interface ArtistDao {

    List<Artist> findAll();

    Optional<Artist> findByName(String name);

    List<Artist> findByCity(String city);

    void save(Artist artist);

    void update(Artist artist);

    void delete(String artistName);

    List<Discipline> findAllDisciplines();
}
