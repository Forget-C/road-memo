# RoadMemo V0.1 最终可开发数据模型设计

## 1. 设计目标

本版数据模型面向以下目标：

- 支持燃油车、纯电车、插混车、油混车
- 用统一的能源记录承载加油与充电
- 金额存储避免浮点误差
- 首页汇总、统计、提醒都能直接落地
- 为后续云同步和附件能力预留扩展空间，但不提前引入复杂度

本设计基于以下结论：

- 首版保留 `Vehicle / EnergyRecord / MaintenanceRecord / ExpenseRecord / RenewalRecord / Reminder`
- `currentMileage` 不落表，作为派生值使用
- `unitPrice` 不作为数据库权威字段，优先以总金额与数量为准
- 插混车首版只做油、电分开统计，不做综合等效能耗

## 2. 核心建模决策

### 2.1 车辆动力类型

使用 `powertrain_type` 表示车辆动力形态：

- `GASOLINE`
- `DIESEL`
- `HEV`
- `PHEV`
- `EV`

派生支持的能源类型：

- `GASOLINE` / `DIESEL` / `HEV` -> `FUEL`
- `EV` -> `ELECTRIC`
- `PHEV` -> `FUEL + ELECTRIC`

### 2.2 能源记录统一建模

用 `energy_records` 统一承载：

- 加油记录
- 充电记录

区别通过 `energy_type` 和扩展字段表达：

- 燃油记录：油号、是否加满、加油站
- 充电记录：充电方式、是否充满、充电站

### 2.3 金额与数量精度规则

金额统一使用整数：

- `amount_in_cent: Long`

能源数量也统一使用整数放大：

- `quantity_in_thousandth: Long`

含义：

- 燃油：单位为 `0.001 L`
- 电量：单位为 `0.001 kWh`

这样可以避免 `Double` 参与数据库存储。

### 2.4 默认车辆规则

默认车辆仅由 `vehicles.is_default` 表示。  
切换默认车辆时必须走事务：

1. 清空所有车辆默认标记
2. 设置目标车辆为默认

### 2.5 里程规则

首版统一按“录入时不允许回退”处理：

- 同一车辆新增记录时，里程不得小于该车辆最近一条带里程记录
- 首版不做复杂历史补录重排

### 2.6 续期事项独立建模

保险、年检、车船税这类事项既是费用，也是“有有效期”的到期事项。  
若只放在 `expense_records` 中，会导致：

- 费用有了，但没有有效期
- 提醒需要手工重复录入
- 续保历史和到期状态难以维护

因此首版引入 `renewal_records`：

- 用于承载保险、年检、车船税等续期类事项
- 仍然计入总支出
- 同时为提醒提供明确数据来源

### 2.7 提醒生命周期规则

提醒不只是“有无提醒时间”，还要有明确状态流转。

建议状态：

- `PENDING`：待触发
- `TRIGGERED`：已触发但未处理
- `DONE`：用户已处理
- `DISMISSED`：用户忽略或关闭

建议约束：

- 提醒触发后保留记录，不直接删除
- 来源记录删除时，关联提醒需同步失效或删除
- 处理完成后不再重复触发

## 3. 表结构总览

```text
vehicles
├── 1:N energy_records
├── 1:N maintenance_records
├── 1:N expense_records
├── 1:N renewal_records
└── 1:N reminders
```

## 4. 数据表设计

### 4.1 vehicles

