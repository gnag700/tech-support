# Tech-Support - Implementation Examples

**Project:** Tech-Support Helpdesk System  
**Author:** Winston (Architect) + Paige (Technical Writer)  
**Date:** 2025-11-10  
**Version:** 1.0  
**Status:** Reference Implementation Guide

---

## Overview

This document provides **production-ready code examples** for key architectural patterns used in Tech-Support:

1. **Domain Event Publishing** - Spring Modulith event-driven communication
2. **CQRS Read Model** - Analytics module denormalized queries
3. **React Query Integration** - Frontend data fetching and caching
4. **Spring Modulith Testing** - Module boundary verification

**Prerequisites:**

- Read: [Architecture Document](./architecture-2025-11-06.md)
- Familiar with: Spring Boot 3.5, React 18, TypeScript

---

## Example 1: Domain Event Publishing

### Publishing Scenario

При создании тикета публикуется событие `TicketCreatedEvent` для модулей:

- **Notifications** - отправка уведомлений в Telegram/Email
- **Analytics** - обновление метрик
- **Audit** - логирование действий

### 1.1 Event Definition (shared module)

```java
package by.crb.mh.techsupport.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new ticket is created.
 * Implements TicketEvent marker interface for type-safe event handling.
 * 
 * Consumed by:
 * - notifications module (send alerts)
 * - analytics module (update metrics)
 * - audit module (log action)
 * 
 * Note: Factory methods are NOT included here to avoid coupling the shared
 * module to domain entities. Use mapper classes in the ticketing module instead.
 */
public record TicketCreatedEvent(
    UUID ticketId,
    UUID createdByUserId,
    String title,
    String category,      // HARDWARE, SOFTWARE, NETWORK, ACCESS, OTHER
    String priority,      // P1, P2, P3
    Instant createdAt
) implements TicketEvent {
}
```

**Key Points:**

- ✅ **Java Record** - immutable, equals/hashCode generated automatically
- ✅ **Marker Interface** - `TicketEvent` для type-safe filtering
- ✅ **Minimal Data** - только необходимые поля (не весь Ticket entity)
- ✅ **No Factory Methods** - избегает coupling shared module к domain entities

### 1.2 Event Mapper (ticketing module)

```java
package by.crb.mh.techsupport.ticketing.domain;

import by.crb.mh.techsupport.shared.events.TicketCreatedEvent;
import org.springframework.stereotype.Component;

/**
 * Maps Ticket domain entities to TicketCreatedEvent.
 * Keeps shared.events module decoupled from ticketing domain.
 */
@Component
public class TicketEventMapper {
    
    /**
     * Creates TicketCreatedEvent from Ticket entity.
     * Extracts UUID values from value objects (TicketId, UserId).
     */
    public TicketCreatedEvent toCreatedEvent(Ticket ticket) {
        return new TicketCreatedEvent(
            ticket.getId().value(),           // Extract UUID from TicketId value object
            ticket.getCreatedBy().getId().value(), // Extract UUID from UserId value object
            ticket.getTitle(),
            ticket.getCategory().name(),
            ticket.getPriority().name(),
            ticket.getCreatedAt()
        );
    }
}
```

**Key Points:**

- ✅ **Decoupling** - shared module не зависит от ticketing domain
- ✅ **Value Object Extraction** - `.value()` извлекает UUID из TicketId/UserId
- ✅ **Single Responsibility** - отдельный класс для mapping логики

### 1.3 Service Publishing Event (ticketing module)

