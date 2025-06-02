package com.example.ticketapp.service;

import com.example.ticketapp.config.JmsConfig;
import com.example.ticketapp.dto.TicketEventDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketEventPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void publishTicketEvent(TicketEventDto event) {
        jmsTemplate.convertAndSend(JmsConfig.TICKET_EVENT_QUEUE, event);
    }
}