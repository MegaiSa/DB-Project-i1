package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

public class JdbcWorkshopDao implements WorkshopDao {

    private static final String SELECT_ALL =
            "SELECT w.workshop_id, w.title, w.date, w.duration_minutes, w.max_participants, " +
            "       w.price, w.location, w.description, w.level, " +
            "       a.name AS instructor_name, a.city AS instructor_city, " +
            "       a.contact_email AS instructor_email " +
            "FROM workshop w JOIN artist a ON a.artist_id = w.instructor_artist_id";

    @Override
    public List<Workshop> findAll() {
        List<Workshop> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " ORDER BY w.date");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapWorkshop(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load workshops", ex);
        }
        return result;
    }

    @Override
    public Optional<Workshop> findByTitle(String title) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL + " WHERE w.title = ?")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapWorkshop(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find workshop " + title, ex);
        }
        return Optional.empty();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;

        String sql =
                "INSERT INTO booking (member_id, workshop_id, payment_status) " +
                "SELECT m.member_id, w.workshop_id, 'PENDING' " +
                "FROM community_member m, workshop w " +
                "WHERE m.email = ? AND w.title = ?";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, member.getEmail());
                ps.setString(2, workshop.getTitle());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            rollbackQuietly(conn);
            throw new RuntimeException(
                    "Failed to book workshop \"" + workshop.getTitle()
                    + "\" for " + member.getEmail() + ": " + ex.getMessage(), ex);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public List<Booking> findBookingsByMember(CommunityMember member) {
        List<Booking> result = new ArrayList<>();
        if (member == null || member.getEmail() == null) return result;

        String sql =
                "SELECT b.booking_date, b.payment_status, " +
                "       w.title AS w_title, w.date AS w_date, w.price AS w_price, " +
                "       w.level AS w_level, w.location AS w_location, " +
                "       a.name AS instructor_name " +
                "FROM booking b " +
                "JOIN community_member m ON m.member_id = b.member_id " +
                "JOIN workshop w        ON w.workshop_id = b.workshop_id " +
                "JOIN artist a          ON a.artist_id   = w.instructor_artist_id " +
                "WHERE m.email = ? ORDER BY b.booking_date DESC";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workshop w = new Workshop();
                    w.setTitle(rs.getString("w_title"));
                    Timestamp ts = rs.getTimestamp("w_date");
                    if (ts != null) w.setDate(ts.toLocalDateTime());
                    w.setPrice(rs.getDouble("w_price"));
                    w.setLevel(rs.getString("w_level"));
                    w.setLocation(rs.getString("w_location"));

                    Artist instr = new Artist();
                    instr.setName(rs.getString("instructor_name"));
                    w.setInstructor(instr);

                    Booking b = new Booking(w, member);
                    Timestamp bts = rs.getTimestamp("booking_date");
                    if (bts != null) b.setBookingDate(bts.toLocalDateTime());
                    b.setPaymentStatus(rs.getString("payment_status"));
                    result.add(b);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load bookings for " + member.getEmail(), ex);
        }
        return result;
    }


    private Workshop mapWorkshop(ResultSet rs) throws SQLException {
        Workshop w = new Workshop();
        w.setTitle(rs.getString("title"));
        Timestamp ts = rs.getTimestamp("date");
        if (ts != null) w.setDate(ts.toLocalDateTime());
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setMaxParticipants(rs.getInt("max_participants"));
        w.setPrice(rs.getDouble("price"));
        w.setLocation(rs.getString("location"));
        w.setDescription(rs.getString("description"));
        w.setLevel(rs.getString("level"));

        Artist instr = new Artist();
        instr.setName(rs.getString("instructor_name"));
        instr.setCity(rs.getString("instructor_city"));
        instr.setContactEmail(rs.getString("instructor_email"));
        w.setInstructor(instr);
        return w;
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
    }
}