```java
package by.crb.mh.techsupport.ticketing.domain;

import by.crb.mh.techsupport.shared.events.TicketCreatedEvent;
import by.crb.mh.techsupport.shared.domain.TicketId;
import by.crb.mh.techsupport.shared.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TicketEventMapper eventMapper;
    
    /**
     * Creates a new ticket and publishes TicketCreatedEvent.
     * Event is persisted to event_publication table within same transaction.
     * 
     * @param command validated create ticket command
     * @return persisted ticket entity
     * @throws BusinessRuleException if business rules violated
     */
    @Transactional
    public Ticket createTicket(CreateTicketCommand command) {
        log.info("Creating ticket: {}", command.title());
        
        // 1. Validate business rules
        validateCreateTicketRules(command);
        
        // 2. Build domain entity
        Ticket ticket = Ticket.builder()
            .id(TicketId.generate())
            .title(command.title())
            .description(command.description())
            .category(command.category())
            .priority(command.priority())
            .status(TicketStatus.OPEN)
            .createdBy(command.createdBy())
            .createdAt(Instant.now())
            .build();
        
        // 3. Persist to database
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket created: {} - {}", savedTicket.getId(), savedTicket.getTitle());
        
        // 4. Publish domain event using mapper
        // Spring Modulith persists event to event_publication table
        // Event is published AFTER transaction commit (async listeners)
        TicketCreatedEvent event = eventMapper.toCreatedEvent(savedTicket);
        eventPublisher.publishEvent(event);
        
        log.debug("Published TicketCreatedEvent for ticket {}", savedTicket.getId());
        
        return savedTicket;
    }
    
    private void validateCreateTicketRules(CreateTicketCommand command) {
        if (command.title().length() < 5) {
            throw new BusinessRuleException("Title must be at least 5 characters");
        }
        if (command.description().length() < 10) {
            throw new BusinessRuleException("Description must be at least 10 characters");
        }
        // Additional validations...
    }
}
```

**Key Points:**

- ✅ **@Transactional** - event persisted with ticket in same transaction
- ✅ **Event Publication** - `eventPublisher.publishEvent()` stores to DB
- ✅ **Mapper Injection** - uses `TicketEventMapper` to create event
- ✅ **No Direct Dependencies** - ticketing module doesn't know about notifications
- ✅ **Logging** - structured logs for observability

### 1.4 Event Consumer (notifications module)

```java
package by.crb.mh.techsupport.notifications.listener;

import by.crb.mh.techsupport.shared.events.TicketCreatedEvent;
import by.crb.mh.techsupport.notifications.domain.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketEventNotificationListener {
    
    private final NotificationService notificationService;
    
    /**
     * Handles TicketCreatedEvent by sending notifications to support team.
     * 
     * Async: runs in separate thread, does not block ticket creation
     * TransactionalEventListener: waits until transaction commits
     * Transactional: ensures idempotency check and notification queue operations are atomic
     * Idempotent: checks notification_queue to prevent duplicates
     * 
     * @param event the ticket created event
     */
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketCreated(TicketCreatedEvent event) {
        log.info("Handling TicketCreatedEvent for ticket {}", event.ticketId());
        
        try {
            // Check if notification already sent (idempotency protection)
            if (notificationService.alreadySent(event.ticketId(), "TICKET_CREATED")) {
                log.warn("Duplicate TicketCreatedEvent detected for ticket {}, skipping", 
                         event.ticketId());
                return;
            }
            
            // Send notifications (Telegram primary, Email fallback)
            notificationService.sendTicketCreatedNotification(
                event.ticketId(),
                event.title(),
                event.category(),
                event.priority(),
                event.createdByUserId()
            );
            
            log.info("Successfully sent notifications for ticket {}", event.ticketId());
            
        } catch (Exception e) {
            log.error("Failed to send notification for ticket {}", event.ticketId(), e);
            // Event remains in event_publication table
            // Will be retried on application restart
            throw e;  // Re-throw to mark event as failed
        }
    }
}
```

**Key Points:**

- ✅ **@Async** - не блокирует создание тикета
- ✅ **@Transactional** - атомарность idempotency check и queue operations
- ✅ **@TransactionalEventListener** - запускается после COMMIT
- ✅ **Idempotency** - проверка дубликатов через `alreadySent()`
- ✅ **Retry Logic** - при ошибке event остаётся в БД для повтора

### 1.5 Event Publication Registry Monitoring

**Check for stuck events:**

```sql
-- Events pending longer than 1 hour (possible listener failure)
SELECT 
    id,
    event_type,
    listener_id,
    publication_date,
    AGE(NOW(), publication_date) as age
FROM event_publication
WHERE completion_date IS NULL
  AND publication_date < NOW() - INTERVAL '1 hour'
ORDER BY publication_date
LIMIT 50;
```

