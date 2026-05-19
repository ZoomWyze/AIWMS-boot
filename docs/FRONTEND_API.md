# 后端接口说明（面向前端联调）

本文档基于 `jshERP-boot` 后端代码整理，覆盖当前前端已封装的主要功能模块，便于论文撰写与前端改动联调使用。仅包含前端现有功能清单相关接口。

## 1. 项目结构与调用约定

### 1.1 后端组织架构（简述）
- Controller: `src/main/java/com/jsh/erp/controller`，对外 REST 接口入口。
- Service: `src/main/java/com/jsh/erp/service`，业务逻辑。
- Mapper/SQL: `src/main/resources/mapper_xml`，MyBatis 映射。
- Entity/VO: `src/main/java/com/jsh/erp/datasource`，实体与前端需要的视图模型。
- 公共返回体与工具: `src/main/java/com/jsh/erp/utils`。

### 1.2 基础路径与登录
- 部署时通常存在上下文路径 `/jshERP-boot`（见过滤器配置）。
- 认证由 `LogCostFilter` 控制：未登录会返回 500 并写入 `loginOut`。
- 登录后会将 `userId` 写入会话缓存，部分接口依赖 `X-Access-Token` 头。

### 1.3 通用返回格式
1) `BaseResponseInfo`
```json
{ "code": 200, "data": { } }
```

2) `TableDataInfo`
```json
{ "code": 200, "data": { "rows": [ ... ], "total": 123 } }
```

3) `returnJson/returnStr`（字符串形式 JSON）
```json
{ "code": 200, "data": { "message": "成功", "rows": [ ... ], "total": 123 } }
```

### 1.4 通用查询参数
- 列表类接口普遍使用 `search` 字段（字符串），服务端通过 `StringUtil.getInfo(search, key)` 解析。
- 分页：`currentPage` + `pageSize`（或部分接口使用 `page/rows`）。
- 排序：部分统计接口提供 `column` + `order`。

---

## 2. 用户与权限

### 2.1 登录与会话
#### POST `/user/login`
- 用途: 登录
- 请求体(JSON):
  - `loginName` string 必填
  - `password` string 必填
  - `code` string 必填（验证码）
  - `uuid` string 必填（验证码 uuid）
- 响应: `BaseResponseInfo`

#### GET `/user/logout`
- 用途: 退出登录（清理 session）
- 响应: `BaseResponseInfo`

#### GET `/user/randomImage`
- 用途: 获取验证码（登录前使用）
- 响应: `BaseResponseInfo`
  - `data.uuid` 验证码 uuid
  - `data.base64` 图片 Base64

### 2.2 用户管理
#### GET `/user/getUserList`
- 用途: 用户下拉列表
- 响应: `JSONArray`（`id`, `userName`）

#### POST `/user/addUser`
- 用途: 新增用户（含机构关系）
- 请求体(JSON): 用户信息字段
- 响应: 标准成功 JSON

#### PUT `/user/updateUser`
- 用途: 修改用户（含机构关系）
- 请求体(JSON): 用户信息字段
- 响应: 标准成功 JSON

### 2.3 菜单/权限树
#### POST `/function/findMenuByPNumber`
- 用途: 根据父编号查询菜单树
- 请求体(JSON):
  - `pNumber` string 父编号
  - `userId` string 用户 id
- 响应: `JSONArray` 菜单树

---

## 3. 租户 / 角色 / 组织

### 3.1 租户
#### GET `/tenant/checkIsNameExist`
- 用途: 校验租户名是否存在
- 请求参数: `id`, `name`
- 响应: `returnJson`（`status` boolean）

#### POST `/tenant/add`
- 用途: 新增租户
- 请求体(JSON): 租户字段
- 响应: `returnStr`

#### PUT `/tenant/update`
- 用途: 修改租户
- 请求体(JSON): 租户字段
- 响应: `returnStr`

### 3.2 角色
#### POST `/role/add`
- 用途: 新增角色
- 请求体(JSON): 角色字段
- 响应: `returnStr`

#### PUT `/role/update`
- 用途: 修改角色
- 请求体(JSON): 角色字段
- 响应: `returnStr`

#### GET `/role/allList`
- 用途: 获取全部角色列表
- 响应: `List<Role>`

