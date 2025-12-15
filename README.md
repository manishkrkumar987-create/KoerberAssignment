# Körber InventoryService & Order Management System

## 📦 Service Overview
This project is a microservice-based solution for managing stock and orders. It is built using Java 17 and Spring Boot, with a focus on the First Expired, First Out (FEFO) principle.

# 🚀 Key Features

* **Microservices**: Two independent services (Inventory and Order) communicating via REST

* **Batch Management**: Products are stored in batches, each with its own batch_id, quantity, and expiry_date.

* **Factory Design Pattern**: Implements a strategy-based approach for updating stock. This allows the system to switch between FEFO or other future strategies ( like LIFO) without changing the core service code.

* **Database Migrations**: Automated schema creation and data seeding using Liquibase.

* **Automated Data Seeding**: Uses Liquibase to read inventory.csv and populate the H2 database on startup.

* **Inter-Service Communication**: The Order Service communicates with the Inventory Service using RestTemplate to validate and reserve stock.


## 🛠 Tech Stack
Java 17

Spring Boot 3.x

Spring Data JPA (Persistence)

H2 Database (In-memory)

Liquibase (Database Migrations)

Lombok (Boilerplate reduction)

## Running the Services
You must run both services simultaneously in separate terminal windows.

* **Start the Inventory Service (Port 8081)**

```Bash

cd InventoryService
mvn spring-boot:run
```

* ** Start the Order Service (Port 8082)**

```Bash

cd OrderService
mvn spring-boot:run
```
## 📑 API Documentation
**1. Inventory Service (Port 8081)**
```Method	Endpoint	Description
GET	/inventory/{productId}	Returns all batches for a product sorted by expiry date.
POST	/inventory/update	Internal logic to deduct stock based on the FEFO strategy.
```
**2. Order Service (Port 8082)**
```
MethodEndpointDescription POST/orderCreates an order and triggers stock deduction in Inventory
```
**Example Order Request Body:**

```JSON

{
    "productId": 1005,
    "quantity": 10
}
```

Get Sorted Inventory
Returns all batches for a specific product, sorted by the closest expiry date first.

* **URL**: /inventory/{productId}
* **Method**: `GET`
* **Success Response**:

#### JSON
```json
{
  "productId": 1005,
  "productName": "Smartwatch",
  "batches": [
    { "batchId": 5, "quantity": 39, "expiryDate": "2026-03-31" },
    { "batchId": 7, "quantity": 40, "expiryDate": "2026-04-24" }
  ]
}
```

Update Inventory (Internal)
Deducts stock based on the FEFO strategy. This is typically called by the Order Service.

* **URL**: /inventory/update
* **Method**: `POST`
* **Params**: `productId` (Long), `quantity` (Integer)

* **Returns**: `List<Long>` (The IDs of the batches affected by this deduction).

## 🏗 Design Patterns: The Factory Pattern
The service uses a `StockUpdateFactory` to determine how stock should be deducted.

Strategy Interface: `StockUpdateStrategy`

Concrete Strategy: `FEFOUpdateStrategy` (Deducts from the earliest expiring batches first).

Factory: StockUpdateFactory (Returns the correct strategy bean).

## 🧪 Testing
The service includes unit tests to ensure the FEFO logic works correctly even when an order spans multiple batches.

Run tests via Maven:

```Bash

mvn test
```
**Key Test Case**: `shouldDeductFromMultipleBatchesWhenFirstIsInsufficient()`

* Tests that if Batch A has 10 units and Batch B has 10 units, an order of 15 units correctly leaves Batch A with 0 and Batch B with 5.

## 💾 Database & Persistence

The Inventory service utilizes a StockUpdateFactory to determine how stock should be deducted.

**1. Strategy Interface**: `StockUpdateStrategy`

**2. Concrete Strategy**: `FEFOUpdateStrategy` (Deducts from the earliest expiring batches first).

 **3.Factory**: `StockUpdateFactory`

**4.Console**: `http://localhost:8081/h2-console`

**5.JDBC URL**: inventoryService: `jdbc:h2:mem:inventorydb` , orderService:`jdbc:h2:mem:orderdb`

**6.Migration Path**: `src/main/resources/db/changelog/master.xml`

**7.CSV Data Path**: `src/main/resources/db/changelog/data/inventory.csv`
