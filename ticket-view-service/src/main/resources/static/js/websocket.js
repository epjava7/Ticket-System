let stompClient = null

function connectWebSocket() {
  const notificationServiceUrl = 'http://localhost:8083/ws'
  const socket = new SockJS(notificationServiceUrl)
  stompClient = Stomp.over(socket)
  stompClient.connect({}, function (frame) {
    stompClient.subscribe('/topic/ticket-notifications', function (e) {
      const parsedEvent = JSON.parse(e.body)

      const eventForHandler = {
        ticketId: parsedEvent.ticketId,
        eventType: parsedEvent.ticketStatus,
        user: parsedEvent.updatedBy,
        details: `Status: ${parsedEvent.ticketStatus}`,
        newStatus: parsedEvent.ticketStatus,
      }
      handleTicketEvent(eventForHandler)
    })
  })
}

function disconnectWebSocket() {
  if (stompClient !== null) {
    stompClient.disconnect()
  }
}

function handleTicketEvent(ticketEvent) {
  showDashboardAlert(
    `Ticket ${ticketEvent.ticketId} ${ticketEvent.eventType}`,
    ticketEvent.ticketId
  )

  const canLoadTickets = typeof loadTickets === 'function'
  const isOnDashboardPage = window.location.pathname.includes('/dashboard')
  if (canLoadTickets && isOnDashboardPage) {
    if (
      typeof currentStatusFilter !== 'undefined' &&
      typeof currentPage !== 'undefined'
    ) {
      loadTickets(currentStatusFilter, currentPage)
    } else {
      loadTickets('OPEN', 1)
    }
  }

  const isViewingSpecificTicketPage = typeof ticketId !== 'undefined'
  const eventIsForCurrentTicket = ticketEvent.ticketId === ticketId
  const isOnTicketDetailPage = window.location.pathname.includes('/ticket/')
  if (
    isViewingSpecificTicketPage &&
    eventIsForCurrentTicket &&
    isOnTicketDetailPage
  ) {
    if (typeof renderTicket === 'function') {
      renderTicket()
    }
    if (typeof fetchAndRenderTicketHistory === 'function') {
      fetchAndRenderTicketHistory(ticketId)
    }
  }
}

function showDashboardAlert(message, ticketId) {
  const ticketUrl = `http://localhost:8082/ticket?id=${ticketId}`

  const alertHtml = `<div class="alert alert-info alert-dismissible" role="alert">
                                <a href="${ticketUrl}" target="_blank" class="alert-link">${message}</a>
                                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                            </div>`

  const alertPlaceholder = $('#dashboardAlertPlaceholder')
  alertPlaceholder.prepend(alertHtml)
}

$(document).ready(function () {
  connectWebSocket()
})

$(window).on('beforeunload', function () {
  disconnectWebSocket()
})