### 3.3 组织
#### GET `/organization/getOrganizationTree`
- 用途: 获取组织树
- 请求参数: `id`
- 响应: `JSONArray`

#### GET `/organization/findById`
- 用途: 根据 id 获取组织信息
- 请求参数: `id`
- 响应: `BaseResponseInfo`（组织信息字段）

---

## 4. 基础资料

### 4.1 客户 / 供应商 / 会员（`/supplier/*`）
#### GET `/supplier/list`
- 用途: 列表查询
- search: `supplier`, `type`, `contacts`, `phonenum`, `telephone`
- 响应: `TableDataInfo`

#### POST `/supplier/add`
#### PUT `/supplier/update`
#### DELETE `/supplier/delete`
#### DELETE `/supplier/deleteBatch`
- 用途: CRUD

#### POST `/supplier/findBySelect_cus`
- 用途: 客户下拉
- 请求体(JSON): `key`, `organId`, `limit`

#### POST `/supplier/findBySelect_sup`
- 用途: 供应商下拉
- 请求体(JSON): `key`, `organId`, `limit`

#### POST `/supplier/findBySelect_organ`
- 用途: 往来单位下拉（客户+供应商）
- 请求体(JSON): `key`, `organId`, `limit`

#### POST `/supplier/findBySelect_retail`
- 用途: 会员下拉
- 请求体(JSON): `key`, `organId`, `limit`

#### GET `/supplier/getAllCustomer`
- 用途: 获取全部客户
- 响应: `TableDataInfo`

#### GET `/supplier/getBeginNeedByOrganId`
- 用途: 查询客户/供应商期初与欠款
- 请求参数: `organId`
- 响应: `BaseResponseInfo`

#### POST `/supplier/importVendor|importCustomer|importMember`
- 用途: 导入供应商/客户/会员
- 请求体: `MultipartFile file`
- 响应: `BaseResponseInfo`

#### GET `/supplier/exportExcel`
- 用途: 导出 Excel
- 请求参数: `supplier`, `type`, `phonenum`, `telephone`

#### POST `/supplier/batchSetAdvanceIn`
- 用途: 批量设置会员预付款
- 请求体(JSON): `ids`

#### GET `/supplier/getInfoByName`
- 用途: 按名称获取信息
- 请求参数: `name`, `type`

### 4.2 仓库（`/depot/*`）
#### GET `/depot/list` / `info` / `checkIsNameExist`
#### POST `/depot/add`
#### PUT `/depot/update`
#### DELETE `/depot/delete` / `deleteBatch`
- 用途: 仓库基础 CRUD

#### GET `/depot/getAllList`
- 用途: 仓库列表（下拉）
- 响应: `BaseResponseInfo`

#### GET `/depot/findDepotByCurrentUser`
- 用途: 当前用户可访问仓库
- 响应: `BaseResponseInfo`（JSONArray）

#### POST `/depot/updateIsDefault`
- 用途: 更新默认仓库
- 请求体(JSON): `id`

#### GET `/depot/getAllListWithStock`
- 用途: 仓库列表（带库存）
- 请求参数: `mId`

### 4.3 经手人（`/person/*`）
#### GET `/person/list` / `info` / `checkIsNameExist`
#### POST `/person/add`
#### PUT `/person/update`
#### DELETE `/person/delete` / `deleteBatch`
- 用途: 经手人基础 CRUD

#### GET `/person/getAllList`
- 用途: 经手人列表

#### GET `/person/getPersonByIds`
- 用途: 按 id 集合获取姓名
- 请求参数: `personIds`

#### GET `/person/getPersonByType`
- 用途: 按类型获取经手人
- 请求参数: `type`

#### GET `/person/getPersonByNumType`
- 用途: 按类型编码获取经手人
- 请求参数: `type`（1 销售员 / 2 仓管 / 3 财务）

### 4.4 账户（`/account/*`）
#### GET `/account/list` / `info` / `checkIsNameExist`
#### POST `/account/add`
#### PUT `/account/update`
#### DELETE `/account/delete` / `deleteBatch`
- 用途: 账户基础 CRUD

#### GET `/account/findBySelect`
- 用途: 结算账户下拉

#### GET `/account/getAccount`
- 用途: 获取全部结算账户

