package com.hotel.army.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByIsEnded(boolean isEnded);

    List<Session> findByRoomId(Long roomId);

    List<Session> findByRoom(Room room);

    List<Session> findByGuest(Guest guest);
}