# RoadMemo V0.1 能耗统计功能方案

## 1. 目标

为当前 `RoadMemo` 增加：

- 油耗统计
- 电耗统计
- 插混油 / 电分开统计

并保持：

- 不修改现有数据库表结构
- 不引入高风险 migration
- 不打破当前 `V0.1` 已冻结的产品边界

一句话定义：

> 基于现有 `energy_records` 动态计算最近能耗、平均能耗和趋势，不做综合等效能耗。

---

## 2. 当前基础条件

当前已有的关键数据已经足够支撑首版能耗统计：

- `EnergyRecord.odometerKm`
- `EnergyDetail.quantityInThousandth`
- `EnergyDetail.isFull`
- `EnergyDetail.energyType`
- `Vehicle.powertrainType`

因此：

- 油耗可以按 `FUEL + isFull`
- 电耗可以按 `ELECTRIC + isFull`
- 插混可以天然分成油、电两套口径

当前缺口主要在：

- 统一的能耗计算器
- 首页与统计页的展示聚合
- 对应单元测试

---

## 3. 统计口径

### 3.1 油耗

适用对象：

- `GASOLINE`
- `DIESEL`
- `HEV`
- `PHEV` 中的燃油记录

计算规则：

```text
油耗 (L/100km) = 本次加油量(L) / (本次里程 - 上次满油里程) * 100
```

仅在以下条件成立时生成有效区间：

- 同一车辆
- `energyType = FUEL`
- 上一次记录 `isFull = true`
- 本次记录 `isFull = true`
- `endOdometer > startOdometer`

### 3.2 电耗

适用对象：

- `EV`
- `PHEV` 中的充电记录

计算规则：

```text
电耗 (kWh/100km) = 本次充电量(kWh) / (本次里程 - 上次满电里程) * 100
```

仅在以下条件成立时生成有效区间：

- 同一车辆
- `energyType = ELECTRIC`
- 上一次记录 `isFull = true`
- 本次记录 `isFull = true`
- `endOdometer > startOdometer`

### 3.3 插混

`PHEV` 首版明确采用：

- 油耗单独计算
- 电耗单独计算
- 首页与统计页分别显示

首版不做：

- 综合等效能耗
- 油电折算统一能耗
- 续航推算

---

## 4. 边界规则

### 4.1 非满补记录

`isFull = false` 的记录：

- 参与成本统计
- 不作为有效能耗区间终点

### 4.2 里程异常

若：

- `endOdometer <= startOdometer`

则该区间视为无效，不输出能耗结果。

### 4.3 数据不足

若某种能源类型下：

- 只有一条 `isFull = true` 记录
- 或无法构成完整闭环

则展示：

- `暂无有效油耗`
- `暂无有效电耗`

不展示：

- `0.0`
- `0 kWh/100km`

### 4.4 编辑 / 删除后的口径

首版不存储能耗区间，只动态计算，因此：

- 编辑记录后自动按新数据重算
- 删除记录后自动按当前剩余记录重算

这也是首版不改库的最大收益。

---

## 5. Domain 模型建议

建议新增以下模型。

### 5.1 ConsumptionSegment

```kotlin
data class ConsumptionSegment(
    val vehicleId: Long,
    val energyType: EnergyType,
    val startRecordId: Long,
    val endRecordId: Long,
    val startOdometerKm: Int,
    val endOdometerKm: Int,
    val distanceKm: Int,
    val quantityInThousandth: Long,
    val valuePer100: Double,
    val occurredAt: Instant
)
```

说明：

- `valuePer100`
  - `FUEL` 时表示 `L/100km`
  - `ELECTRIC` 时表示 `kWh/100km`

### 5.2 ConsumptionMetric

```kotlin
data class ConsumptionMetric(
    val latestText: String?,
    val averageText: String?,
    val segmentCount: Int
)
```

### 5.3 ConsumptionSummary

```kotlin
data class ConsumptionSummary(
    val fuel: ConsumptionMetric?,
    val electric: ConsumptionMetric?,
    val fuelSegments: List<ConsumptionSegment>,
    val electricSegments: List<ConsumptionSegment>
)
```

说明：

- 普通燃油车：通常只有 `fuel`
- 纯电车：通常只有 `electric`
- 插混：两者都可能有

---

## 6. 计算器设计

建议新增：

- `EnergyConsumptionCalculator`

建议位置：

- `app/src/main/java/com/roadmemo/app/domain/`

职责：

- 输入：某车辆全部能源记录
- 输出：`ConsumptionSummary`

### 6.1 输入要求

- 记录按 `occurredAt` 升序
- 同时支持：
  - `FUEL`
  - `ELECTRIC`

### 6.2 核心流程

#### Step 1

按 `energyType` 分组：

- `fuelRecords`
- `electricRecords`

#### Step 2

对每一组分别执行：

- 过滤并遍历 `isFull = true` 的链路
- 两两构成区间
- 检查里程有效性
- 生成 `ConsumptionSegment`

#### Step 3

从 `segment list` 生成：

- 最近值
- 平均值
- 格式化文本

---

## 7. 伪代码

