# FoodWaste.AI

An AI-powered food waste reduction system for restaurants and food businesses.
Built with Java Spring Boot and PostgreSQL.

---

## The Problem

Food businesses — restaurants, canteens, catering services — consistently over-order
and over-prepare, leading to massive food waste and direct revenue loss. There is no lightweight, intelligent tool that helps them forecast demand and optimize supply
routes without expensive enterprise software.

## What FoodWaste.AI Does

- Forecasts surplus food quantities using Holt-Winters exponential smoothing
- Optimizes redistribution routes using a Nearest Neighbour VRP (Vehicle Routing Problem) algorithm
- Exposes clean REST API endpoints consumable by any frontend or mobile app
- Built entirely from scratch in Java — no ML library dependencies

---

## Tech Stack

| Layer       | Technology 
| Backend     | Java 17, Spring Boot 3 
| Database    | PostgreSQL (JPA/Hibernate) 
| Algorithms  | Holt-Winters (forecasting), Nearest Neighbour VRP (routing) 
| Auth        | JWT (in progress) 
| Build Tool  | Maven 

---

## API Endpoints

| Method | Endpoint        | Description 
| GET    | `/api/health`   | Health check 
| POST   | `/api/forecast` | Predict surplus food quantity 
| POST   | `/api/route`    | Optimize redistribution route 

---

## What's Built So Far

- [x] Project setup with Spring Boot 3 + PostgreSQL
- [x] Holt-Winters forecasting algorithm (pure Java)
- [x] Nearest Neighbour VRP routing algorithm (pure Java)
- [x] Three working REST API endpoints
- [x] JPA/Hibernate auto-creating database tables
- [x] CORS configured for React frontend (port 5173)
- [ ] JWT Authentication (in progress)
- [ ] Real ML model integration
- [ ] Natural language query interface

---

## How to Run Locally

### Prerequisites
- Java 17
- Maven
- PostgreSQL running on localhost:5432

### Setup

1. Clone the repository
   git clone https://github.com/promitib/foodwaste-ai.git

2. Create the database
   CREATE DATABASE foodwaste;

3. Configure your credentials in src/main/resources/application.properties
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD

4. Run the project from inside the food-waste-ai folder
   cd food-waste-ai
   mvn spring-boot:run

5. Hit the health check to confirm it's running
   GET http://localhost:8080/api/health

---

## Roadmap

- [ ] Complete JWT auth (register, login, token refresh)
- [ ] Integrate a real ML forecasting model
- [ ] Add natural language interface ("How much rice will we waste this week?")
- [ ] Build React frontend dashboard
- [ ] Deploy to Railway or Render

---

## Author

Promiti — Computer Science & Business Systems, final year
Building at the intersection of AI and real-world operational problems.