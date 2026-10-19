# 后台接口文档（Admin API）

> 适用工程：`system-cookies`（Spring Boot 3.5 + MyBatis-Plus 3.5 + Java 21）
> 服务端口：`8088`，上下文路径：`/`
> 基础前缀：`/api/admin/...`
> 数据格式：`application/json; charset=utf-8`
> 鉴权：所有 `/api/admin/**` 接口必须**登录且角色 `role=1`（管理员）**，未登录返回 `401`，非管理员返回 `403`。请求头 `Authorization: Bearer <token>`（也可走 `?token=` 查询参数，但仅限调试）。

---

## 0. 公共约定

### 0.1 统一响应体 `R<T>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* 业务数据 */ },
  "timestamp": 1718000000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 成功；401 未登录；403 无权限；500 业务异常 |
| message | String | 提示语 |
| data | T | 业务数据（无数据时为 `null`） |
| timestamp | Long | 响应时间戳（毫秒） |

### 0.2 分页响应 `PageResult<T>`

```json
{
  "records": [ /* 数据列表 */ ],
  "total": 100,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 10
}
```

### 0.3 业务错误码

`BizException(code, message)` 抛错由 `GlobalExceptionHandler` 捕获后写入 `R.fail(code, message)`。常见 code：

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录 / 令牌失效 |
| 403 | 角色不足（需要管理员） |
| 500 | 业务异常（参数非法、状态不允许等） |

---

## 1. 订单管理 `OrderAdminController`

**基础路径**：`/api/admin/orders`

### 1.1 订单分页查询

| 属性 | 值 |
|------|-----|
| 接口名称 | 订单分页查询（不传 userId 表示查全部用户） |
| 请求方法 | `GET` |
| 请求路径 | `/api/admin/orders` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Query 参数**（`@ModelAttribute` 绑定 `OrderQueryDTO`）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Long | 否 | 1 | 当前页 |
| pageSize | Long | 否 | 10 | 每页条数 |
| userId | Long | 否 | — | 下单用户 ID，**不传 = 查全部用户** |
| status | Integer | 否 | — | 订单状态：`0`待支付 `1`待制作 `2`制作中 `3`已完成 `4`已取消 |
| keyword | String | 否 | — | 订单号关键字（模糊匹配） |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1024,
        "orderNo": "SC202603120001",
        "userId": 8,
        "totalAmount": 88.00,
        "payAmount": 88.00,
        "itemCount": 2,
        "status": 1,
        "statusText": "待制作",
        "contactName": "张三",
        "contactPhone": "13800000000",
        "address": "阳光小区 3-2-301",
        "remark": "少辣",
        "createTime": "2026-03-12 12:00:00",
        "updateTime": "2026-03-12 12:01:00"
      }
    ],
    "total": 1, "pageNum": 1, "pageSize": 10, "pages": 1
  },
  "timestamp": 1718000000000
}
```

---

### 1.2 订单详情

| 属性 | 值 |
|------|-----|
| 接口名称 | 订单详情（含订单项） |
| 请求方法 | `GET` |
| 请求路径 | `/api/admin/orders/{id}` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单 ID |

**响应示例**（`OrderVO` = `Order` 字段 + `items` 列表 + `statusText`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1024,
    "orderNo": "SC202603120001",
    "userId": 8,
    "totalAmount": 88.00,
    "payAmount": 88.00,
    "itemCount": 2,
    "status": 1,
    "statusText": "待制作",
    "contactName": "张三",
    "contactPhone": "13800000000",
    "address": "阳光小区 3-2-301",
    "remark": "少辣",
    "createTime": "2026-03-12 12:00:00",
    "updateTime": "2026-03-12 12:01:00",
    "items": [
      {
        "id": 501,
        "orderId": 1024,
        "recipeId": 7,
        "recipeName": "番茄炒蛋",
        "coverUrl": "https://.../fqcd.jpg",
        "price": 28.00,
        "quantity": 2
      }
    ]
  },
  "timestamp": 1718000000000
}
```

---

### 1.3 推进订单状态

| 属性 | 值 |
|------|-----|
| 接口名称 | 推进订单状态（0→1→2→3 推进；4 取消） |
| 请求方法 | `PUT` |
| 请求路径 | `/api/admin/orders/{id}/status` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单 ID |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | Integer | 是 | — | 目标状态：`0`待支付 `1`待制作 `2`制作中 `3`已完成 `4`已取消 |

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