表用途：车辆主数据。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| brand | TEXT | 是 | 品牌，如 Toyota |
| model | TEXT | 是 | 车型，如 Camry |
| plate_number | TEXT | 否 | 车牌号 |
| purchase_date_epoch_day | INTEGER | 否 | 购车日期，按 epochDay 存储 |
| powertrain_type | TEXT | 是 | 见枚举定义 |
| note | TEXT | 否 | 备注 |
| is_default | INTEGER | 是 | 0/1 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_vehicles_is_default`
- `index_vehicles_brand_model`

业务约束：

- `brand`、`model` 不能为空
- 首辆车自动设为默认
- 删除默认车辆后，若仍有剩余车辆，系统自动选择一辆设为默认

删除策略：

- V1 删除车辆时弹出明确确认提示
- 用户确认后，级联硬删除该车辆下的全部关联数据：
  - `energy_records`
  - `maintenance_records`
  - `expense_records`
  - `renewal_records`
  - `reminders`
- V1 不支持“仅删除车辆、保留历史记录”的分离模式

### 4.2 energy_records

表用途：统一记录加油与充电。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| vehicle_id | INTEGER | 是 | 外键 -> vehicles.id |
| occurred_at_epoch_millis | INTEGER | 是 | 发生时间 |
| energy_type | TEXT | 是 | `FUEL` / `ELECTRIC` |
| odometer_km | INTEGER | 是 | 记录时里程 |
| quantity_in_thousandth | INTEGER | 是 | 0.001 L 或 0.001 kWh |
| amount_in_cent | INTEGER | 是 | 本次总金额，单位分 |
| is_full | INTEGER | 是 | 0/1，表示加满或充满 |
| fuel_label | TEXT | 否 | 仅燃油记录使用，如 `92` / `95` / `98` / `0#` |
| charge_mode | TEXT | 否 | 仅充电记录使用，如 `HOME_AC` / `PUBLIC_AC` / `PUBLIC_DC` |
| station_name | TEXT | 否 | 加油站或充电站名称 |
| note | TEXT | 否 | 备注 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_energy_records_vehicle_id`
- `index_energy_records_vehicle_id_occurred_at`
- `index_energy_records_vehicle_id_odometer`
- `index_energy_records_vehicle_id_energy_type_occurred_at`

业务约束：

- `quantity_in_thousandth > 0`
- `amount_in_cent >= 0`
- `odometer_km >= 0`
- 燃油记录必须满足 `fuel_label != null`
- 充电记录必须满足 `charge_mode != null`
- 同一车辆录入时里程不能回退

说明：

- UI 可以支持用户输入“单价”或“总价”
- 入库时统一换算为 `amount_in_cent + quantity_in_thousandth`
- 单价不作为数据库权威字段，展示时可根据总价和数量反推

### 4.3 maintenance_records

表用途：保养与更换配件记录。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| vehicle_id | INTEGER | 是 | 外键 -> vehicles.id |
| occurred_at_epoch_millis | INTEGER | 是 | 保养时间 |
| odometer_km | INTEGER | 否 | 保养时里程 |
| maintenance_type | TEXT | 是 | 见枚举定义 |
| amount_in_cent | INTEGER | 是 | 金额，单位分 |
| store_name | TEXT | 否 | 服务门店 |
| note | TEXT | 否 | 备注 |
| next_due_date_epoch_day | INTEGER | 否 | 下次保养日期 |
| next_due_odometer_km | INTEGER | 否 | 下次保养里程 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_maintenance_records_vehicle_id`
- `index_maintenance_records_vehicle_id_occurred_at`
- `index_maintenance_records_vehicle_id_next_due_date`

业务约束：

- `amount_in_cent >= 0`
- `maintenance_type` 不能为空
- 若填写 `next_due_odometer_km`，必须大于 `odometer_km`

### 4.4 expense_records