**Cleanup completed events:**

```java
@Scheduled(cron = "0 0 3 * * *")  // 03:00 daily
@Transactional
public void archiveCompletedEvents() {
    OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
    
    int deleted = jdbcTemplate.update(
        "DELETE FROM event_publication WHERE completion_date < ?",
        cutoff
    );
    
    log.info("Deleted {} completed events older than 30 days", deleted);
}
```

---

## Example 2: CQRS Read Model (Analytics)

### Read Model Scenario

Analytics module строит денормализованную read model из событий тикетов для быстрых dashboard-запросов.

### 2.1 Read Model Entity

```java
package by.crb.mh.techsupport.analytics.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * Denormalized read model for analytics dashboard queries.
 * 
 * Updated via event listeners, never by REST API directly.
 * Aggregates ticket metrics by date, category, and priority.
 * 
 * Unique key: (date, category, priority)
 */
@Entity
@Table(
    name = "ticket_metrics",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_metrics_date_category_priority",
        columnNames = {"date", "category", "priority"}
    ),
    indexes = {
        @Index(name = "idx_metrics_date", columnList = "date"),
        @Index(name = "idx_metrics_category", columnList = "category")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false, length = 50)
    private String category;  // HARDWARE, SOFTWARE, NETWORK, ACCESS, OTHER
    
    @Column(nullable = false, length = 10)
    private String priority;  // P1, P2, P3
    
    @Column(nullable = false)
    @Builder.Default
    private Integer ticketsCreated = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer ticketsResolved = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer ticketsClosed = 0;
    
    /**
     * Total resolution time in minutes for all resolved tickets.
     * Used to calculate average resolution time.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long totalResolutionTimeMinutes = 0L;
    
    /**
     * Count of resolved tickets (for averaging).
     * Separate from ticketsResolved to handle resolution time calculation.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer resolvedTicketCount = 0;
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    /**
     * Calculate average resolution time in hours.
     */
    public double getAvgResolutionTimeHours() {
        if (resolvedTicketCount == 0) return 0.0;
        return (totalResolutionTimeMinutes / (double) resolvedTicketCount) / 60.0;
    }
}
```

### 2.2 Event Listener Building Read Model

```java
package by.crb.mh.techsupport.analytics.listener;

import by.crb.mh.techsupport.shared.events.*;
import by.crb.mh.techsupport.analytics.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketMetricsListener {
    
    private final TicketMetricsRepository metricsRepository;
    
    @Async
    @EventListener
    @Transactional
    public void onTicketCreated(TicketCreatedEvent event) {
        LocalDate date = LocalDate.ofInstant(event.createdAt(), ZoneOffset.UTC);
        
        updateMetricsWithRetry(date, event.category(), event.priority(), metrics -> {
            metrics.setTicketsCreated(metrics.getTicketsCreated() + 1);
        });
        
        log.debug("Updated metrics for {}: +1 created", date);
    }
    
    @Async
    @EventListener
    @Transactional
    public void onTicketResolved(TicketResolvedEvent event) {
        LocalDate date = LocalDate.ofInstant(event.resolvedAt(), ZoneOffset.UTC);
        
        // Calculate resolution time
        long resolutionMinutes = Duration
            .between(event.createdAt(), event.resolvedAt())
            .toMinutes();
        
        updateMetricsWithRetry(date, event.category(), event.priority(), metrics -> {
            metrics.setTicketsResolved(metrics.getTicketsResolved() + 1);
            metrics.setTotalResolutionTimeMinutes(
                metrics.getTotalResolutionTimeMinutes() + resolutionMinutes
            );
            metrics.setResolvedTicketCount(metrics.getResolvedTicketCount() + 1);
        });
        
        log.debug("Updated metrics for {}: +1 resolved ({}min)", date, resolutionMinutes);
    }
    
    @Async
    @EventListener
    @Transactional
    public void onTicketClosed(TicketClosedEvent event) {
        LocalDate date = LocalDate.ofInstant(event.closedAt(), ZoneOffset.UTC);
        
        updateMetricsWithRetry(date, event.category(), event.priority(), metrics -> {
            metrics.setTicketsClosed(metrics.getTicketsClosed() + 1);
        });
        
        log.debug("Updated metrics for {}: +1 closed", date);
    }
    
    /**
     * Updates metrics with retry logic to handle concurrent listener race conditions.
     * If unique constraint is violated, retries by reloading the existing record.
     * 
     * @param date the date bucket
     * @param category ticket category
     * @param priority ticket priority
     * @param updater lambda to apply updates to metrics
     */
    private void updateMetricsWithRetry(
        LocalDate date, 
        String category, 
        String priority,
        java.util.function.Consumer<TicketMetrics> updater
    ) {
        int maxRetries = 3;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                TicketMetrics metrics = metricsRepository
                    .findByDateAndCategoryAndPriority(date, category, priority)
                    .orElseGet(() -> TicketMetrics.builder()
                        .date(date)
                        .category(category)
                        .priority(priority)
                        .build());
                
                updater.accept(metrics);
                metricsRepository.save(metrics);
                return;  // Success
                
            } catch (DataIntegrityViolationException e) {
                // Concurrent listener created the row - retry with existing record
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("Failed to update metrics after {} retries for {}/{}/{}", 
                             maxRetries, date, category, priority, e);
                    throw e;
                }
                log.debug("Retry {} due to concurrent insert for {}/{}/{}", 
                         attempt, date, category, priority);
                // Small delay before retry
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during metrics update retry", ie);
                }
            }
        }
    }
}
```