#### GET `/account/findAccountInOutList`
- 用途: 账户流水
- 请求参数: `currentPage`, `pageSize`, `accountId`, `initialAmount`, `number`, `beginTime`, `endTime`

#### POST `/account/updateIsDefault`
- 用途: 更新默认账户
- 请求体(JSON): `id`

#### GET `/account/listWithBalance`
- 用途: 账户带余额报表
- 请求参数: `name`, `serialNo`

#### GET `/account/getStatistics`
- 用途: 账户统计
- 请求参数: `name`, `serialNo`

### 4.5 收支项目（`/inOutItem/*`）
#### GET `/inOutItem/list` / `info` / `checkIsNameExist`
#### POST `/inOutItem/add`
#### PUT `/inOutItem/update`
#### DELETE `/inOutItem/delete` / `deleteBatch`

#### GET `/inOutItem/findBySelect`
- 用途: 收支项目下拉
- 请求参数: `type`

---

## 5. 商品与库存

### 5.1 商品（`/material/*`）
#### GET `/material/list` / `info`
- search: `categoryId`, `materialParam`, `standard`, `model`, `color`, `brand`, `mfrs`, `otherField1~3`,
  `weight`, `expiryNum`, `enableSerialNumber`, `enableBatchNumber`, `position`, `enabled`, `remark`, `mpList`

#### POST `/material/add`
#### PUT `/material/update`
#### DELETE `/material/delete` / `deleteBatch`

#### GET `/material/checkIsNameExist`
#### GET `/material/checkIsExist`

#### GET `/material/findById`
- 用途: 按 id 查询商品信息

#### GET `/material/findByIdWithBarCode`
- 参数: `meId`, `mpList`

#### GET `/material/getMaterialByParam`
- 参数: `q`

#### GET `/material/findBySelect`
- 参数: `categoryId`, `q`, `standardOrModel`, `mpList`, `depotId`, `color`, `brand`, `mfrs`, `otherField1~3`,
  `enableSerialNumber`, `enableBatchNumber`, `page`, `rows`

#### GET `/material/getMaterialByMeId`
- 参数: `meId`, `mpList`

#### GET `/material/exportExcel`
- 参数: `categoryId`, `materialParam`, `color`, `materialOther`, `weight`, `expiryNum`, `enabled`,
  `enableSerialNumber`, `enableBatchNumber`, `remark`, `mpList`

#### POST `/material/importExcel`
- 文件导入产品

#### GET `/material/getMaterialEnableSerialNumberList`
- 参数: `q`, `page`, `rows`

#### GET `/material/getMaxBarCode`

#### GET `/material/getMaterialNameList`

#### GET `/material/getMaterialByBarCode`
- 参数: `barCode`, `organId`, `depotId`, `mpList`, `prefixNo`

#### GET `/material/getListWithStock`
- 参数: `currentPage`, `pageSize`, `depotIds`, `categoryId`, `position`, `materialParam`, `zeroStock`, `column`, `order`

#### POST `/material/batchSetMaterialCurrentStock`
#### POST `/material/batchSetMaterialCurrentUnitPrice`
#### POST `/material/batchUpdate`

#### POST `/material/changeNameToPinYin`
- 参数: `name`

#### GET `/material/getMaterialDepotStock`
- 参数: `depotIds`, `materialId`

### 5.2 商品分类（`/materialCategory/*`）
#### GET `/materialCategory/list` / `info` / `checkIsNameExist`
#### POST `/materialCategory/add`
#### PUT `/materialCategory/update`
#### DELETE `/materialCategory/delete` / `deleteBatch`

#### GET `/materialCategory/getAllList`
- 参数: `parentId`

#### GET `/materialCategory/findById`
- 参数: `id`

#### GET `/materialCategory/getMaterialCategoryTree`
- 参数: `id`

### 5.3 商品扩展字段（`/materialProperty/*`）
#### GET `/materialProperty/list` / `info` / `checkIsNameExist`
#### POST `/materialProperty/add`
#### PUT `/materialProperty/update`
#### DELETE `/materialProperty/delete` / `deleteBatch`

#### POST `/materialProperty/addOrUpdate`
- 用途: 按 `nativeName` 新增或修改
- 请求体: `nativeName`, `anotherName`