表用途：非能源、非保养的其他车辆支出。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| vehicle_id | INTEGER | 是 | 外键 -> vehicles.id |
| occurred_at_epoch_millis | INTEGER | 是 | 发生时间 |
| category | TEXT | 是 | 见枚举定义 |
| amount_in_cent | INTEGER | 是 | 金额，单位分 |
| note | TEXT | 否 | 备注 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_expense_records_vehicle_id`
- `index_expense_records_vehicle_id_occurred_at`
- `index_expense_records_vehicle_id_category_occurred_at`

业务约束：

- `amount_in_cent >= 0`
- `category` 不能为空
- 充电费用不得放入此表，应走 `energy_records`

### 4.5 renewal_records

表用途：承载保险、年检、车船税等具有起止有效期的续期事项。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| vehicle_id | INTEGER | 是 | 外键 -> vehicles.id |
| type | TEXT | 是 | `INSURANCE` / `INSPECTION` / `TAX` / `OTHER` |
| provider_name | TEXT | 否 | 机构名称，如保险公司、检测站 |
| policy_number | TEXT | 否 | 保单号或单据编号 |
| amount_in_cent | INTEGER | 是 | 金额，单位分 |
| valid_from_epoch_day | INTEGER | 否 | 生效日期 |
| valid_until_epoch_day | INTEGER | 是 | 到期日期 |
| reminder_enabled | INTEGER | 是 | 0/1 |
| note | TEXT | 否 | 备注 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_renewal_records_vehicle_id`
- `index_renewal_records_vehicle_id_type`
- `index_renewal_records_vehicle_id_valid_until`

业务约束：

- `amount_in_cent >= 0`
- `valid_until_epoch_day` 必填
- 若填写 `valid_from_epoch_day`，则必须小于等于 `valid_until_epoch_day`
- 每条 `renewal_record` 可自动生成 0 或 1 条来源提醒

说明：

- 首版将保险、年检从普通费用中分离，避免费用与提醒双录入
- 统计时 `renewal_records` 仍然计入总支出

联动规则：

1. 新增 `renewal_record` 且 `reminder_enabled = true` 时，自动创建一条来源为 `RENEWAL_RECORD` 的提醒
2. 修改 `valid_until_epoch_day` 时，同步更新来源提醒的触发时间
3. 将 `reminder_enabled` 从 `true` 改为 `false` 时，不删除来源提醒，而是将其 `is_enabled` 置为 `false`
4. 删除 `renewal_record` 时，同步删除其来源提醒

### 4.6 reminders

表用途：统一承载保养、保险、年检、车船税、自定义提醒。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | INTEGER | 是 | 主键，自增 |
| vehicle_id | INTEGER | 是 | 外键 -> vehicles.id |
| type | TEXT | 是 | `MAINTENANCE` / `INSURANCE` / `INSPECTION` / `TAX` / `CUSTOM` |
| title | TEXT | 是 | 提醒标题 |
| remind_at_epoch_millis | INTEGER | 否 | 日期提醒时间点 |
| remind_odometer_km | INTEGER | 否 | 里程提醒值 |
| advance_days | INTEGER | 否 | 提前 N 天提醒，仅日期提醒使用 |
| is_enabled | INTEGER | 是 | 0/1 |
| status | TEXT | 是 | `PENDING` / `TRIGGERED` / `DONE` / `DISMISSED` |
| source_type | TEXT | 否 | 来源类型，如 `MAINTENANCE_RECORD` / `RENEWAL_RECORD` / `MANUAL` |
| source_id | INTEGER | 否 | 来源记录 ID |
| note | TEXT | 否 | 备注 |
| last_triggered_at_epoch_millis | INTEGER | 否 | 最近一次触发时间 |
| completed_at_epoch_millis | INTEGER | 否 | 处理完成时间 |
| dismissed_at_epoch_millis | INTEGER | 否 | 忽略时间 |
| created_at_epoch_millis | INTEGER | 是 | 创建时间 |
| updated_at_epoch_millis | INTEGER | 是 | 更新时间 |

索引建议：

- `index_reminders_vehicle_id`
- `index_reminders_is_enabled_remind_at`
- `index_reminders_vehicle_id_type`
- `index_reminders_status_remind_at`

业务约束：

- `title` 不能为空
- `remind_at_epoch_millis` 与 `remind_odometer_km` 至少填写一个
- `advance_days >= 0`
- `status` 必填
- `DONE` 状态必须有 `completed_at_epoch_millis`
- `DISMISSED` 状态必须有 `dismissed_at_epoch_millis`

联动规则：

- 来源为 `MAINTENANCE_RECORD`、`RENEWAL_RECORD` 的提醒，在来源记录被删除时同步删除
- `TRIGGERED` 状态的提醒允许用户一键标记为 `DONE` 或 `DISMISSED`

