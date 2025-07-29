# SMS messaging platform (sc3-intergo)

## Overview

This project implements a microservice-based SMS messaging platform in Java using Quarkus. It demonstrates core backend engineering principles including RESTful API design, asynchronous communication, and message-driven architecture. The system consists of loosely coupled services that interact through RabbitMQ to handle SMS delivery requests. It includes synchronous request validation, reliable message queuing, simulated processing, and a callback mechanism to notify the original service of the result. 

## Installation Instructions

1. **Clone the project**
   ```bash
   gh repo clone sotirisConst/sc3-intergo
   ```

2. **Navigate to the project root directory**
   ```bash
   cd sc3-intergo/
   ```

3. **Build both services using Maven**
   ```bash
   mvn clean package -f rabbitmq-producer
   mvn clean package -f rabbitmq-processor
   ```

4. **Start the system using Docker Compose**

   For Docker Compose v1:
   ```bash
   docker-compose up --build
   ```

   Or for Docker Compose v2+:
   ```bash
   docker compose up --build
   ```

5. **Verify the services are running**

   Open your browser and visit:
   ```text
   http://localhost:8080/q/health
   http://localhost:8080/q/metrics
   ```

## API Documentation

You can explore and test the available API endpoints via the built-in Swagger UI:
   ```text
   http://localhost:8080/q/swagger-ui
   ```
<img width="930" height="777" alt="image" src="https://github.com/user-attachments/assets/e8a8d22e-4f26-4c4b-83a9-bd11132a7171" />

## Testing and Experimenting
For demonstration purposes, a simple web interface has been created to showcase the functionality of the system and expose some of its APIs.
You can explore and interact with it here using simple user role permissions (username:bob, password:bob):
 ```text
   http://localhost:8080/messages.html
   ```
To see the pool of all the messages and their status, you could run the following GET endpoint (either on postman or web-browser) with admin role permisssions (username:adminsc, password:adminpass):
 ```text
   http://localhost:8080/messages/all
   ```
<img width="919" height="937" alt="image" src="https://github.com/user-attachments/assets/5ca7cf82-cabb-4ac2-bed1-ad0bc0d82132" />

## Setup & Architecture

The system consists of two microservices built with Quarkus:

### SMS Service (`rabbitmq-producer`)
- Exposes REST endpoints to send and list messages
- Validates input synchronously
- Sends accepted messages to RabbitMQ (`message-requests`)
- Streams real-time updates to the frontend via SSE
- Handles internal callbacks for delivery results

### Processor Service (`rabbitmq-processor`)
- Listens to RabbitMQ for incoming messages
- Simulates SMS delivery (random success/failure)
- Stores delivery results in the database
- Sends a callback to the SMS Service upon completion

### Message Flow
1. SMS Service receives and validates input
2. Sends it to RabbitMQ
3. Processor handles delivery and posts the result back
4. SMS Service updates the client in real-time