#### GET `/materialProperty/getAllList`
- 用途: 获取全部扩展字段

### 5.4 商品属性（`/materialAttribute/*`）
#### GET `/materialAttribute/list` / `info` / `checkIsNameExist`
#### POST `/materialAttribute/add`
#### PUT `/materialAttribute/update`
#### DELETE `/materialAttribute/delete` / `deleteBatch`

#### GET `/materialAttribute/getNameList`
- 用途: 属性名称列表

#### GET `/materialAttribute/getValueListById`
- 参数: `id`

### 5.5 序列号（`/serialNumber/*`）
#### POST `/serialNumber/batAddSerialNumber`
- 用途: 批量添加序列号
- 请求体: `materialCode`, `serialNumberPrefix`, `batAddTotal`, `remark`

#### POST `/serialNumber/getEnableSerialNumberList`
- 用途: 查询可用序列号
- 请求体: `name`, `depotItemId`, `depotId`, `barCode`, `page`, `rows`

---

## 6. 单据与业务

### 6.1 单据主表（`/depotHead/*`）
#### GET `/depotHead/list`
- search: `type`, `subType`, `hasDebt`, `status`, `purchaseStatus`, `number`, `linkApply`, `linkNumber`,
  `beginTime`, `endTime`, `materialParam`, `organId`, `creator`, `depotId`, `accountId`, `salesMan`, `remark`

#### GET `/depotHead/info`
- 参数: `id`

#### DELETE `/depotHead/delete`
#### DELETE `/depotHead/deleteBatch`

#### POST `/depotHead/forceCloseBatch`
#### POST `/depotHead/forceClosePurchaseBatch`

#### POST `/depotHead/batchSetStatus`
- 参数: `status`, `ids`

#### GET `/depotHead/findInOutDetail`
- 参数: `currentPage`, `pageSize`, `organId`, `number`, `materialParam`, `depotId`, `beginTime`, `endTime`,
  `type`, `creator`, `categoryId`, `organizationId`, `remark`, `column`, `order`

#### GET `/depotHead/findInOutMaterialCount`
- 参数: `currentPage`, `pageSize`, `organId`, `materialParam`, `depotId`, `categoryId`, `organizationId`,
  `beginTime`, `endTime`, `type`, `column`, `order`

#### GET `/depotHead/findAllocationDetail`
- 参数: `currentPage`, `pageSize`, `number`, `materialParam`, `depotId`, `depotIdF`, `categoryId`,
  `organizationId`, `beginTime`, `endTime`, `subType`, `remark`, `column`, `order`

#### GET `/depotHead/getStatementAccount`
- 参数: `currentPage`, `pageSize`, `beginTime`, `endTime`, `organId`, `hasDebt`, `supplierType`

#### GET `/depotHead/getNeedCount`
- 参数: `type`（vendor/customer）

#### GET `/depotHead/getDetailByNumber`
- 参数: `number`

#### GET `/depotHead/getBillListByLinkNumber`
- 参数: `number`

#### POST `/depotHead/addDepotHeadAndDetail`
- 请求体: `DepotHeadVo4Body`（`info` + `rows`）

#### PUT `/depotHead/updateDepotHeadAndDetail`
- 请求体: `DepotHeadVo4Body`（`info` + `rows`）

#### GET `/depotHead/getBuyAndSaleStatistics`
- 用途: 今日/昨日/本月/本年采购、销售、零售统计

#### GET `/depotHead/getCreatorByCurrentUser`
- 用途: 当前用户可见操作员数组

#### GET `/depotHead/debtList`
- 参数: `search`, `currentPage`, `pageSize`

#### GET `/depotHead/debtExport`
- 参数: `organId`, `materialParam`, `number`, `type`, `subType`, `beginTime`, `endTime`, `status`, `mpList`

#### GET `/depotHead/waitBillList`
- 参数: `search`, `currentPage`, `pageSize`

#### GET `/depotHead/waitBillCount`
- 参数: `search`

#### POST `/depotHead/batchAddDepotHeadAndDetail`
- 请求体: `ids`