```kotlin
fun calculate(records: List<EnergyRecord>): ConsumptionSummary {
    val fuelSegments = buildSegments(records, EnergyType.FUEL)
    val electricSegments = buildSegments(records, EnergyType.ELECTRIC)

    return ConsumptionSummary(
        fuel = fuelSegments.toMetric("L/100km"),
        electric = electricSegments.toMetric("kWh/100km"),
        fuelSegments = fuelSegments,
        electricSegments = electricSegments
    )
}

private fun buildSegments(
    records: List<EnergyRecord>,
    energyType: EnergyType
): List<ConsumptionSegment> {
    val candidates = records
        .filter { it.detail.energyType == energyType && it.detail.isFull }
        .sortedBy { it.occurredAt }

    val result = mutableListOf<ConsumptionSegment>()
    for (index in 1 until candidates.size) {
        val previous = candidates[index - 1]
        val current = candidates[index]
        val distance = current.odometerKm - previous.odometerKm
        if (distance <= 0) continue

        val quantity = current.detail.quantityInThousandth / 1000.0
        val per100 = quantity / distance * 100

        result += ConsumptionSegment(
            vehicleId = current.vehicleId,
            energyType = energyType,
            startRecordId = previous.id,
            endRecordId = current.id,
            startOdometerKm = previous.odometerKm,
            endOdometerKm = current.odometerKm,
            distanceKm = distance,
            quantityInThousandth = current.detail.quantityInThousandth,
            valuePer100 = per100,
            occurredAt = current.occurredAt
        )
    }
    return result
}
```

---

## 8. Repository 接入建议

首版不建议改 `EnergyRepository` 接口。

原因：

- 现有 `observeRecords(vehicleId)` 已足够
- 计算器可在 ViewModel 聚合层或单独 use-case 层完成
- 这样不需要动 DAO，不需要 migration

推荐做法：

- `ViewModel` 获取 `List<EnergyRecord>`
- 交给 `EnergyConsumptionCalculator`

如后续要复用更广，可再抽为：

- `GetConsumptionSummaryUseCase`

---

## 9. UI 展示方案

### 9.1 首页

建议只展示“最近值”，不要一次塞太多。

#### 燃油车 / 油混

- 最近油耗：`6.8 L/100km`

#### 纯电

- 最近电耗：`14.2 kWh/100km`

#### 插混

- 最近油耗：`5.3 L/100km`
- 最近电耗：`15.1 kWh/100km`

建议位置：

- 放在首页 `本月构成` 之前
- 用 1~2 张 `MiniInfoCard` 展示

### 9.2 统计页

建议新增一块：

#### 能耗表现

- 最近油耗
- 平均油耗
- 最近电耗
- 平均电耗

再新增一块：

#### 能耗趋势

- 先用趋势列表或 mini card
- 首版不强制接真实折线图

---

## 10. ViewModel 接入点

### 10.1 StatisticsViewModel

最优先接入。

当前已经聚合：

- `energy`
- `maintenance`
- `expense`
- `renewal`

只需要在：

- `Vehicle.toStatisticsUiState(...)`

里接入：

- `consumptionSummary = calculator.calculate(energy)`

然后新增几个 UI 字段，例如：

```kotlin
val latestFuelConsumptionText: String? = null
val averageFuelConsumptionText: String? = null
val latestElectricConsumptionText: String? = null
val averageElectricConsumptionText: String? = null
```

### 10.2 HomeViewModel

第二优先级接入。

只需要展示：

- 最近油耗
- 最近电耗

不需要平均值。

---

## 11. 测试方案

建议新增：

- `EnergyConsumptionCalculatorTest`

必须覆盖的场景：

### 11.1 油耗基础链路

- 两条 `FUEL + isFull = true`
- 正确输出 `L/100km`

### 11.2 电耗基础链路

- 两条 `ELECTRIC + isFull = true`
- 正确输出 `kWh/100km`

### 11.3 非满补记录

- 中间存在 `isFull = false`
- 不参与有效区间终点

### 11.4 里程倒退

- `endOdometer <= startOdometer`
- 不输出区间

### 11.5 插混双链路

- 同时存在 `FUEL` 和 `ELECTRIC`
- 两套结果分别生成

### 11.6 数据不足

- 仅一条满补记录
- `latestText / averageText = null`

---

## 12. 实施顺序建议

### 第 1 步

新增 domain 模型：

- `ConsumptionSegment`
- `ConsumptionMetric`
- `ConsumptionSummary`

### 第 2 步

新增：

- `EnergyConsumptionCalculator`

并补单元测试。

### 第 3 步

接 `StatisticsViewModel`：

- 最近油耗 / 平均油耗
- 最近电耗 / 平均电耗

### 第 4 步

接 `HomeViewModel`：

- 最近油耗 / 最近电耗

### 第 5 步

视效果决定是否补趋势图。

---

## 13. 当前建议结论

对 `RoadMemo` 当前阶段来说，最稳妥的落地方式是：

- 不改数据库
- 不做预计算表
- 不做综合等效能耗
- 基于现有能源记录动态计算
- 先接统计页，再接首页

一句话总结：

> 先把“最近能耗 + 平均能耗 + 有效区间趋势”做出来，油电分开算，插混继续双账本，这就是当前版本最合适的电耗 / 油耗统计方案。
