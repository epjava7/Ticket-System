package com.example.ticketapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ticketapp.domain.TicketHistory;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    
}