**Key Points:**

- ✅ **Race Condition Handling** - retry logic catches `DataIntegrityViolationException`
- ✅ **Atomic Updates** - reloads existing record on constraint violation
- ✅ **TicketClosed Handler** - keeps `ticketsClosed` column accurate
- ✅ **Exponential Backoff** - small delay between retries (50ms * attempt)

### 2.3 Query Service for Dashboard

```java
package by.crb.mh.techsupport.analytics.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportGenerator {
    
    private final TicketMetricsRepository metricsRepository;
    
    /**
     * Generates dashboard metrics for the last 7 days.
     * Results are cached for 5 minutes to reduce DB load.
     */
    @Cacheable(value = "dashboardMetrics", key = "'weekly'")
    @Transactional(readOnly = true)
    public DashboardMetricsDTO generateWeeklyReport() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);  // 7 days inclusive
        
        List<TicketMetrics> weekMetrics = metricsRepository
            .findByDateBetween(startDate, endDate);
        
        // Aggregate totals
        int totalCreated = weekMetrics.stream()
            .mapToInt(TicketMetrics::getTicketsCreated)
            .sum();
        
        int totalResolved = weekMetrics.stream()
            .mapToInt(TicketMetrics::getTicketsResolved)
            .sum();
        
        int totalClosed = weekMetrics.stream()
            .mapToInt(TicketMetrics::getTicketsClosed)
            .sum();
        
        // Calculate average resolution time correctly:
        // Sum all resolution minutes and all resolved tickets, then divide once
        long totalMinutes = weekMetrics.stream()
            .mapToLong(TicketMetrics::getTotalResolutionTimeMinutes)
            .sum();
        
        int totalResolvedCount = weekMetrics.stream()
            .mapToInt(TicketMetrics::getResolvedTicketCount)
            .sum();
        
        double avgResolutionTimeHours = totalResolvedCount > 0
            ? (totalMinutes / (double) totalResolvedCount) / 60.0
            : 0.0;
        
        // Group by category and priority
        Map<String, Integer> byCategory = new HashMap<>();
        Map<String, Integer> byPriority = new HashMap<>();
        
        weekMetrics.forEach(m -> {
            byCategory.merge(m.getCategory(), m.getTicketsCreated(), Integer::sum);
            byPriority.merge(m.getPriority(), m.getTicketsCreated(), Integer::sum);
        });
        
        return DashboardMetricsDTO.builder()
            .totalTickets(totalCreated)
            .openTickets(totalCreated - totalClosed)  // Use ticketsClosed, not resolved
            .resolvedTickets(totalResolved)
            .avgResolutionTimeHours(avgResolutionTimeHours)
            .ticketsByCategory(byCategory)
            .ticketsByPriority(byPriority)
            .period("last_7_days")
            .build();
    }
}
```

