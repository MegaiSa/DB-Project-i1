package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Exhibition;

public interface ExhibitionDao {

    List<Exhibition> findAll();

    Optional<Exhibition> findByTitle(String title);

    void save(Exhibition exhibition);

    void update(Exhibition exhibition);

    void delete(String title);
}
