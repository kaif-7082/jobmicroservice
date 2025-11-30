**Job Microservice**
* Overview The Job Microservice manages the lifecycle of job postings within the ecosystem. It is a robust service that demonstrates advanced microservice patterns, including synchronous inter-service communication via OpenFeign, fault tolerance using Resilience4j Circuit Breakers, and asynchronous background processing.

**Tech Stack**

* Java 21

* Spring Boot 3.5.7

* Spring Cloud OpenFeign

* Resilience4j (Circuit Breaker)

* MySQL & H2 Database

* Micrometer & Zipkin (Distributed Tracing)

* Spring Security (JWT)

**Configuration** This service supports multiple profiles for different environments:

**Development (Dev)**

* Profile: dev

* Port: 8090

* Database: H2 In-Memory (jdbc:h2:mem:test)

**Production (Prod)**

* Profile: prod

* Port: 8082

* Database: MySQL (jobms_db)

**Application Name**: jobms

**Key Features**

* **Synchronous Communication (OpenFeign):** Communicates directly with the Company Microservice to validate company existence before creating jobs and to fetch company details when retrieving job listings.

* **Fault Tolerance (Circuit Breaker):** Implements Resilience4j to handle failures gracefully. If the Company Microservice is down, the system prevents cascading failures by "opening the circuit" and returning controlled error responses.

* **Asynchronous Processing:** Utilizes @Async to handle non-blocking tasks, such as simulating email notifications to companies upon job creation, ensuring the user interface remains responsive.

* **Scheduled Tasks:** Includes a background scheduler (@Scheduled) that performs periodic audits (e.g., logging the total job count to the console every minute) for monitoring purposes.

* **Advanced Search & Filtering:** Offers powerful retrieval capabilities, allowing users to filter jobs by minimum salary, location, or perform keyword searches across multiple fields.

* **Role-Based Security:** Protected by JWT. ADMINs can create, update, and delete jobs, while USERs generally have read access to search and view listings.

**API Endpoints** 

| Method | Endpoint | Description | Auth |

| GET | /jobs | Get all jobs (optional companyId filter) | User/Admin |

| POST | /jobs | Create a new job | Admin |

| GET | /jobs/{jobId} | Get specific job details | User/Admin |

| PUT | /jobs/{jobId} | Update a job | Admin |

| GET | /jobs/search | Search jobs by keyword | User/Admin |

| GET | /jobs/stats/location-counts | Get stats on jobs per location | User/Admin |

**How to Run** 

Infrastructure: Ensure MySQL, Zipkin, and Eureka are running.

Run (default/dev):

./mvnw spring-boot:run

Run (prod):

./mvnw spring-boot:run -Dspring.profiles.active=prod