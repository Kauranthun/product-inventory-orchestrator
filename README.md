# Product Inventory Orchestrator

An enterprise-grade, secure asynchronous integration ecosystem built using **Apache Camel**, **Spring Boot 4**, **ActiveMQ**, and **JavaFX**. The system demonstrates a decoupled middleware architecture where a Camel Proxy acts as an "invisible security gateway" that intercepts, authenticates, and encrypts traffic before communicating asynchronously with a backend inventory service.

---

## 🏗️ System Architecture

The architecture relies on strict protocol and concern separation across four key layers:

1. **Frontend (JavaFX):** The client application requiring user authentication.
2. **Camel Integration Proxy (Port 8081):** The middleware gateway managing stateless HTTP perimeters, JWT validation, role checking, and payload transformations.
3. **Message Broker (ActiveMQ - Port 61616):** The asynchronous messaging backbone holding isolated, encrypted transport queues.
4. **Backend Microservice (Port 8082):** The core business domain manager handling data persistence and strict business rule validation.

## 🛡️ Implemented Security Architecture

The entire ecosystem is hardened based on a strict 3-Pillar Security Framework:

### 1. Route Security & RBAC (Pillar 1)
* **JWT Interception:** Custom `JwtAuthenticationProcessor` intercepts all ingress routes to extract and cryptographically verify tokens issued by the authentication service.
* **Role-Based Access Control (RBAC):** Fine-grained privileges are checked directly at the Camel routing layer:
  * `ROLE_USER` / `ROLE_ADMIN` are permitted to execute read operations (`GET`).
  * `ROLE_ADMIN` is strictly required to execute mutating operations (`POST`, `PUT`, `DELETE`).

### 2. Payload-Level Security (Pillar 2)
* **Data-in-Transit Encryption:** Message payloads sent to the ActiveMQ broker are symmetrically encrypted using **AES-128** paired with Base64 encoding at the Proxy boundaries to guarantee absolute data confidentiality inside the queues.
* **Strict Jackson Marshalling:** Mitigates Remote Code Execution (RCE) vectors by disabling polymorphic type lookups and binding payloads exclusively to explicit Data Transfer Objects (DTOs).
* **Delegated Validation:** Camel acts as a high-performance proxy; data constraint validation is deliberately delegated to the Backend service to eliminate processing overhead in the routing layer.

### 3. Endpoint & Configuration Hardening (Pillar 3)
* **Stateless Gateway:** The Spring Security 7 filter chain on the Camel Proxy enforces a `STATELESS` session creation policy and disables CSRF since all operations rely entirely on ephemeral bearer tokens.
* **Broker Hardening:** Anonymous connections to ActiveMQ are disabled. Camel utilizes secure authenticated credentials over isolated internal ports, optimized via a native Jakarta-compliant connection pool (`pooled-jms`).
* **Externalized Configuration:** Zero hardcoded secrets. All sensitive keys, database passwords, and cryptographic constants are loaded dynamically via environment variables with secure local fallbacks.

---

## ⚙️ Configuration & Environment Variables

Before launching the applications, you can externalize secrets by setting the following environment variables on your system:

| Variable Name | Description | Default Fallback |
| :--- | :--- | :--- |
| `ACTIVEMQ_USER` | Connection username for the JMS Broker | `admin` |
| `ACTIVEMQ_PASSWORD` | Connection password for the JMS Broker | `admin` |
| `APP_CRYPTO_KEY` | 16-character AES symmetric encryption key | `MySecretKey12345` |

---

## 🚀 Getting Started

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

### 2. Run the Backend Microservice
Open the backend folder in an IDE, go to the CrudApplication.java file and press run

### 3. Run the Apache Camel Proxy
Open the backend folder in an IDE, go to the DemoApplication.java file and press run

### 4. Run the JavaFX Frontend Application
Open the backend folder in an IDE, go to the Main.java file and press run

## 📂 Project Structure

├── backend/          # Spring Boot Data API & Core Domain Logic (Port 8082)

├── demo/             # Apache Camel Integration & Security Proxy (Port 8081)

├── frontend/         # JavaFX UI Client Application 

└── camunda/          # BPMN Workflow integration diagrams
