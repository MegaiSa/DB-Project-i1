package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;

public interface CommunityMemberDao {

    List<CommunityMember> findAll();

    Optional<CommunityMember> findByEmail(String email);

    Optional<CommunityMember> findByName(String name);

    /** Returns all reviews written by the given member, with each review
     *  having its {@code reviewer} and {@code artwork} fields populated. */
    List<Review> findReviewsByMember(CommunityMember member);
}
