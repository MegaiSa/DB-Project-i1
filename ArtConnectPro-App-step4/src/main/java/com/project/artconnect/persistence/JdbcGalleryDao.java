package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;


public class JdbcGalleryDao implements GalleryDao {

    private static final String SELECT_ALL =
            "SELECT gallery_id, name, address, owner_name, opening_hours, " +
            "       contact_phone, rating, website FROM gallery ORDER BY name";

    private static final String SELECT_BY_NAME = SELECT_ALL.replace("ORDER BY name", "WHERE name = ?");

    private static final String SELECT_ALL_EXHIBITIONS =
            "SELECT e.exhibition_id, e.title, e.start_date, e.end_date, e.description, " +
            "       e.curator_name, e.theme, e.gallery_id, g.name AS gallery_name " +
            "FROM exhibition e JOIN gallery g ON g.gallery_id = e.gallery_id";

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        Map<Integer, Gallery> galleriesById = new HashMap<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("gallery_id");
                Gallery g = mapGallery(rs);
                galleries.add(g);
                galleriesById.put(id, g);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load galleries", ex);
        }

        if (!galleriesById.isEmpty()) {
            loadAllExhibitionsInto(galleriesById);
        }
        return galleries;
    }

    @Override
    public Optional<Gallery> findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("gallery_id");
                    Gallery g = mapGallery(rs);
                    Map<Integer, Gallery> single = new HashMap<>();
                    single.put(id, g);
                    loadAllExhibitionsInto(single);
                    return Optional.of(g);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find gallery " + name, ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Exhibition> findExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return List.of();
        Optional<Gallery> g = findByName(gallery.getName());
        return g.map(Gallery::getExhibitions).orElse(List.of());
    }

    // -----------------------------------------------------------------

    private Gallery mapGallery(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setName(rs.getString("name"));
        g.setAddress(rs.getString("address"));
        g.setOwnerName(rs.getString("owner_name"));
        g.setOpeningHours(rs.getString("opening_hours"));
        g.setContactPhone(rs.getString("contact_phone"));
        double rating = rs.getDouble("rating");
        g.setRating(rs.wasNull() ? 0.0 : rating);
        g.setWebsite(rs.getString("website"));
        return g;
    }

    private void loadAllExhibitionsInto(Map<Integer, Gallery> galleriesById) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_EXHIBITIONS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int gid = rs.getInt("gallery_id");
                Gallery g = galleriesById.get(gid);
                if (g == null) continue;
                Exhibition e = mapExhibition(rs, g);
                g.addExhibition(e);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load exhibitions for galleries", ex);
        }
    }

    private Exhibition mapExhibition(ResultSet rs, Gallery gallery) throws SQLException {
        Exhibition e = new Exhibition();
        e.setTitle(rs.getString("title"));
        Date start = rs.getDate("start_date");
        Date end   = rs.getDate("end_date");
        e.setStartDate(start != null ? start.toLocalDate() : null);
        e.setEndDate(end != null ? end.toLocalDate() : null);
        e.setDescription(rs.getString("description"));
        e.setCuratorName(rs.getString("curator_name"));
        e.setTheme(rs.getString("theme"));
        e.setGallery(gallery);
        return e;
    }
}
