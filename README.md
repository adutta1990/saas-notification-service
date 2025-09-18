# Notification System

A Spring Boot-based notification system with tenant-based rate limiting, user preferences, and multi-database support.

## Features

- **Multi-channel notifications**: SMS, IVRS, Push notifications, Email
- **User preference management**: Users can control notification settings per type
- **Priority-based processing**: OTP has highest priority, marketing emails/newsletters have lower priority
- **Tenant-based rate limiting**: Token bucket algorithm for rate limiting per tenant
- **Bulk notification support**: Send notifications to multiple recipients
- **Asynchronous processing**: Queue-based notification processing with Kafka support
- **REST API**: RESTful endpoints for notifications and user preferences
- **Multi-database support**: H2 (default) and MySQL configurations

## Architecture

- **Rate Limiting**: Token bucket algorithm with configurable capacity and refill rate per tenant
- **Priority Queue**: Notifications are processed based on type priority (OTP > IVRS > SMS > Push > Marketing > Newsletter)
- **User Preferences**: Database-stored preferences control notification delivery per user/tenant
- **Async Processing**: Multi-threaded notification processing with Kafka and queue support
- **Tenant Configuration**: Per-tenant configuration for rate limits and service settings
- **Database Layer**: JPA entities with MySQL and H2 support

## API Endpoints

### Notifications

#### Send Single Notification
```
POST /api/v1/notifications/send
```

Example request:
```json
{
  "tenantId": "tenant1",
  "type": "OTP",
  "recipient": "+1234567890",
  "message": "Your OTP is 123456",
  "subject": "OTP Verification"
}
```

#### Send Bulk Notification
```
POST /api/v1/notifications/send/bulk
```

Example request:
```json
{
  "tenantId": "tenant1",
  "type": "MARKETING_EMAIL",
  "recipients": ["+1234567890", "+0987654321"],
  "message": "Check out our latest offers!",
  "subject": "Special Offers"
}
```

### Tenant Configuration

#### Get Tenant Configuration
```
GET /api/v1/tenants/{tenantId}/config
```

#### Update Tenant Configuration
```
PUT /api/v1/tenants/{tenantId}/config
```

Example request:
```json
{
  "rateLimitCapacity": 1000,
  "rateLimitRefillRate": 50
}
```

#### Check Available Rate Limit Tokens
```
GET /api/v1/tenants/{tenantId}/rate-limit/tokens
```

### User Management

#### Create or Update User
```
POST /api/users
```

Example request:
```json
{
  "tenantId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "name": "John Doe"
}
```

#### Get User
```
GET /api/users/{tenantId}
```

### User Notification Preferences

#### Save User Notification Preference
```
POST /api/users/{tenantId}/preferences
```

Example request:
```json
{
  "notificationType": "EMAIL",
  "enabled": true,
  "deliveryHourStart": 9,
  "deliveryHourEnd": 18,
  "maxFrequencyPerDay": 5
}
```

#### Get All User Preferences
```
GET /api/users/{tenantId}/preferences
```

#### Get Specific Preference
```
GET /api/users/{tenantId}/preferences/{notificationType}
```

#### Delete Preference
```
DELETE /api/users/{tenantId}/preferences/{notificationType}
```

## Notification Types and Priorities

1. **OTP** (Priority 1) - Highest priority
2. **IVRS** (Priority 2)
3. **SMS** (Priority 3)
4. **PUSH** (Priority 4)
5. **EMAIL** (Priority 5)
6. **MARKETING_EMAIL** (Priority 6)
7. **NEWSLETTER** (Priority 7) - Lowest priority

## User Preference Controls

Users can control notifications with the following settings:

- **enabled**: Enable/disable notifications for a specific type
- **deliveryHourStart/End**: Time window for notification delivery (0-23)
- **maxFrequencyPerDay**: Maximum notifications per day (optional)

Notifications are automatically **BLOCKED** if:
- User has disabled the notification type
- Current time is outside delivery hours
- User preferences are not met

## Configuration

Default configuration in `application.yml`:

```yaml
notification:
  rate-limit:
    default-capacity: 100
    default-refill-rate: 10
  queue:
    max-size: 10000
```

## Database Setup

### H2 Database (Default)
The application uses H2 in-memory database by default. No additional setup required.

### MySQL Database
To use MySQL, create a database named `notification_system` and run with the MySQL profile:

```bash
java -jar notification-system.jar --spring.profiles.active=mysql
```

MySQL configuration (`application-mysql.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notification_system
    username: root
    password: password
```

## Running the Application

1. Import the project into IntelliJ IDEA as a Maven project
2. Ensure Java 17 is configured
3. Run the `NotificationSystemApplication` class
4. The application will start on port 8080

### Database Profiles
- **Default (H2)**: `java -jar notification-system.jar`
- **MySQL**: `java -jar notification-system.jar --spring.profiles.active=mysql`

## Testing

### H2 Console
Access at: http://localhost:8080/h2-console
- URL: jdbc:h2:mem:testdb
- Username: sa
- Password: password

### API Testing
Health check endpoint: http://localhost:8080/api/v1/notifications/health

### Database Tables
The application automatically creates these tables:
- `users` - User/tenant information
- `user_notification_preferences` - User notification settings
- `notification_audit` - Notification history (Cassandra)