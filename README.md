# SMS messaging platform (sc3-intergo)

## Overview

This project implements a microservice-based SMS messaging platform in Java using Quarkus. It demonstrates core backend engineering principles including RESTful API design, asynchronous communication, and message-driven architecture. The system consists of loosely coupled services that interact through RabbitMQ to handle SMS delivery requests. It includes synchronous request validation, reliable message queuing, simulated processing with randomized delivery outcomes, and a callback mechanism to notify the original service of the result. The design highlights proficiency in Java microservices, message broker integration, and robust error handling practices.

## Installation Instructions

1. Clone the project
   
2. Navigate to the project root directory:

cd /sc3-intergo/

3.Build both services using Maven:
mvn clean package -f rabbitmq-producer
mvn clean package -f rabbitmq-processor

4. Start the system using Docker Compose:

If you're using Docker Compose v1:
docker-compose up --build
Or for Docker Compose v2+:
docker compose up --build

5.Verify the services are running:
Open your browser and visit: http://localhost:8080/q/health