### 6.2 单据明细（`/depotItem/*`）
#### GET `/depotItem/findDetailByDepotIdsAndMaterialId`
- 参数: `pageSize`, `currentPage`, `depotIds`, `sku`, `batchNumber`, `number`, `beginTime`, `endTime`, `materialId`

#### GET `/depotItem/findStockByDepotAndBarCode`
- 参数: `depotId`, `barCode`

#### GET `/depotItem/getDetailList`
- 参数: `headerId`, `mpList`, `linkType`, `isReadOnly`

#### GET `/depotItem/getInOutStock`
- 参数: `currentPage`, `pageSize`, `depotIds`, `categoryId`, `beginTime`, `endTime`, `materialParam`, `mpList`

#### GET `/depotItem/getInOutStockCountMoney`
- 参数: `depotIds`, `categoryId`, `endTime`, `materialParam`

#### GET `/depotItem/getMaterialDepotStockByParam`
- 参数: `depotIds`, `materialId`, `unitPrice`, `beginTime`, `endTime`

#### GET `/depotItem/buyIn`
- 参数: `currentPage`, `pageSize`, `beginTime`, `endTime`, `organId`, `depotId`, `categoryId`,
  `organizationId`, `materialParam`, `mpList`

#### GET `/depotItem/retailOut`
- 参数: `currentPage`, `pageSize`, `beginTime`, `endTime`, `organId`, `depotId`, `categoryId`,
  `organizationId`, `materialParam`, `mpList`

#### GET `/depotItem/saleOut`
- 参数: `currentPage`, `pageSize`, `beginTime`, `endTime`, `organId`, `depotId`, `categoryId`,
  `organizationId`, `materialParam`, `mpList`

#### GET `/depotItem/findStockWarningCount`
- 参数: `currentPage`, `pageSize`, `materialParam`, `depotId`, `categoryId`, `mpList`

#### GET `/depotItem/buyOrSalePrice`
- 用途: 采购/销售/零售金额统计（最近 6 个月）

#### GET `/depotItem/getBatchNumberList`
- 参数: `name`, `depotItemId`, `depotId`, `barCode`, `batchNumber`

#### POST `/depotItem/importItemExcel`
- 用途: 导入单据明细
- 参数: `prefixNo`
- 文件: `MultipartFile file`

---

## 7. 系统配置

### 7.1 系统参数（`/systemConfig/*`）
#### GET `/systemConfig/list` / `info` / `checkIsNameExist`
#### POST `/systemConfig/add`
#### PUT `/systemConfig/update`
#### DELETE `/systemConfig/delete` / `deleteBatch`

#### GET `/systemConfig/getCurrentInfo`
- 用途: 获取当前租户系统配置

#### GET `/systemConfig/fileSizeLimit`
- 用途: 获取上传文件大小限制

#### POST `/systemConfig/upload`
- 用途: 文件上传（`biz`=bill|financial|material）

#### GET `/systemConfig/static/**`
- 用途: 预览图片/下载文件

#### GET `/systemConfig/static/mini/**`
- 用途: 预览缩略图/下载文件

#### POST `/systemConfig/exportExcelByParam`
- 用途: 统一 Excel 导出

### 7.2 平台参数（`/platformConfig/*`）
#### GET `/platformConfig/list` / `info`
#### POST `/platformConfig/add`
#### PUT `/platformConfig/update`
#### DELETE `/platformConfig/delete` / `deleteBatch`

#### GET `/platformConfig/getPlatform/name`
#### GET `/platformConfig/getPlatform/url`
#### GET `/platformConfig/getPlatform/registerFlag`
#### GET `/platformConfig/getPlatform/checkcodeFlag`

#### POST `/platformConfig/updatePlatformConfigByKey`
- 参数: `platformKey`, `platformValue`

#### GET `/platformConfig/getInfoByKey`
- 参数: `platformKey`

---

## 8. 常见字段/状态字典（摘录）
- 单据类型: 入库/出库/其它
- 单据状态: `0` 未审核 / `1` 已审核 / `2` 完成 / `3` 部分完成
- 出入库子类型: 采购、采购退货、零售、零售退货、销售、销售退货、调拨等

如需更细字段定义，可结合实体类 `com.jsh.erp.datasource.entities` 与 VO 类 `com.jsh.erp.datasource.vo` 对照前端字段使用。

