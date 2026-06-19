# Product Inventory Orchestrator

An enterprise-grade, secure asynchronous integration ecosystem built using **Apache Camel**, **Spring Boot 4**, **ActiveMQ**, and **JavaFX**. The system demonstrates a decoupled middleware architecture where a Camel Proxy acts as an "invisible security gateway" that intercepts, authenticates, and encrypts traffic before communicating asynchronously with a backend inventory service.

---

## System Architecture

The architecture relies on strict protocol and concern separation across four key layers:

1. **Frontend (JavaFX):** The client application requiring user authentication.
2. **Camel Integration Proxy (Port 8443):** The middleware gateway managing stateless HTTPs perimeters, JWT validation, role checking, and payload transformations.
3. **Message Broker (ActiveMQ - Port 61616):** The asynchronous messaging backbone holding isolated, encrypted transport queues.
4. **Backend Microservice (Port 8082):** The core business domain manager handling data persistence and strict business rule validation.


## Configuration & Environment Variables

Before launching the applications, you can externalize secrets by setting the following environment variables on your system:

| Variable Name          | Description                                 | Default Fallback |
|:-----------------------|:--------------------------------------------|:-----------------|
| `ACTIVEMQ_USER`        | Connection username for the JMS Broker      | `admin`          |
| `ACTIVEMQ_PASSWORD`    | Connection password for the JMS Broker      | `admin`          |
| `CAMEL_PGP_PASSPHRASE` | Password of the encrypt and decrypt gpg key | `corentin`       |

---

## Getting Started

### Prerequisites
* **Java 21** (Eclipse Adoptium/Hotspot recommended)
* **Apache ActiveMQ** (Running locally on default ports)
* **Maven 3.x**

### 1. Launch the Message Broker
Download apache-activemq-6.2.6-bin.zip at this url https://activemq.apache.org/components/classic/download/classic-06-02-06 :
```bash
# Go to your ActiveMQ installation directory and run:
bin\activemq start
```

### 2. Run the Backend
Open the backend folder in an IDE, go to the CrudApplication.java file and press run

### 3. Run the Apache Camel Proxy
Open the demo folder in an IDE, go to the DemoApplication.java file and press run

### 4. Run the JavaFX Frontend Application
Open the frontend folder in an IDE, go to the Main.java file and press run

## Project Structure

├── backend/          # Spring Boot Data API & Core Domain Logic (Port 8082)

├── demo/             # Apache Camel Integration & Security Proxy (Port 8443)

├── frontend/         # JavaFX UI Client Application 

└── camunda/          # BPMN Workflow integration diagrams
