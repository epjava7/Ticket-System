https://docs.google.com/presentation/d/1SqpdIsJFJulPyoQNEHclwU1ionScfdfv9Xf7IXHYayI/edit?slide=id.p#slide=id.p

Multiple microservices for a Ticket Management app for ticket lifecycle, user management and notifications 

Ticket-Service: Backend REST API for ticket management 

Ticket-View-Service: Web Frontend with session based auth, websocket 

Notification-Service: Consumer to ticket events send by Ticket-Service via JMS over ActiveMQ (point to point queue). Sends emails and provides real time web-socket updates over pub-sub using STOMP

Core Features:

- Ticket CRUD with file attachements
- Ticket status and role based user workflow
- User signup and management
- Ticket History tracking and PDF exports
- Dynamic Email notifications 
- Real time notifications via websocket

  Core Tech:
  - Java 17, Spring Boot 3.4.5
  - Spring JPA, Spring Security, Spring JMS - ActiveMQ
  - MultiPart, BCrypt, Mail Starter, CommandLineRunner
  - Actuator, Scheduling (cron jobs), Starter Cache 
  - ThymeLeaf, Bootstrap, Jquery, AJAX, JS
  - Oracle Database
  - iText PDF
  - Maven
 
  Needed to run:
  - Java 17+
  - Maven
  - Oracle Database. Set up -> ticket-service/src/main/resources/application.properties
  - ActiveMQ. Set up -> tcp://localhost:61616
  - SMTP email login in env variables -> notification-service/src/main/resources/application.properties
 
  Default Users created on startup:
  - Check implementation -> ticket-service/src/main/java/com/example/ticketapp/config/AdminCLR.java
 
  REST API Endpoints:
  - See the ticket-service controller for specific endpoints: /api/tickets and /api/employees

  
