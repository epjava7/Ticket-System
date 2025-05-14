package com.example.ticketapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

import com.example.ticketapp.domain.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    @Query("SELECT DISTINCT t.category FROM Ticket t WHERE t.category IS NOT NULL")
    List<String> findDistinctCategories();

    Optional<Ticket> findByTitle(String title);

}
