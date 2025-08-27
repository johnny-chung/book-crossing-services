# Book Crossing Services

## Introduction

Book Crossing Services is a microservices-based platform designed to facilitate book sharing, ordering, posting, messaging, and member management. The system leverages modern cloud-native technologies, including Spring Boot, Docker, Kubernetes, and Apache Kafka, to deliver scalable, resilient, and maintainable services.

This repository contains multiple services:

- **book-service**: Manages book data and operations.
- **post-service**: Handles posts related to book sharing.
- **order-service**: Manages book orders and transactions.
- **member-service**: Manages user/member data.
- **message-service**: Handles messaging between users.
- **core-module**: Shared code and utilities across services.
- **infra/k8s**: Kubernetes deployment manifests.
- **kafka**: Helm charts and configuration for Apache Kafka.

## Features

- **Microservices Architecture**: Each domain (book, post, order, member, message) is a separate Spring Boot service.
- **MongoDB Integration**: Each service uses MongoDB for persistence.
- **Kafka Messaging**: Asynchronous communication between services via Apache Kafka.
- **RESTful APIs**: Exposes endpoints for CRUD and business operations.
- **Dockerized**: Each service is containerized for easy deployment.
- **Kubernetes Ready**: Manifests for local and production deployments.
- **Scalable & Resilient**: Designed for cloud environments with horizontal scaling.

## Technology Stack

- ![Java](https://img.shields.io/badge/Java-21-blue?logo=java) **Java 21** (Eclipse Temurin JDK)
- ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot) **Spring Boot** (with Data MongoDB, Web, etc.)
- ![MongoDB](https://img.shields.io/badge/MongoDB-6.x-green?logo=mongodb) **MongoDB**
- ![Apache Kafka](https://img.shields.io/badge/Kafka-3.x-black?logo=apachekafka) **Apache Kafka** (Bitnami Helm Chart)
- ![Docker](https://img.shields.io/badge/Docker-24.x-blue?logo=docker) **Docker**
- ![Kubernetes](https://img.shields.io/badge/Kubernetes-1.23+-blue?logo=kubernetes) **Kubernetes**
- ![Gradle](https://img.shields.io/badge/Gradle-8.x-green?logo=gradle) **Gradle** (build tool)
- ![Helm](https://img.shields.io/badge/Helm-3.8.0+-blue?logo=helm) **Helm** (for managing Kubernetes charts)

## Project Structure

- `book-service/`, `post-service/`, `order-service/`, `member-service/`, `message-service/`: Microservices source code and Dockerfiles.
- `core-module/`: Shared codebase.
- `infra/k8s/`: Kubernetes manifests for deployments, ingress, etc.
- `kafka/`: Helm charts and configuration for Kafka.
- `skaffold*.yml`: Skaffold configuration for local development and CI/CD.
- `docker-command.txt`: Docker build and push commands.

## Prerequisites

- **Java 21+**
- **Docker**
- **Kubernetes (minikube, kind, or cloud provider)**
- **Helm 3.8.0+**
- **MongoDB** (local or cloud instance)
- **PV provisioner support** in your Kubernetes cluster (for Kafka persistence)

## Setup & Running Locally

### 1. Clone the Repository

```sh
git clone https://github.com/your-org/book-crossing-services.git
cd book-crossing-services
```

### 2. Build Docker Images

Use the provided commands in `docker-command.txt`:

```sh
docker build -t goodmanisltd/bookcrossing-book-service:latest ./book-service
docker build -t goodmanisltd/bookcrossing-post-service:latest ./post-service
docker build -t goodmanisltd/bookcrossing-order-service:latest ./order-service
docker build -t goodmanisltd/bookcrossing-member-service:latest ./member-service
docker build -t goodmanisltd/bookcrossing-message-service:latest ./message-service
```

### 3. Push Images (if using remote registry)

```sh
docker push goodmanisltd/bookcrossing-book-service:latest
docker push goodmanisltd/bookcrossing-post-service:latest
docker push goodmanisltd/bookcrossing-order-service:latest
docker push goodmanisltd/bookcrossing-member-service:latest
docker push goodmanisltd/bookcrossing-message-service:latest
```

### 4. Deploy Kafka

Navigate to the `kafka/` directory and install Kafka using Helm:

```sh
helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka
```

Refer to `kafka/README.md` for advanced configuration and external access setup.

### 5. Deploy MongoDB

Deploy MongoDB using your preferred method (local Docker, cloud, or Helm chart).

### 6. Deploy Services to Kubernetes

Apply the manifests in `infra/k8s/`:

```sh
kubectl apply -f infra/k8s/book-service-depl-local.yml
kubectl apply -f infra/k8s/post-service-depl-local.yml
kubectl apply -f infra/k8s/order-service-depl-local.yml
kubectl apply -f infra/k8s/member-service-depl-local.yml
kubectl apply -f infra/k8s/message-service-depl-local.yml
kubectl apply -f infra/k8s/ingress-depl.yml
```

### 7. Access the Services

- Use the configured ingress or NodePort to access APIs.
- Default ports (from Dockerfiles):
  - book-service: 8081
  - post-service: 8085
  - order-service: 8083
  - member-service: 8087
  - message-service: 8089

## Environment Variables

Each service may require environment variables for MongoDB connection, Kafka brokers, etc. Example:

- `SPRING_DATA_MONGODB_URI`
- `KAFKA_BOOTSTRAP_SERVERS`
- `SERVER_PORT`

Refer to each service's `application.yml` or deployment manifest for details.

## Development Environment

- Recommended: Use a dedicated environment (e.g., `book-crossing-dev`) for local development.
- For production, configure secrets and environment variables securely.

## Advanced Topics

- **Scaling**: Use Kubernetes Horizontal Pod Autoscaler.
- **Monitoring**: Integrate Prometheus and Grafana for metrics (see Kafka chart for JMX exporter).
- **Persistence**: Kafka and MongoDB require persistent volumes.
- **Security**: Configure SASL/TLS for Kafka, secure MongoDB, and use Kubernetes secrets.

## Upgrading

Refer to the Kafka and Helm chart documentation for upgrade notes and breaking changes.


## Contribution

Contributions are welcome! Please fork the repo and submit a pull request. For major changes, open an issue first to discuss what you would like to change.

## Contact

For questions, job opportunities, or feedback:

- **Author:** Johnny C.
- **LinkedIn:** [johnny-wychung](https://www.linkedin.com/in/johnny-wychung/)

---

## License

This project is licensed under the Apache License 2.0. See [LICENSE](kafka/README.md#license) for details.

---

For more details, see the individual service documentation and the Kafka Helm chart README. If you have questions or need troubleshooting, consult the [Bitnami Helm Chart Troubleshooting Guide](https://docs.bitnami.com/general/how-to/troubleshoot-helm-chart-issues).
