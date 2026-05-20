package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

public class JdbcExhibitionDao implements ExhibitionDao {

    private static final String SELECT_ALL =
            "SELECT e.exhibition_id, e.title, e.start_date, e.end_date, e.description, " +
            "       e.curator_name, e.theme, " +
            "       g.name AS gallery_name, g.address AS gallery_address " +
            "FROM exhibition e JOIN gallery g ON g.gallery_id = e.gallery_id";

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " ORDER BY e.start_date");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapExhibition(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load exhibitions", ex);
        }
        return result;
    }

    @Override
    public Optional<Exhibition> findByTitle(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE e.title = ?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapExhibition(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find exhibition " + title, ex);
        }
        return Optional.empty();
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql =
                "INSERT INTO exhibition (title, start_date, end_date, description, curator_name, theme, gallery_id) " +
                "SELECT ?, ?, ?, ?, ?, ?, g.gallery_id FROM gallery g WHERE g.name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exhibition.getTitle());
            ps.setDate(2, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(3, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            ps.setString(7, exhibition.getGallery() != null ? exhibition.getGallery().getName() : null);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Unknown gallery for new exhibition: "
                        + (exhibition.getGallery() != null ? exhibition.getGallery().getName() : "null"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save exhibition " + exhibition.getTitle(), ex);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql =
                "UPDATE exhibition SET start_date = ?, end_date = ?, description = ?, " +
                "       curator_name = ?, theme = ? WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(3, exhibition.getDescription());
            ps.setString(4, exhibition.getCuratorName());
            ps.setString(5, exhibition.getTheme());
            ps.setString(6, exhibition.getTitle());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update exhibition " + exhibition.getTitle(), ex);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM exhibition WHERE title = ?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete exhibition " + title, ex);
        }
    }

    // -----------------------------------------------------------------

    private Exhibition mapExhibition(ResultSet rs) throws SQLException {
        Exhibition e = new Exhibition();
        e.setTitle(rs.getString("title"));
        Date start = rs.getDate("start_date");
        Date end   = rs.getDate("end_date");
        e.setStartDate(start != null ? start.toLocalDate() : null);
        e.setEndDate(end != null ? end.toLocalDate() : null);
        e.setDescription(rs.getString("description"));
        e.setCuratorName(rs.getString("curator_name"));
        e.setTheme(rs.getString("theme"));

        Gallery g = new Gallery();
        g.setName(rs.getString("gallery_name"));
        g.setAddress(rs.getString("gallery_address"));
        e.setGallery(g);
        return e;
    }
}