> 仅 `OrderStatus.isActive(...)`（即非「已完成 / 已取消」）的订单允许继续推进，否则业务异常 `500`。

---

## 2. 食谱管理 `RecipeAdminController`

**基础路径**：`/api/admin/recipes`

### 2.1 食谱分页

| 属性 | 值 |
|------|-----|
| 接口名称 | 食谱分页（默认含下架，可 `onSaleOnly=true` 只看上架） |
| 请求方法 | `GET` |
| 请求路径 | `/api/admin/recipes` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Query 参数**（`@ModelAttribute` 绑定 `RecipeQueryDTO`，顾客端/管理端共用）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Long | 否 | 1 | 当前页 |
| pageSize | Long | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 关键字：菜名 / 副标题 / 简介 / 菜系 |
| categoryId | Long | 否 | — | 分类 ID |
| taste | String | 否 | — | 口味 |
| cookMethod | String | 否 | — | 烹饪方法 |
| difficulty | Integer | 否 | — | 难度：`1`简单 `2`中等 `3`困难 |
| minPrice | BigDecimal | 否 | — | 最低价 |
| maxPrice | BigDecimal | 否 | — | 最高价 |
| recommendOnly | Boolean | 否 | — | 是否只看推荐 |
| onSaleOnly | Boolean | 否 | **true** | 管理端可传 `false` 同时看上下架 |
| orderBy | String | 否 | `default` | 排序：`default` / `sales` / `priceAsc` / `priceDesc` / `time` / `newest` |

**响应示例**（`RecipeListVO` = `Recipe` 字段 + `categoryName` + `stepCount`）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 7,
        "categoryId": 3,
        "categoryName": "家常菜",
        "name": "番茄炒蛋",
        "subtitle": "国民下饭菜",
        "coverUrl": "https://.../fqcd.jpg",
        "description": "酸甜开胃，5 分钟出锅",
        "cuisine": "川菜",
        "taste": "酸甜",
        "cookMethod": "炒",
        "difficulty": 1,
        "cookTime": 5,
        "servings": 2,
        "calories": 320,
        "price": 28.00,
        "originalPrice": 32.00,
        "stock": 50,
        "sales": 128,
        "isRecommend": 1,
        "status": 1,
        "stepCount": 5,
        "createTime": "2026-02-01 10:00:00",
        "updateTime": "2026-03-01 10:00:00"
      }
    ],
    "total": 1, "pageNum": 1, "pageSize": 10, "pages": 1
  },
  "timestamp": 1718000000000
}
```

---

### 2.2 食谱详情

| 属性 | 值 |
|------|-----|
| 接口名称 | 食谱详情（含下架，供编辑回填） |
| 请求方法 | `GET` |
| 请求路径 | `/api/admin/recipes/{id}` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 食谱 ID |

**响应示例**（`RecipeDetailVO` = `Recipe` 字段 + `categoryName` + 步骤 / 食材 / 调料 / 技巧 四类子表）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 7,
    "categoryId": 3,
    "categoryName": "家常菜",
    "name": "番茄炒蛋",
    "subtitle": "国民下饭菜",
    "coverUrl": "https://.../fqcd.jpg",
    "description": "酸甜开胃，5 分钟出锅",
    "cuisine": "川菜",
    "taste": "酸甜",
    "cookMethod": "炒",
    "difficulty": 1,
    "cookTime": 5,
    "servings": 2,
    "calories": 320,
    "price": 28.00,
    "originalPrice": 32.00,
    "stock": 50,
    "sales": 128,
    "isRecommend": 1,
    "status": 1,
    "createTime": "2026-02-01 10:00:00",
    "updateTime": "2026-03-01 10:00:00",
    "steps": [
      { "id": 11, "recipeId": 7, "stepNo": 1, "title": "备料", "content": "番茄切块、鸡蛋打散", "imageUrl": null, "duration": 2, "tip": "番茄去皮口感更好" }
    ],
    "ingredients": [
      { "id": 21, "recipeId": 7, "name": "番茄", "amount": "2 个", "unit": "个", "isMain": 1, "note": "熟透的更甜", "sort": 1 }
    ],
    "seasonings": [
      { "id": 31, "recipeId": 7, "name": "盐", "amount": "少许", "unit": "g", "note": null, "sort": 1 }
    ],
    "tips": [
      { "id": 41, "recipeId": 7, "title": "鸡蛋更嫩", "content": "加几滴温水", "sort": 1 }
    ]
  },
  "timestamp": 1718000000000
}
```