**Key Points:**

- ✅ **Denormalized** - быстрые запросы без JOIN
- ✅ **Event-Sourced** - строится из событий, не прямых записей
- ✅ **Cached** - `@Cacheable` снижает нагрузку на БД
- ✅ **Correct Averaging** - sum totals first, then divide (не averaging averages)
- ✅ **Correct Open Count** - uses `totalClosed` instead of `totalResolved`
- ✅ **7 Days Inclusive** - `minusDays(6)` gives exactly 7 days

---

## Example 3: React Query Integration

### 3.1 API Client Setup

```typescript
// lib/api/client.ts
import axios from 'axios';

export interface ApiResponse<T> {
  data: T;
  metadata: {
    timestamp: string;
    request_id: string;
  };
  error: ErrorDetails | null;
}

export interface ErrorDetails {
  code: string;
  message: string;
  details?: Record<string, any>;
  field_errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Try refresh token using apiClient to preserve base URL and interceptors
      try {
        const { data } = await apiClient.post('/auth/refresh', {}, {
          withCredentials: true,
        });
        localStorage.setItem('access_token', data.data.access_token);
        
        // Retry original request with new token
        // Ensure headers object exists
        error.config.headers = error.config.headers ?? {};
        error.config.headers.Authorization = `Bearer ${data.data.access_token}`;
        return apiClient.request(error.config);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('access_token');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);
```

### 3.2 Tickets API Module

```typescript
// lib/api/tickets.ts
import { apiClient, ApiResponse } from './client';

export interface Ticket {
  ticket_id: string;
  title: string;
  description: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  priority: 'P1' | 'P2' | 'P3';
  category: string;
  created_by: UserSummary;
  assigned_to?: UserSummary;
  created_at: string;
  updated_at: string;
}

export interface UserSummary {
  user_id: string;
  full_name: string;
  email: string;
}

export interface PagedTickets {
  content: Ticket[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  category: string;
  priority: 'P1' | 'P2' | 'P3';
}

export interface TicketsFilters {
  status?: string;
  priority?: string;
  category?: string;
  assigned_to?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const ticketsApi = {
  async getTickets(filters: TicketsFilters = {}) {
    const { data } = await apiClient.get<ApiResponse<PagedTickets>>('/tickets', {
      params: filters,
    });
    return data.data;
  },

  async getTicket(ticketId: string) {
    const { data } = await apiClient.get<ApiResponse<Ticket>>(`/tickets/${ticketId}`);
    return data.data;
  },

  async createTicket(request: CreateTicketRequest) {
    const { data } = await apiClient.post<ApiResponse<Ticket>>('/tickets', request);
    return data.data;
  },

  async updateTicket(ticketId: string, updates: Partial<Ticket>) {
    const { data } = await apiClient.patch<ApiResponse<Ticket>>(
      `/tickets/${ticketId}`,
      updates
    );
    return data.data;
  },

  async assignTicket(ticketId: string, assignedToUserId?: string) {
    const { data} = await apiClient.post<ApiResponse<void>>(
      `/tickets/${ticketId}/assign`,
      assignedToUserId ? { assigned_to_user_id: assignedToUserId } : {}
    );
    return data.data;
  },
};
```

### 3.3 React Component with Optimistic Updates

