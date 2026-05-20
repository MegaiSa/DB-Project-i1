package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;


public class JdbcArtworkDao implements ArtworkDao {

    private static final String SELECT_ALL =
            "SELECT aw.artwork_id, aw.title, aw.creation_year, aw.type, aw.medium, " +
            "       aw.dimensions, aw.description, aw.price, aw.status, " +
            "       a.name AS artist_name, a.bio AS artist_bio, a.birth_year AS artist_birth_year, " +
            "       a.contact_email AS artist_email, a.city AS artist_city " +
            "FROM artwork aw JOIN artist a ON a.artist_id = aw.artist_id";

    @Override
    public List<Artwork> findAll() {
        List<Artwork> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " ORDER BY aw.title");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapArtwork(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load artworks", ex);
        }
        return result;
    }

    @Override
    public Optional<Artwork> findByTitle(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE aw.title = ?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapArtwork(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find artwork " + title, ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE a.name = ? ORDER BY aw.title")) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapArtwork(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find artworks of " + artistName, ex);
        }
        return result;
    }

    @Override
    public void save(Artwork artwork) {
        String sql =
                "INSERT INTO artwork (title, creation_year, type, medium, dimensions, " +
                "                     description, price, status, artist_id) " +
                "SELECT ?, ?, ?, ?, ?, ?, ?, ?, a.artist_id FROM artist a WHERE a.name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artwork.getTitle());
            setNullableInt(ps, 2, artwork.getCreationYear());
            ps.setString(3, artwork.getType());
            ps.setString(4, artwork.getMedium());
            ps.setString(5, artwork.getDimensions());
            ps.setString(6, artwork.getDescription());
            ps.setDouble(7, artwork.getPrice());
            ps.setString(8, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
            ps.setString(9, artwork.getArtist() != null ? artwork.getArtist().getName() : null);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Unknown artist for new artwork: "
                        + (artwork.getArtist() != null ? artwork.getArtist().getName() : "null"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save artwork " + artwork.getTitle(), ex);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String sql =
                "UPDATE artwork SET creation_year = ?, type = ?, medium = ?, dimensions = ?, " +
                "       description = ?, price = ?, status = ? WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableInt(ps, 1, artwork.getCreationYear());
            ps.setString(2, artwork.getType());
            ps.setString(3, artwork.getMedium());
            ps.setString(4, artwork.getDimensions());
            ps.setString(5, artwork.getDescription());
            ps.setDouble(6, artwork.getPrice());
            ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : "FOR_SALE");
            ps.setString(8, artwork.getTitle());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update artwork " + artwork.getTitle(), ex);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM artwork WHERE title = ?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete artwork " + title, ex);
        }
    }

    // -----------------------------------------------------------------

    private Artwork mapArtwork(ResultSet rs) throws SQLException {
        Artwork a = new Artwork();
        a.setTitle(rs.getString("title"));
        int cy = rs.getInt("creation_year");
        a.setCreationYear(rs.wasNull() ? null : cy);
        a.setType(rs.getString("type"));
        a.setMedium(rs.getString("medium"));
        a.setDimensions(rs.getString("dimensions"));
        a.setDescription(rs.getString("description"));
        a.setPrice(rs.getDouble("price"));
        String status = rs.getString("status");
        if (status != null) {
            a.setStatus(Artwork.Status.valueOf(status));
        }

        Artist artist = new Artist();
        artist.setName(rs.getString("artist_name"));
        artist.setBio(rs.getString("artist_bio"));
        int by = rs.getInt("artist_birth_year");
        artist.setBirthYear(rs.wasNull() ? null : by);
        artist.setContactEmail(rs.getString("artist_email"));
        artist.setCity(rs.getString("artist_city"));
        a.setArtist(artist);
        return a;
    }

    private static void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER);
        else             ps.setInt(idx, val);
    }
}