---

### 2.3 新增食谱

| 属性 | 值 |
|------|-----|
| 接口名称 | 新增食谱（含烹饪步骤 / 食材 / 调料 / 技巧） |
| 请求方法 | `POST` |
| 请求路径 | `/api/admin/recipes` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Request Body**（`RecipeSaveDTO`，`@Valid`）

```json
{
  "categoryId": 3,
  "name": "番茄炒蛋",
  "subtitle": "国民下饭菜",
  "coverUrl": "https://.../fqcd.jpg",
  "description": "酸甜开胃，5 分钟出锅",
  "cuisine": "川菜",
  "taste": "酸甜",
  "cookMethod": "炒",
  "difficulty": 1,
  "cookTime": 5,
  "servings": 2,
  "calories": 320,
  "price": 28.00,
  "originalPrice": 32.00,
  "stock": 50,
  "isRecommend": 1,
  "status": 1,
  "steps": [
    { "stepNo": 1, "title": "备料", "content": "番茄切块、鸡蛋打散", "imageUrl": null, "duration": 2, "tip": "番茄去皮口感更好" }
  ],
  "ingredients": [
    { "name": "番茄", "amount": "2 个", "unit": "个", "isMain": 1, "note": "熟透的更甜", "sort": 1 },
    { "name": "鸡蛋", "amount": "3 个", "unit": "个", "isMain": 1, "note": null, "sort": 2 }
  ],
  "seasonings": [
    { "name": "盐", "amount": "少许", "unit": "g", "note": null, "sort": 1 }
  ],
  "tips": [
    { "title": "鸡蛋更嫩", "content": "加几滴温水", "sort": 1 }
  ]
}
```

**字段约束**

| 字段 | 必填 | 校验 / 说明 |
|------|------|-------------|
| name | 是 | `@NotBlank` 食谱名称不能为空 |
| price | 是 | `@NotNull` 点菜价格不能为空 |
| difficulty | 否 | `@Min(1)` 难度取值非法（默认 1） |
| cookTime | 否 | `@Min(0)` 耗时不能为负（默认 0） |
| servings | 否 | `@Min(1)` 份量至少为 1（默认 1） |
| stock | 否 | `@Min(0)` 库存不能为负（默认 0） |
| status | 否 | `1`上架 `0`下架（默认 1） |
| steps[].stepNo | 是 | 步骤序号 |
| steps[].content | 是 | 步骤内容 |
| ingredients[].name | 是 | 食材名称 |
| seasonings[].name | 是 | 调料名称 |
| tips[].title | 是 | 技巧标题 |

**响应示例**

```json
{ "code": 200, "message": "新增成功", "data": { "id": 7 }, "timestamp": 1718000000000 }
```

---

### 2.4 修改食谱

| 属性 | 值 |
|------|-----|
| 接口名称 | 修改食谱（子表全量替换） |
| 请求方法 | `PUT` |
| 请求路径 | `/api/admin/recipes` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Request Body**：与「新增」完全相同，但**`id` 必填**（修改主键）。子表（步骤 / 食材 / 调料 / 技巧）为**全量替换**语义。

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

---

### 2.5 删除食谱

| 属性 | 值 |
|------|-----|
| 接口名称 | 删除食谱（MyBatis-Plus 逻辑删除，`deleted=1`） |
| 请求方法 | `DELETE` |
| 请求路径 | `/api/admin/recipes/{id}` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 食谱 ID |

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

---

### 2.6 上架 / 下架

| 属性 | 值 |
|------|-----|
| 接口名称 | 上下架切换（`status=1` 上架，`status=0` 下架） |
| 请求方法 | `PUT` |
| 请求路径 | `/api/admin/recipes/{id}/status` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 食谱 ID |

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | Integer | 是 | — | `1`上架 `0`下架 |

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

---

## 3. 食谱分类管理 `RecipeCategoryAdminController`

**基础路径**：`/api/admin/categories`

### 3.1 分类列表

