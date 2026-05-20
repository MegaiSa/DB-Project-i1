package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.util.ConnectionManager;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    private static final String SELECT_ALL =
            "SELECT member_id, name, email, birth_year, phone, city, membership_type " +
            "FROM community_member ORDER BY name";

    private static final String SELECT_BY_EMAIL = SELECT_ALL.replace("ORDER BY name",
            "WHERE email = ?");

    private static final String SELECT_BY_NAME = SELECT_ALL.replace("ORDER BY name",
            "WHERE name = ?");

    private static final String SELECT_REVIEWS_BY_MEMBER =
            "SELECT r.rating, r.comment, r.review_date, " +
            "       aw.title AS artwork_title, aw.type AS artwork_type, aw.price AS artwork_price, " +
            "       aw.status AS artwork_status, " +
            "       a.name AS artist_name " +
            "FROM review r " +
            "JOIN artwork aw ON aw.artwork_id = r.artwork_id " +
            "JOIN artist  a  ON a.artist_id   = aw.artist_id " +
            "JOIN community_member m ON m.member_id = r.member_id " +
            "WHERE m.email = ? ORDER BY r.review_date DESC";

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapMember(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load community members", ex);
        }
        return result;
    }

    @Override
    public Optional<CommunityMember> findByEmail(String email) {
        return findOneBy(SELECT_BY_EMAIL, email);
    }

    @Override
    public Optional<CommunityMember> findByName(String name) {
        return findOneBy(SELECT_BY_NAME, name);
    }

    @Override
    public List<Review> findReviewsByMember(CommunityMember member) {
        List<Review> reviews = new ArrayList<>();
        if (member == null || member.getEmail() == null) return reviews;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_REVIEWS_BY_MEMBER)) {
            ps.setString(1, member.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewer(member);
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    Date d = rs.getDate("review_date");
                    r.setReviewDate(d != null ? d.toLocalDate() : null);

                    Artist artist = new Artist();
                    artist.setName(rs.getString("artist_name"));

                    Artwork aw = new Artwork();
                    aw.setTitle(rs.getString("artwork_title"));
                    aw.setType(rs.getString("artwork_type"));
                    aw.setPrice(rs.getDouble("artwork_price"));
                    String status = rs.getString("artwork_status");
                    if (status != null) aw.setStatus(Artwork.Status.valueOf(status));
                    aw.setArtist(artist);

                    r.setArtwork(aw);
                    reviews.add(r);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load reviews for " + member.getEmail(), ex);
        }
        return reviews;
    }


    private Optional<CommunityMember> findOneBy(String sql, String key) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapMember(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find community member by " + key, ex);
        }
        return Optional.empty();
    }

    private CommunityMember mapMember(ResultSet rs) throws SQLException {
        CommunityMember m = new CommunityMember();
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        int by = rs.getInt("birth_year");
        m.setBirthYear(rs.wasNull() ? null : by);
        m.setPhone(rs.getString("phone"));
        m.setCity(rs.getString("city"));
        m.setMembershipType(rs.getString("membership_type"));
        return m;
    }
}
