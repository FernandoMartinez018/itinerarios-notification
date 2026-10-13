package com.itinerarios.notification.repository;

import com.itinerarios.notification.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByItineraryId(Long itineraryId);

    boolean existsByEventIdAndStatus(UUID eventId, NotificationHistory.Status status);
}
