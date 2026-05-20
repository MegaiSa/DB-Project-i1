package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;

public interface WorkshopDao {

    List<Workshop> findAll();

    Optional<Workshop> findByTitle(String title);

    /**
     * Books the given member for the given workshop. The transaction
     * is atomic: any constraint violation (capacity check, duplicate
     * booking) rolls back the insert.
     */
    void bookWorkshop(Workshop workshop, CommunityMember member);

    /** Returns all bookings made by a given member. */
    List<Booking> findBookingsByMember(CommunityMember member);
}
