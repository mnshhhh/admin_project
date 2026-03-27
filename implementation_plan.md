## Overview

Implement a new asset application workflow including user application, administrator approval, external procurement, asset warehousing, and notification to the applicant.

## Files to Create/Modify

### Backend
- `backend/src/main/java/com/university/asset/entity/AssetApplication.java`: New entity for asset application.
- `backend/src/main/java/com/university/asset/entity/PurchaseOrder.java`: New entity for purchase order.
- `backend/src/main/java/com/university/asset/entity/WarehouseEntry.java`: New entity for warehouse entry.
- `backend/src/main/java/com/university/asset/dto/AssetApplicationDTO.java`: New DTO for asset application request.
- `backend/src/main/java/com/university/asset/dto/PurchaseOrderDTO.java`: New DTO for purchase order request.
- `backend/src/main/java/com/university/asset/dto/WarehouseEntryDTO.java`: New DTO for warehouse entry request.
- `backend/src/main/java/com/university/asset/vo/AssetApplicationVO.java`: New VO for asset application response.
- `backend/src/main/java/com/university/asset/mapper/AssetApplicationMapper.java`: New MyBatis Mapper for AssetApplication.
- `backend/src/main/java/com/university/asset/mapper/PurchaseOrderMapper.java`: New MyBatis Mapper for PurchaseOrder.
- `backend/src/main/java/com/university/asset/mapper/WarehouseEntryMapper.java`: New MyBatis Mapper for WarehouseEntry.
- `backend/src/main/resources/mapper/AssetApplicationMapper.xml`: New XML for AssetApplicationMapper.
- `backend/src/main/resources/mapper/PurchaseOrderMapper.xml`: New XML for PurchaseOrderMapper.
- `backend/src/main/resources/mapper/WarehouseEntryMapper.xml`: New XML for WarehouseEntryMapper.
- `backend/src/main/java/com/university/asset/service/AssetApplicationService.java`: New service for asset application logic.
- `backend/src/main/java/com/university/asset/controller/AssetApplicationController.java`: New controller for asset application API.
- `backend/src/main/resources/sql/init.sql`: Add new table creation statements and permissions.

### Frontend
- `frontend/src/api/assetApplication.ts`: New API file for asset application.
- `frontend/src/views/asset/application/index.vue`: New page for asset application form and list.
- `frontend/src/views/asset/application/approval.vue`: New page for asset application approval.
- `frontend/src/views/asset/application/purchaseOrder.vue`: New page for managing purchase orders.
- `frontend/src/views/asset/application/warehouseEntry.vue`: New page for managing warehouse entries.
- `frontend/src/router/index.ts`: Add new routes for asset application pages.
- `frontend/src/store/user.ts`: Potentially update permissions.
- `frontend/src/layout/SidebarMenu.vue`: Add new menu items.

## Step-by-Step Tasks

### 1. Database Schema and Entities
   - **Action**: Create `asset_application`, `purchase_order`, and `warehouse_entry` tables.
     - `asset_application`: `id`, `apply_no`, `applicant_id`, `dept_id`, `asset_name`, `quantity`, `reason`, `status`, `approver_id`, `approve_time`, `remark`, `create_time`, `update_time`.
     - `purchase_order`: `id`, `order_no`, `application_id`, `external_partner`, `total_amount`, `status`, `delivery_date`, `create_time`, `update_time`.
     - `warehouse_entry`: `id`, `entry_no`, `purchase_order_id`, `asset_id`, `quantity`, `entry_date`, `status`, `approver_id`, `approve_time`, `remark`, `create_time`, `update_time`.
   - **Expected Outcome**: New tables and their corresponding `AssetApplication`, `PurchaseOrder`, `WarehouseEntry` entities.
   - **Verification**: Run `init.sql` to create tables.

### 2. Backend DTOs, VOs, and Mappers
   - **Action**: Create `AssetApplicationDTO`, `PurchaseOrderDTO`, `WarehouseEntryDTO` for request bodies. Create `AssetApplicationVO` for response. Create corresponding Mapper interfaces and XMLs.
   - **Expected Outcome**: All DTOs, VOs, Mapper interfaces and XMLs are defined.
   - **Verification**: Compile the backend.

