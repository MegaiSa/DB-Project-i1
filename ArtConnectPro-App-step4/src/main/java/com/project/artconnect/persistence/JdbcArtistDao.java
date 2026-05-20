package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

public class JdbcArtistDao implements ArtistDao {

    private static final String SELECT_ALL =
            "SELECT artist_id, name, bio, birth_year, contact_email, city, " +
            "       phone, website, social_media, is_active " +
            "FROM artist ORDER BY name";

    private static final String SELECT_BY_NAME = SELECT_ALL.replace("ORDER BY name",
            "WHERE name = ?");

    private static final String SELECT_BY_CITY = SELECT_ALL.replace("ORDER BY name",
            "WHERE city = ? ORDER BY name");

    private static final String INSERT =
            "INSERT INTO artist (name, bio, birth_year, contact_email, city, phone, website, social_media, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE artist SET bio = ?, birth_year = ?, contact_email = ?, city = ?, " +
            "phone = ?, website = ?, social_media = ?, is_active = ? WHERE name = ?";

    private static final String DELETE =
            "DELETE FROM artist WHERE name = ?";

    private static final String SELECT_DISCIPLINES_BY_ARTIST_ID =
            "SELECT ad.artist_id, d.name " +
            "FROM artist_discipline ad JOIN discipline d ON d.discipline_id = ad.discipline_id " +
            "WHERE ad.artist_id IN (%s)";

    private static final String SELECT_ALL_DISCIPLINES =
            "SELECT name FROM discipline ORDER BY name";

    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        Map<Integer, Artist> artistsById = new HashMap<>();

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("artist_id");
                Artist a = mapArtist(rs);
                artists.add(a);
                artistsById.put(id, a);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load artists", ex);
        }

        if (!artistsById.isEmpty()) {
            loadDisciplinesInto(artistsById);
        }
        return artists;
    }

    @Override
    public Optional<Artist> findByName(String name) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("artist_id");
                    Artist a = mapArtist(rs);
                    Map<Integer, Artist> single = new HashMap<>();
                    single.put(id, a);
                    loadDisciplinesInto(single);
                    return Optional.of(a);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find artist " + name, ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> result = new ArrayList<>();
        Map<Integer, Artist> byId = new HashMap<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CITY)) {
            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("artist_id");
                    Artist a = mapArtist(rs);
                    result.add(a);
                    byId.put(id, a);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find artists by city " + city, ex);
        }
        if (!byId.isEmpty()) {
            loadDisciplinesInto(byId);
        }
        return result;
    }

    @Override
    public void save(Artist artist) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            int generatedId;
            try (PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                bindArtistInsert(ps, artist);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No generated id returned for new artist.");
                    }
                    generatedId = keys.getInt(1);
                }
            }

            saveDisciplineLinks(conn, generatedId, artist);
            conn.commit();

        } catch (SQLException ex) {
            rollbackQuietly(conn);
            throw new RuntimeException("Failed to save artist " + artist.getName(), ex);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void update(Artist artist) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
                ps.setString(1, artist.getBio());
                setNullableInt(ps, 2, artist.getBirthYear());
                ps.setString(3, artist.getContactEmail());
                ps.setString(4, artist.getCity());
                ps.setString(5, artist.getPhone());
                ps.setString(6, artist.getWebsite());
                ps.setString(7, artist.getSocialMedia());
                ps.setBoolean(8, artist.isActive());
                ps.setString(9, artist.getName());
                ps.executeUpdate();
            }

            // Rewrite the discipline links if disciplines are present on the model.
            Integer id = lookupArtistId(conn, artist.getName());
            if (id != null) {
                try (PreparedStatement clear = conn.prepareStatement(
                        "DELETE FROM artist_discipline WHERE artist_id = ?")) {
                    clear.setInt(1, id);
                    clear.executeUpdate();
                }
                saveDisciplineLinks(conn, id, artist);
            }

            conn.commit();
        } catch (SQLException ex) {
            rollbackQuietly(conn);
            throw new RuntimeException("Failed to update artist " + artist.getName(), ex);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void delete(String artistName) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setString(1, artistName);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete artist " + artistName, ex);
        }
    }

    @Override
    public List<Discipline> findAllDisciplines() {
        List<Discipline> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_DISCIPLINES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Discipline(rs.getString("name")));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load disciplines", ex);
        }
        return result;
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private Artist mapArtist(ResultSet rs) throws SQLException {
        Artist a = new Artist();
        a.setName(rs.getString("name"));
        a.setBio(rs.getString("bio"));
        int by = rs.getInt("birth_year");
        a.setBirthYear(rs.wasNull() ? null : by);
        a.setContactEmail(rs.getString("contact_email"));
        a.setCity(rs.getString("city"));
        a.setPhone(rs.getString("phone"));
        a.setWebsite(rs.getString("website"));
        a.setSocialMedia(rs.getString("social_media"));
        a.setActive(rs.getBoolean("is_active"));
        return a;
    }

    private void loadDisciplinesInto(Map<Integer, Artist> artistsById) {
        String placeholders = String.join(",", java.util.Collections.nCopies(artistsById.size(), "?"));
        String sql = String.format(SELECT_DISCIPLINES_BY_ARTIST_ID, placeholders);

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : artistsById.keySet()) {
                ps.setInt(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int aid = rs.getInt("artist_id");
                    Artist a = artistsById.get(aid);
                    if (a != null) {
                        a.getDisciplines().add(new Discipline(rs.getString("name")));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load disciplines for artists", ex);
        }
    }

    private void bindArtistInsert(PreparedStatement ps, Artist a) throws SQLException {
        ps.setString(1, a.getName());
        ps.setString(2, a.getBio());
        setNullableInt(ps, 3, a.getBirthYear());
        ps.setString(4, a.getContactEmail());
        ps.setString(5, a.getCity());
        ps.setString(6, a.getPhone());
        ps.setString(7, a.getWebsite());
        ps.setString(8, a.getSocialMedia());
        ps.setBoolean(9, a.isActive());
    }

    private Integer lookupArtistId(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT artist_id FROM artist WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private void saveDisciplineLinks(Connection conn, int artistId, Artist artist) throws SQLException {
        if (artist.getDisciplines() == null || artist.getDisciplines().isEmpty()) {
            return;
        }
        String sql =
                "INSERT IGNORE INTO artist_discipline (artist_id, discipline_id) " +
                "SELECT ?, discipline_id FROM discipline WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Discipline d : artist.getDisciplines()) {
                ps.setInt(1, artistId);
                ps.setString(2, d.getName());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER);
        else             ps.setInt(idx, val);
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
