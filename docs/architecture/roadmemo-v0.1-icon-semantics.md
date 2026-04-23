# RoadMemo 图标语义规范 v0.1

## 1. 目标

RoadMemo 的图标不应只是“看起来差不多”，而应承担稳定的产品语义。

本规范目标：

- 限制图标选择的随意性
- 保持主导航、空状态、操作按钮的一致表达
- 避免不同页面对同一概念使用不同图标

---

## 2. 当前统一入口

统一入口文件：

- `app/src/main/java/com/roadmemo/app/ui/theme/RoadMemoIcons.kt`

要求：

- 页面内优先使用 `RoadMemoIcons`
- 不再默认直接散落使用 `Icons.Outlined.*`
- 如需新增图标语义，先补到 `RoadMemoIcons`，再在页面中接入

---

## 3. 当前语义映射

### 主导航

- `Home` -> 首页
- `Records` -> 记录
- `Statistics` -> 统计
- `Settings` -> 设置

### 通用操作

- `Back` -> 返回
- `Add` -> 新增
- `Calendar` -> 日期选择 / 时间范围

### 业务对象

- `Vehicle` -> 车辆 / 车相关空状态 / 默认车辆语义
- `Reminder` -> 提醒 / 通知 / 待处理事项

### 分析与状态

- `Trend` -> 趋势变化
- `Analytics` -> 汇总 / 分析 / 统计指标
- `Timeline` -> 持续记录 / 时间推进 / 记账过程

---

## 4. 使用规则

### 4.1 主导航图标

- 必须稳定，不应频繁更换
- 同一模块的隐藏业务页，可沿用该模块主图标语义

### 4.2 空状态图标

- 只做弱提示，不承担主信息
- 应优先使用业务对象图标：
  - 车辆空状态 -> `Vehicle`
  - 提醒空状态 -> `Reminder`
  - 趋势空状态 -> `Trend`
  - 分类汇总空状态 -> `Analytics`

### 4.3 按钮图标

- 图标只作为辅助，不应替代文案
- 新增动作统一优先使用 `Add`
- 日期或时间范围切换统一优先使用 `Calendar`

### 4.4 禁止项

- 同一语义在不同页面使用不同图标
- 为了“看起来更丰富”随意更换 icon
- 空状态使用过于情绪化或装饰性的图标

---

## 5. 当前已落地页面

- `RoadMemoDestination`
- `RoadMemoTopBar`
- `RoadMemoDateField`
- `HomeScreen`
- `RecordsScreen`
- `StatisticsScreen`
- `ReminderScreen`
- `VehicleScreen`

---

## 6. 后续扩展原则

新增图标语义时，应先回答：

1. 这是新语义，还是已有语义的重复表达？
2. 是否会影响主导航、空状态或操作按钮的一致性？
3. 是否真的需要新增，还是应复用已有图标？

只有在现有语义无法覆盖时，才新增到 `RoadMemoIcons`。
