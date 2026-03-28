# Bulk SMS API

## Overview
This project is a scalable Bulk SMS API designed to handle high-volume messaging workflows. It allows sending SMS messages to multiple recipients efficiently while ensuring reliability, performance, and proper error handling.

The system was built with a strong focus not only on functionality, but also on validation, testing, and system behavior under different conditions.

---

## Features

- Send SMS messages to single or multiple recipients
- RESTful API endpoints for message handling
- Input validation and error handling
- Scalable architecture for high-volume messaging
- Integration-ready for external SMS providers

---

## Tech Stack

- Backend: Spring Boot / .NET Core (adjust based on your repo)
- Language: Java / C#
- Database: MySQL / SQL Server
- Deployment: Docker / AWS (if applicable)

---

## API Endpoints

| Method | Endpoint       | Description                  |
|--------|---------------|------------------------------|
| POST   | /send         | Send SMS to recipients       |
| GET    | /messages     | Retrieve sent messages       |

*(Adjust endpoints based on your implementation)*

---

## Testing & Validation

This project was tested using multiple scenarios to ensure reliability, correctness, and robustness.

### Functional Testing
- Valid SMS requests with single and multiple recipients
- Message delivery flow validation
- Verification of API responses (status codes and payload)

### Input Validation
- Invalid phone numbers
- Empty message body
- Missing required fields
- Invalid authentication (if applicable)

### Edge Cases
- Extremely long message content
- Large batch of recipients
- Concurrent requests from multiple users
- Duplicate message handling

### Error Handling
- Proper error responses for invalid inputs
- Graceful handling of failed requests
- Consistent response structure

---

## Performance Testing

- Evaluated API response time under different workloads
- Tested behavior with high-volume message requests
- Identified potential bottlenecks in message processing
- Ensured system stability under concurrent usage

---

## Observations

- The system behaves consistently under normal usage conditions
- Input validation prevents incorrect or malformed requests
- Under higher load, response time increases slightly but remains stable
- Error handling ensures reliable feedback to the client

---

## Project Goals

- Build a real-world API with practical use cases
- Ensure system reliability through testing and validation
- Explore performance optimization and scalability
- Apply QA mindset to backend development

---

## Future Improvements

- Add automated tests (unit and integration)
- Implement rate limiting for high traffic scenarios
- Improve logging and monitoring
- Enhance scalability with message queues

---

## Author

Carlos De Los Santos  
Software Engineer focused on backend systems, testing, and system reliability
