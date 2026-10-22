# Smart Vehicle Service & Management System — Backend (Spring Boot + Relational DB / SQLite REST API)

Backend server for the **Smart Vehicle Service & Management System** featuring:
- **Role-Based Authentication & JWT Security** (`CUSTOMER`, `MECHANIC`, `ADMIN`)
- **9-Stage Repair Lifecycle Engine** with Customer Quotation Approval Gate & Payment Gate
- **Equal Load-Balanced Mechanic Auto-Dispatch** & Optional Preferred Mechanic Selection
- **21-System AI Automotive Diagnostic & Cost Estimation Engine** (`/api/ai/analyze`)
- **Vehicle Registration & Insurance / PUC Compliance Renewal Tracking** (`/api/vehicles`)
- **OEM Spare Parts Inventory & Custom Restock Control** (`/api/inventory`)
- **Digital GST Tax Invoicing & Payment Processing** (`/api/invoices`)

## Running the Backend Server (Port 8080)
### Option A: Zero-Config Node + Built-In SQLite Server
```bash
node api-server.cjs
```
### Option B: Spring Boot + MySQL Server
```bash
mvn spring-boot:run
```
API Explorer available at **http://localhost:8080**.
