package com.ryanburnsworth.ryanGpt.repository;

import com.ryanburnsworth.ryanGpt.data.entities.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT message FROM ChatMessage message ORDER BY message.id DESC")
    List<ChatMessage> getLatestChatMessages(Pageable pageable);
}
