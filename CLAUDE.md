# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Maven Commands
- **Build the project**: `mvn clean compile`
- **Run tests**: `mvn test`
- **Run the application**: `mvn spring-boot:run`
- **Package the application**: `mvn package`
- **Clean build artifacts**: `mvn clean`

### Database Setup
- **PostgreSQL database**: `crowdfund` (configured for localhost:5432)
- **Initialize database**: Execute `database/create_tables.sql` in PostgreSQL
- **Database user**: `gaurair` (password configured in application.properties)

### Application Configuration
- **Server port**: 8080
- **Database**: PostgreSQL with JPA/Hibernate (DDL auto-update enabled)
- **Security**: JWT-based authentication with bcrypt password encoding
- **API base path**: `/v1/api/`

## Architecture Overview

### Core Technologies
- **Spring Boot 3.4.5** with Java 23
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Lombok** for code generation
- **Maven** for build management

### Package Structure
```
com.example.crowdfund/
├── entity/           # JPA entities (User, Campaign, Category)
├── controller/       # REST controllers (Auth, User)
├── service/          # Business logic services
├── repository/       # Data access layer
├── config/           # Security and application configuration
├── DTO/              # Data transfer objects
├── enums/            # Enumerations (CampaignStatus)
├── util/             # Utility classes
└── GloablExceptionHandler/  # Global exception handling
```

### Key Entities and Relationships
- **User**: Implements UserDetails for Spring Security, includes Stripe integration fields
- **Campaign**: Core crowdfunding entity with status, funding goals, and progress tracking
- **Category**: Campaign categorization system
- **Database relationships**: Users create campaigns, campaigns belong to categories

### Security Configuration
- JWT-based stateless authentication
- Public endpoints: `/v1/api/auth/**` and `/v1/api/public/**`
- All other endpoints require authentication
- Password encoding with BCrypt
- Custom UserDetailsService implementation

### Authentication Flow
- Registration: `POST /v1/api/auth/register`
- Login: `POST /v1/api/auth/login` (returns JWT token)
- JWT filter validates tokens for protected endpoints

### Database Schema
- Complete PostgreSQL schema in `database/create_tables.sql`
- Includes comprehensive tables for users, campaigns, categories, contributions, comments, likes, and payments
- Proper foreign key constraints and indexes for performance
- Campaign statistics view for reporting

### Configuration Notes
- JWT secret and API key are configured in `application.properties`
- Database connection uses PostgreSQL with auto-update DDL
- Spring profiles available for different environments
- In-memory test users configured for development