### 3. Backend Service Logic (`AssetApplicationService`)
   - **Action**: Implement service methods:
     - `applyAsset(AssetApplicationDTO)`: User submits an application.
     - `approveApplication(id, approverId, status, remark)`: Admin approves/rejects application.
     - `generatePurchaseOrder(applicationId)`: Admin generates purchase order for approved application.
     - `updatePurchaseOrder(PurchaseOrderDTO)`: External partner/admin updates purchase order details.
     - `submitWarehouseEntry(purchaseOrderId, assetId, quantity, entryDate)`: Procurement fills warehouse entry.
     - `approveWarehouseEntry(id, approverId, status, remark)`: Admin approves warehouse entry.
     - `notifyApplicant(applicationId)`: Notify applicant (e.g., via internal message or email).
     - `getAssetApplications(query)`: Get paginated list of applications with data scope.
     - `getPurchaseOrders(query)`: Get paginated list of purchase orders.
     - `getWarehouseEntries(query)`: Get paginated list of warehouse entries.
   - **Expected Outcome**: Fully implemented business logic for the workflow.
   - **Verification**: Write JUnit tests (if applicable) for the service methods. Manually test via Postman.

### 4. Backend Controller (`AssetApplicationController`)
   - **Action**: Create REST endpoints for the service methods. Apply `@PreAuthorize` for permission control and `@Log` for auditing.
     - `POST /asset/application`: Apply for asset.
     - `PUT /asset/application/{id}/approve`: Approve/reject application.
     - `POST /asset/purchase-order`: Generate purchase order.
     - `PUT /asset/purchase-order`: Update purchase order.
     - `POST /asset/warehouse-entry`: Submit warehouse entry.
     - `PUT /asset/warehouse-entry/{id}/approve`: Approve/reject warehouse entry.
     - `GET /asset/application/list`: List applications.
     - `GET /asset/purchase-order/list`: List purchase orders.
     - `GET /asset/warehouse-entry/list`: List warehouse entries.
   - **Expected Outcome**: Functional API endpoints.
   - **Verification**: Use Postman to test all endpoints.

### 5. Backend Permissions and Menus
   - **Action**: Add new permissions (e.g., `asset:apply`, `asset:application:approve`, `asset:po:manage`, `asset:entry:approve`) to `sys_menu` in `init.sql`. Update role-menu associations.
   - **Expected Outcome**: Permissions are available in the system.
   - **Verification**: Login with different roles and check if `LoginUser` has correct permissions.

### 6. Frontend API Integration
   - **Action**: Create `frontend/src/api/assetApplication.ts` with functions mapping to new backend API endpoints.
   - **Expected Outcome**: Frontend can communicate with new backend APIs.
   - **Verification**: Use browser's network tab to verify requests/responses.

### 7. Frontend Pages and Routing
   - **Action**: Create Vue pages:
     - `asset/application/index.vue`: Form for submitting applications, and list of user's applications.
     - `asset/application/approval.vue`: List of applications awaiting admin approval.
     - `asset/application/purchaseOrder.vue`: List and management of purchase orders.
     - `asset/application/warehouseEntry.vue`: List and management of warehouse entries.
   - **Action**: Add new routes to `frontend/src/router/index.ts` and menu items to `frontend/src/layout/SidebarMenu.vue`.
   - **Expected Outcome**: New pages are accessible via navigation and display relevant data.
   - **Verification**: Navigate through the new pages in the browser.

### 8. Frontend State Management and UI Logic
   - **Action**: Implement UI forms, data tables, and state management for each page. Use existing Element Plus components and Tailwind CSS for styling.
   - **Expected Outcome**: Fully functional and styled pages for the workflow.
   - **Verification**: Test the user journey: apply -> approve -> generate PO -> submit entry -> approve entry.

## Verification

- **Backend Unit Tests**: For `AssetApplicationService` to ensure core logic is sound.
- **Postman/Integration Tests**: Thoroughly test all new API endpoints with different user roles (applicant, admin).
  - Verify that `普通师生` can apply for assets.
  - Verify that `部门管理员` (or `超级管理员`) can approve/reject applications.
  - Verify that `部门管理员` can generate purchase orders.
  - Verify that purchase orders can be updated and warehouse entries submitted/approved.
  - Verify data scope rules apply correctly.
- **Frontend E2E Tests (Manual)**:
  1. Login as "张老师" (普通师生). Navigate to "资产申请" page, submit a new application.
  2. Logout. Login as "admin" (超级管理员). Navigate to "资产申请审批" page, approve the application.
  3. On the same page, initiate a "生成采购单" action.
  4. Navigate to "采购单管理" page. View the new purchase order. Simulate an external partner by manually updating its status or filling in details.
  5. Navigate to "资产入库" page. Simulate "入库" for the purchased asset, then approve the entry.
  6. Login back as "张老师". Check for notifications or updated status of the application.
- **Linting and Build**: Ensure both frontend and backend projects build successfully after changes.
  - `cd backend && mvn clean install`
  - `cd frontend && npm install && npm run build`