```typescript
// features/tickets/TicketList.tsx
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
  ticketsApi, 
  type Ticket,
  type TicketsFilters, 
  type CreateTicketRequest 
} from '@/lib/api/tickets';
import { toast } from '@/components/ui/toast';
import { TicketCard } from './TicketCard';
import { TicketListSkeleton } from './TicketListSkeleton';
import { CreateTicketDialog } from './CreateTicketDialog';
import { TicketFilters } from './TicketFilters';
import { Pagination } from '@/components/ui/pagination';

export function TicketList() {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<TicketsFilters>({ page: 0, size: 20 });

  // Query: Fetch tickets with auto-refresh
  const {
    data: ticketsPage,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['tickets', filters],
    queryFn: () => ticketsApi.getTickets(filters),
    staleTime: 30_000,  // Fresh for 30 seconds
    gcTime: 5 * 60 * 1000,  // Cache for 5 minutes
    refetchOnWindowFocus: true,
    refetchInterval: 60_000,  // Poll every minute
  });

  // Mutation: Create ticket with optimistic update
  const createTicketMutation = useMutation({
    mutationFn: ticketsApi.createTicket,
    
    onMutate: async (newTicket: CreateTicketRequest) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['tickets'] });

      // Snapshot previous value
      const previousTickets = queryClient.getQueryData(['tickets', filters]);
      
      // Optimistically update cache (only if cache exists)
      if (previousTickets) {
        queryClient.setQueryData(['tickets', filters], (old: any) => {
          if (!old) return old;
          
          // Recalculate total_pages from new total_elements
          const newTotalElements = old.total_elements + 1;
          const newTotalPages = Math.ceil(newTotalElements / old.size);
          
          return {
            ...old,
            content: [
              {
                ticket_id: 'temp-' + Date.now(),
                ...newTicket,
                status: 'OPEN',
                created_at: new Date().toISOString(),
                updated_at: new Date().toISOString(),
                created_by: {
                  user_id: 'current-user',
                  full_name: 'You',
                  email: 'you@example.com',
                },
              },
              ...old.content,
            ],
            total_elements: newTotalElements,
            total_pages: newTotalPages,
          };
        });
      }

      return { previousTickets };
    },
    
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousTickets) {
        queryClient.setQueryData(['tickets', filters], context.previousTickets);
      }
      toast.error('Failed to create ticket: ' + err.message);
    },
    
    onSuccess: (newTicket) => {
      // Invalidate to fetch real data from server (replaces temp ticket)
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      toast.success(`Ticket #${newTicket.ticket_id.slice(0, 8)} created successfully`);
    },
  });

  // Mutation: Update ticket status
  const updateTicketMutation = useMutation({
    mutationFn: ({ ticketId, updates }: { ticketId: string; updates: Partial<Ticket> }) =>
      ticketsApi.updateTicket(ticketId, updates),
    
    onMutate: async ({ ticketId, updates }) => {
      await queryClient.cancelQueries({ queryKey: ['tickets'] });
      
      const previousTickets = queryClient.getQueryData(['tickets', filters]);
      
      // Optimistically update specific ticket
      queryClient.setQueryData(['tickets', filters], (old: any) => {
        if (!old) return old;
        
        return {
          ...old,
          content: old.content.map((ticket: Ticket) =>
            ticket.ticket_id === ticketId
              ? { ...ticket, ...updates, updated_at: new Date().toISOString() }
              : ticket
          ),
        };
      });
      
      return { previousTickets };
    },
    
    onError: (err, variables, context) => {
      if (context?.previousTickets) {
        queryClient.setQueryData(['tickets', filters], context.previousTickets);
      }
      toast.error('Failed to update ticket');
    },
    
    onSuccess: () => {
      toast.success('Ticket updated');
    },
  });

  if (isLoading) {
    return <TicketListSkeleton />;
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <p className="text-lg text-destructive mb-4">Failed to load tickets</p>
        <button onClick={() => refetch()} className="btn-primary">
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Tickets</h1>
        <CreateTicketDialog onSubmit={createTicketMutation.mutate} />
      </div>

      <TicketFilters filters={filters} onChange={setFilters} />

      {ticketsPage && ticketsPage.content.length > 0 ? (
        <div className="grid gap-4">
          {ticketsPage.content.map((ticket) => (
            <TicketCard
              key={ticket.ticket_id}
              ticket={ticket}
              onUpdate={(updates) =>
                updateTicketMutation.mutate({ ticketId: ticket.ticket_id, updates })
              }
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12 text-muted-foreground">
          <p>No tickets found</p>
        </div>
      )}

      {ticketsPage && ticketsPage.total_pages > 1 && (
        <Pagination
          page={ticketsPage.page}
          totalPages={ticketsPage.total_pages}
          onPageChange={(page) => setFilters({ ...filters, page })}
        />
      )}
    </div>
  );
}
```

**Key Points:**

- ✅ **Automatic Caching** - React Query handles cache
- ✅ **Optimistic Updates** - UI updates before server confirms
- ✅ **Empty Cache Guard** - checks if cache exists before updating
- ✅ **Pagination Recalc** - recomputes `total_pages` from `total_elements`
- ✅ **Error Rollback** - Reverts changes on failure
- ✅ **Background Refresh** - Auto-updates on focus and interval
- ✅ **All Imports** - includes `useState`, `Ticket`, `TicketListSkeleton`

---

## Example 4: Spring Modulith Boundary Test

### 4.1 Module Structure Verification

```java
package by.crb.mh.techsupport.ticketing;

import by.crb.mh.techsupport.TechSupportApplication;
import by.crb.mh.techsupport.shared.events.TicketCreatedEvent;
import by.crb.mh.techsupport.ticketing.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Modulith integration tests for ticketing module.
 * Verifies module boundaries and event-driven communication.
 * 
 * Note: This test is scoped to the ticketing module only.
 * Cross-module state assertions (like notification_queue) require application-level tests.
 */
@ApplicationModuleTest
class TicketingModuleTests {

    @Autowired
    private TicketService ticketService;

    /**
     * Verifies that all modules are well-structured and boundaries are clean.
     * Generates PlantUML documentation showing module dependencies.
     */
    @Test
    void shouldVerifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(TechSupportApplication.class);
        
        // Verify all modules are valid
        modules.verify();
        
        // Generate documentation
        new Documenter(modules)
            .writeDocumentation()
            .writeModulesAsPlantUml();
    }

    /**
     * Verifies that TicketCreatedEvent is published when ticket is created.
     * Only tests event publication - listener verification requires application-level test.
     */
    @Test
    void shouldPublishTicketCreatedEventWhenTicketIsCreated(Scenario scenario) {
        // Given: Prepare command
        CreateTicketCommand command = CreateTicketCommand.builder()
            .title("Network Issue in Emergency Room")
            .description("WiFi not working, urgent fix needed")
            .category("NETWORK")
            .priority("P1")
            .createdBy(testUser())
            .build();

        // When: Create ticket and wait for event
        scenario.stimulate(() -> ticketService.createTicket(command))
            
            // Then: Wait for TicketCreatedEvent
            .andWaitForEventOfType(TicketCreatedEvent.class)
            
            // And: Verify event data
            .matching(event -> {
                assertThat(event.title()).isEqualTo("Network Issue in Emergency Room");
                assertThat(event.category()).isEqualTo("NETWORK");
                assertThat(event.priority()).isEqualTo("P1");
                return true;
            });
        
        // Note: Cannot verify notification_queue here - that table is in notifications module
        // which is not part of this module-scoped test. Use application-level integration test
        // to verify cross-module side effects.
    }

    /**
     * Verifies that ticketing module does NOT depend on notifications module.
     * Ensures clean boundaries - ticketing communicates only via events.
     */
    @Test
    void ticketingModuleShouldNotDependOnNotificationsModule() {
        ApplicationModules modules = ApplicationModules.of(TechSupportApplication.class);
        
        // Get ticketing module
        var ticketingModule = modules.getModuleByName("ticketing")
            .orElseThrow(() -> new IllegalStateException("Ticketing module not found"));
        
        // Verify no dependencies on notifications module
        ticketingModule.verifyDependencies();
        
        // Ensure no dependency on notifications (allows shared and infrastructure)
        var dependencies = ticketingModule.getDependencies();
        assertThat(dependencies)
            .noneMatch(dep -> dep.getName().equals("notifications"));
    }

    private User testUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("test@crb.by")
            .fullName("Test User")
            .role("ROLE_EMPLOYEE")
            .build();
    }
}
```

### 4.2 Application-Level Integration Test

```java
package by.crb.mh.techsupport;

