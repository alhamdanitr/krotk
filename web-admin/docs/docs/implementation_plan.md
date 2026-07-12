# Phase 5: Dashboard API & Reports (Multi-Tenant SaaS)

This plan outlines the architecture and tasks for building the comprehensive Dashboard and Reports APIs for the Kurotek Backend. The core focus is on SaaS architecture (Multi-Tenant isolation), professional API design (Pagination, Filtering, Sorting, Search), and readiness for Flutter integration.

## User Review Required

> [!IMPORTANT]
> The pagination architecture will introduce standard reusable generic DTOs (`PageOptionsDto`, `PageDto`, `PageMetaDto`). These are widely accepted standards but please review if you have a specific pagination format expected by the Flutter frontend.

> [!NOTE]
> Currently, there is no separate table for "Profits" because Profits are calculated dynamically from Transactions/Deposits. The API will aggregate this data on-the-fly. If you prefer a dedicated table for caching daily profits, let me know.

## Proposed Changes

### 1. Shared Utilities (Pagination, Filtering, Sorting)
To ensure consistency across all list APIs, we will create generic DTOs.
#### [NEW] `src/common/dto/page-options.dto.ts`
- Standard inputs: `order` (ASC/DESC), `page`, `take`, `search`.
#### [NEW] `src/common/dto/page-meta.dto.ts` & `src/common/dto/page.dto.ts`
- Standard outputs: `data`, `meta` (itemCount, pageCount, hasPreviousPage, hasNextPage).

### 2. Dashboard Module (`src/dashboard`)
Refactor the existing dashboard logic to use advanced aggregations and ensure 100% tenant isolation.
#### [MODIFY] `src/dashboard/dashboard.controller.ts` & `src/dashboard/dashboard.service.ts`
- **GET `/dashboard/overview`**: Total sales, total deposits, profits, active cards, pending requests (Today, Month, Year).
- **GET `/dashboard/charts`**: Time-series data for deposits vs. sales vs. profits.

### 3. Reports Module (`src/reports`)
#### [MODIFY] `src/reports/reports.controller.ts` & `src/reports/reports.service.ts`
- **GET `/reports/daily`**, **`/reports/monthly`**, **`/reports/yearly`**: Aggregated performance data.

### 4. Transactions / Sales APIs (`src/transactions`)
#### [MODIFY] `src/transactions/transactions.controller.ts` & `src/transactions/transactions.service.ts`
- **GET `/transactions`**: Paginated list of all sales.
- Search by `recipientPhone`, filter by date range, sort by `createdAt` or `amount`.

### 5. Deposits APIs (`src/deposits`)
#### [MODIFY] `src/deposits/deposits.controller.ts` & `src/deposits/deposits.service.ts`
- **GET `/deposits`**: Paginated list of deposits.
- Filter by `walletType`, search by `senderPhone`, sort by date.

### 6. Cards / Inventory APIs (`src/cards` & `src/inventory`)
#### [MODIFY] `src/cards/cards.controller.ts` & `src/cards/cards.service.ts`
- **GET `/cards`**: Paginated list of cards. Filter by `isUsed`, `categoryValue`, sort by date.

### 7. Swagger Documentation
- Add `@ApiTags`, `@ApiOperation`, `@ApiQuery`, `@ApiResponse` to all the above endpoints.
- Add `@ApiBearerAuth()` to ensure standard JWT protection visibility in Swagger.

## Verification Plan

### Automated Tests
- Build Unit tests for the Pagination utilities.
- Verify that accessing an endpoint with a specific `tenantId` (mocked in the current user context) only returns data belonging to that `tenantId`.

### Manual Verification
- Start the server and navigate to `http://localhost:3000/api/docs` (Swagger UI).
- Test filtering, pagination, and sorting directly from Swagger UI.
- Verify JSON response structure is perfectly tailored for Flutter parsing (`List<T>` with meta).