## 5. 枚举定义建议

### 5.1 车辆动力类型

```text
GASOLINE
DIESEL
HEV
PHEV
EV
```

### 5.2 能源类型

```text
FUEL
ELECTRIC
```

### 5.3 充电方式

```text
HOME_AC
PUBLIC_AC
PUBLIC_DC
OTHER
```

### 5.4 保养类型

```text
MINOR_SERVICE
MAJOR_SERVICE
ENGINE_OIL
OIL_FILTER
AIR_FILTER
CABIN_FILTER
TIRE
BRAKE_PAD
BATTERY
OTHER
```

### 5.5 其他费用分类

```text
REPAIR
PARKING
TOLL
CAR_WASH
TRAFFIC_FINE
ACCESSORY
OTHER
```

### 5.6 提醒类型

```text
MAINTENANCE
INSURANCE
INSPECTION
TAX
CUSTOM
```

### 5.7 续期事项类型

```text
INSURANCE
INSPECTION
TAX
OTHER
```

### 5.8 提醒来源类型

```text
MAINTENANCE_RECORD
RENEWAL_RECORD
MANUAL
```

### 5.9 提醒状态

```text
PENDING
TRIGGERED
DONE
DISMISSED
```

## 6. 统计与派生数据规则

以下数据不单独落表，统一实时查询或聚合：

- 首页本月总支出
- 本月能源支出
- 本月保养支出
- 本月其他支出
- 本月续期支出
- 最近一次燃油油耗
- 最近一次电耗
- 月度趋势
- 分类占比
- 单车成本对比

### 6.1 能耗计算

燃油油耗：

```text
两次满油记录之间：
百公里油耗 = 本次 quantity / (本次里程 - 上次满油里程) * 100
```

电耗：

```text
两次满电记录之间：
百公里电耗 = 本次 quantity / (本次里程 - 上次满电里程) * 100
```

插混：

- 油耗、电耗分别计算
- 总成本统一汇总
- 首版不做综合等效能耗

### 6.2 部分补能与有效区间

首版明确以下口径：

- `is_full = false` 的记录参与费用统计
- `is_full = false` 的记录不作为能耗区间终点
- 仅同一车辆、同一 `energy_type`、相邻两条 `is_full = true` 记录可以形成有效区间
- 插混车的燃油与电能分别按各自链路计算

### 6.3 历史编辑与重算规则

首版支持编辑和删除历史记录，但重算逻辑必须明确：

- 编辑 `energy_records` 的时间、里程、数量、金额、`is_full` 后，需重算该车辆该能源类型从该记录开始的后续所有有效区间
- 删除 `is_full = true` 记录后，原本依赖该记录的能耗区间全部失效并重新计算
- 编辑旧记录若导致后续里程冲突，禁止保存并提示用户

### 6.4 里程纠错策略

首版采用保守策略：

- 不支持复杂历史补录重排
- 不支持仅修正里程但不关联业务记录的 odometer-only 记录
- 如用户误录旧里程，只能通过编辑最新连续链路中的记录修正

### 6.5 总成本口径

总成本 = 能源支出 + 保养支出 + 其他费用支出 + 续期事项支出

说明：

- 保险和年检不再放入普通 `expense_records`
- 为避免重复统计，同一条续期事项不得再复制一条普通费用记录

## 7. Android 平台约束

### 7.1 通知权限

Android 13 及以上，新安装应用默认没有通知权限。  
首版提醒功能必须显式处理 `POST_NOTIFICATIONS` 权限申请时机。

产品结论：

- 未授权通知权限时，提醒记录仍然创建
- 但通知不会实际送达
- 首页和提醒页需提示用户开启系统通知权限

### 7.2 提醒精度

首版默认采用：

- `WorkManager` + 周期扫描即将到期提醒

不承诺：

- 分钟级精确提醒
- 秒级准时触发

原因：