import by.crb.mh.techsupport.shared.events.TicketCreatedEvent;
import by.crb.mh.techsupport.ticketing.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.Scenario;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application-level integration test.
 * Tests cross-module interactions including notifications module.
 */
@SpringBootTest
class CrossModuleIntegrationTests {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifies end-to-end flow: ticket creation triggers notification queuing.
     * This test loads ALL modules, allowing verification of cross-module side effects.
     */
    @Test
    void shouldQueueNotificationWhenTicketIsCreated(Scenario scenario) {
        // Given: Prepare command
        CreateTicketCommand command = CreateTicketCommand.builder()
            .title("Network Issue in Emergency Room")
            .description("WiFi not working, urgent fix needed")
            .category("NETWORK")
            .priority("P1")
            .createdBy(testUser())
            .build();

        // When: Create ticket
        scenario.stimulate(() -> ticketService.createTicket(command))
            
            // Then: Wait for TicketCreatedEvent
            .andWaitForEventOfType(TicketCreatedEvent.class)
            
            // And: Verify notification was queued (cross-module state change)
            .andWaitForStateChange(() -> getNotificationQueueCount())
            .andVerify(count -> {
                assertThat(count).isGreaterThan(0);
            });
    }

    private User testUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("test@crb.by")
            .fullName("Test User")
            .role("ROLE_EMPLOYEE")
            .build();
    }

    private long getNotificationQueueCount() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification_queue WHERE status = 'PENDING'",
            Long.class
        );
    }
}
```

**Key Points:**

- ✅ **Module Verification** - `modules.verify()` checks boundaries
- ✅ **Event Testing** - `Scenario` API tests async event flow
- ✅ **Module Scoping** - `@ApplicationModuleTest` tests single module
- ✅ **Application Tests** - `@SpringBootTest` tests cross-module interactions
- ✅ **Dependency Check** - uses `noneMatch` instead of `allMatch` for flexibility
- ✅ **JdbcTemplate Injection** - properly declared and autowired
- ✅ **Documentation** - Auto-generates PlantUML diagrams

---

## Best Practices Summary

### Domain Events

1. ✅ **Use Records** for immutability
2. ✅ **Mapper Classes** - avoid coupling shared module to domain entities
3. ✅ **Minimal Data** - только необходимые поля
4. ✅ **Idempotent Listeners** - проверка дубликатов + `@Transactional`
5. ✅ **@Async** для неблокирующей обработки

### CQRS Read Models

1. ✅ **Denormalize** для производительности
2. ✅ **Event-Source** - строить из событий
3. ✅ **Race Condition Handling** - retry logic для concurrent updates
4. ✅ **Correct Aggregation** - sum totals first, then calculate averages
5. ✅ **Cache** результаты запросов
6. ✅ **Indexes** на часто используемые поля
7. ✅ **@Transactional(readOnly = true)** для read-only запросов

### React Query

1. ✅ **Structured Query Keys** - `['resource', filters]`
2. ✅ **Optimistic Updates** для лучшего UX
3. ✅ **Empty Cache Guards** - check before optimistic updates
4. ✅ **Pagination Handling** - recalculate total_pages
5. ✅ **Error Handling** с rollback
6. ✅ **Background Refresh** для актуальности данных
7. ✅ **Cache Invalidation** после мутаций
8. ✅ **Refresh Token** - use apiClient, not raw axios

### Module Testing

1. ✅ **Boundary Tests** - проверка изоляции модулей
2. ✅ **Event Flow Tests** - end-to-end тестирование событий
3. ✅ **Module Scope** - `@ApplicationModuleTest` for single module
4. ✅ **Application Scope** - `@SpringBootTest` for cross-module tests
5. ✅ **Documentation Generation** - PlantUML диаграммы
6. ✅ **Flexible Assertions** - use `noneMatch` for exclusions

---

**Last Updated:** 2025-11-10  
**See Also:**

- [Architecture Document](./architecture-2025-11-06.md)
- [OpenAPI Specification](./api/openapi.yaml)
- [PRD](./PRD.md)