| 属性 | 值 |
|------|-----|
| 接口名称 | 分类列表（启用 + 禁用全部返回） |
| 请求方法 | `GET` |
| 请求路径 | `/api/admin/categories` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**响应示例**（`RecipeCategory` 实体）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "家常菜",
      "parentId": 0,
      "icon": "https://.../home.png",
      "sort": 1,
      "status": 1,
      "createTime": "2026-01-01 00:00:00",
      "updateTime": "2026-01-01 00:00:00"
    }
  ],
  "timestamp": 1718000000000
}
```

---

### 3.2 新增分类

| 属性 | 值 |
|------|-----|
| 接口名称 | 新增食谱分类 |
| 请求方法 | `POST` |
| 请求路径 | `/api/admin/categories` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Request Body**（`RecipeCategory`）

```json
{
  "name": "家常菜",
  "parentId": 0,
  "icon": "https://.../home.png",
  "sort": 1,
  "status": 1
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| name | 是 | 分类名称 |
| parentId | 否 | 父分类 ID，`0` 表示顶级分类 |
| icon | 否 | 分类图标 URL |
| sort | 否 | 排序值，越小越靠前 |
| status | 否 | `1`启用 `0`禁用 |

**响应示例**

```json
{ "code": 200, "message": "新增成功", "data": { "id": 1 }, "timestamp": 1718000000000 }
```

---

### 3.3 修改分类

| 属性 | 值 |
|------|-----|
| 接口名称 | 修改食谱分类 |
| 请求方法 | `PUT` |
| 请求路径 | `/api/admin/categories` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Request Body**（`RecipeCategory`，**`id` 必填**）

```json
{ "id": 1, "name": "家常菜（精选）", "parentId": 0, "icon": "https://.../home.png", "sort": 1, "status": 1 }
```

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

---

### 3.4 删除分类

| 属性 | 值 |
|------|-----|
| 接口名称 | 删除食谱分类（MyBatis-Plus 逻辑删除） |
| 请求方法 | `DELETE` |
| 请求路径 | `/api/admin/categories/{id}` |
| Content-Type | `application/json` |
| 是否需要认证 | 是（管理员） |

**Path 参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类 ID |

**响应示例**

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1718000000000 }
```

> 实际删除前需先确认该分类下是否仍有食谱，业务层未做强制级联校验；前端最好在删除前提示「该分类下还有 N 道菜」。

---

## 4. 接口速查表

| 模块 | 方法 | 路径 | 鉴权 | 摘要 |
|------|------|------|------|------|
| 订单 | GET | `/api/admin/orders` | 管理员 | 订单分页查询 |
| 订单 | GET | `/api/admin/orders/{id}` | 管理员 | 订单详情 |
| 订单 | PUT | `/api/admin/orders/{id}/status?status=` | 管理员 | 推进订单状态 |
| 食谱 | GET | `/api/admin/recipes` | 管理员 | 食谱分页 |
| 食谱 | GET | `/api/admin/recipes/{id}` | 管理员 | 食谱详情 |
| 食谱 | POST | `/api/admin/recipes` | 管理员 | 新增食谱 |
| 食谱 | PUT | `/api/admin/recipes` | 管理员 | 修改食谱 |
| 食谱 | DELETE | `/api/admin/recipes/{id}` | 管理员 | 删除食谱 |
| 食谱 | PUT | `/api/admin/recipes/{id}/status?status=` | 管理员 | 上下架 |
| 分类 | GET | `/api/admin/categories` | 管理员 | 分类列表 |
| 分类 | POST | `/api/admin/categories` | 管理员 | 新增分类 |
| 分类 | PUT | `/api/admin/categories` | 管理员 | 修改分类 |
| 分类 | DELETE | `/api/admin/categories/{id}` | 管理员 | 删除分类 |

---

## 5. 配套 Postman 集合

`docs/postman/system-cookies-admin.postman_collection.json` 已落盘，Postman / Apifox / Apipost 均可直接 **Import → File** 导入。集合内含：

- 变量：`baseUrl`（默认 `http://localhost:8088`）、`token`（管理员登录后填入）
- 三个分组：订单管理 / 食谱管理 / 分类管理
- 13 个接口请求 + 成功响应示例
- 集合级 Bearer Token 鉴权头（`Authorization: Bearer {{token}}`）