- Android 14 对 exact alarm 权限更严格
- `WorkManager` 更适合本项目的非闹钟型提醒场景

## 8. Room 层建议

### 8.1 Entity 与 Domain 分离

Room Entity 使用数据库友好字段：

- 时间存 `Long`
- 日期存 `epochDay`
- 枚举存 `String`

Domain Model 使用业务友好类型：

- `Instant`
- `LocalDate`
- `enum class`
- `Money`

### 8.2 类型转换器

建议提供以下 Converter：

- `Instant <-> Long`
- `LocalDate <-> Long`
- `Enum <-> String`

## 9. DataStore 项

以下配置不入数据库，建议放 `DataStore`：

- 主题模式
- 货币单位
- 距离单位
- 油耗单位
- 默认提醒提前天数

注意：

- 默认车辆 ID 不放 DataStore

## 10. 导出与备份契约

### 10.1 CSV 导出

首版建议支持以下导出对象：

- 车辆
- 能源记录
- 保养记录
- 其他费用
- 续期事项
- 提醒

要求：

- 每类记录一张独立 CSV
- 第一行固定列头
- 时间统一导出为 ISO 8601 或本地格式化字符串
- 金额导出为元，保留两位小数

### 10.2 本地备份

首版建议采用：

- 单文件 JSON 备份
- 文件名格式：`roadmemo-backup-yyyyMMdd-HHmmss.json`

顶层结构建议：

```json
{
  "schemaVersion": 1,
  "exportedAt": "2026-04-21T10:00:00Z",
  "vehicles": [],
  "energyRecords": [],
  "maintenanceRecords": [],
  "expenseRecords": [],
  "renewalRecords": [],
  "reminders": []
}
```

恢复策略建议：

- 首版采用“全量导入为新库恢复”
- 不做增量 merge
- 恢复前明确提示会覆盖本地当前数据

## 11. 页面入口冻结建议

底部导航保持：

1. 首页
2. 记录
3. 统计
4. 设置

其中“记录”页一级入口冻结为：

1. 能源
2. 保养
3. 费用
4. 续期

说明：

- “续期”独立于普通费用展示
- 原因是续期事项同时承担费用记录与提醒来源两种职责
- 首版不再沿用“加油 / 保养 / 其他费用”三分结构

## 12. 后续扩展预留

V2 以后若做云同步，可为各表补充：

- `remote_id`
- `sync_status`
- `deleted_at_epoch_millis`

V1.2 若做附件，可增加独立附件表：

- `attachments`
  - `id`
  - `owner_type`
  - `owner_id`
  - `uri`
  - `mime_type`
  - `created_at_epoch_millis`

## 13. 商业与开源项目参考结论

本版修订重点参考了成熟产品的共同能力：

- Fuelio 强调时间线、费用分类、快速录入与提醒
- Drivvo 强调完整车辆成本、提醒、路线/收入扩展能力
- Simply Auto 强调电车支持、部分加油、CSV 导出、备份与多车管理
- MyExpenses 等开源项目强调离线优先、导入导出、数据安全与恢复能力

本项目当前 V0.1 的定位应明确为：

- 面向普通私家车主
- 支持多能源车型
- 核心是车辆成本记录、提醒、统计与本地备份

暂不承诺：

- 网约车收入核算
- 路线追踪
- GPS 自动里程
- 多人协作

## 14. 最终落地建议

建议按以下顺序实现：

1. `vehicles`
2. `energy_records`
3. `maintenance_records`
4. `expense_records`
5. `renewal_records`
6. `reminders`
7. 汇总查询与统计查询
8. WorkManager 通知同步

当前这版已经接近可冻结状态。  
在开始搭建项目之前，仍建议最终确认以下 6 个产品决策：

1. 保险、年检、车船税是否全部进入 `renewal_records`
2. V1 是否明确不支持历史补录重排
3. V1 是否明确不支持 odometer-only 记录
4. 提醒是否接受近似时间触发
5. 备份恢复是否采用覆盖式恢复
6. V1 目标用户是否收敛为普通私家车主
