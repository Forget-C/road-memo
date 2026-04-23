# RoadMemo 项目协作规范

## 1. 目的

本文件是 `RoadMemo / 长路有记` 的项目级执行规范。

目标：

- 为后续所有代理 / 开发者提供单一事实源
- 约束项目结构、入口、模块职责、UI 风格和实现边界
- 避免新增功能时偏离已冻结的产品与设计结论

---

## 2. 适用范围

本文件适用于：

- 代码实现
- 结构调整
- UI 修改
- 文档维护
- 架构演进

若与临时讨论结论冲突，以：

1. 已冻结文档
2. 本文件
3. 当前任务说明

的优先级处理。

---

## 3. 项目定位

RoadMemo 是一个：

- 面向普通私家车主与家庭多车用户
- 支持燃油车、纯电车、油混、插混
- 本地优先
- 记录能源 / 保养 / 费用 / 续期事项
- 提供提醒、统计、导出与备份恢复

的 Android 车辆成本记录工具。

当前阶段：

- `V0.1 / 0.1.0`
- 重点是 MVP 可用性、结构一致性和 UI 系统化

---

## 4. 冻结产品边界

当前冻结结论：

- 支持车型：
  - `GASOLINE`
  - `DIESEL`
  - `HEV`
  - `PHEV`
  - `EV`
- 记录类型固定为：
  - 能源
  - 保养
  - 费用
  - 续期
- 续期类事项统一进入 `renewal_records`
  - 保险
  - 年检
  - 车船税
- 首版不做：
  - 登录
  - 云同步
  - OCR
  - GPS 自动里程
  - 收入 / 路线 / 网约车经营模型
- 首版提醒为近似提醒，不承诺分钟级精确触发
- 首版恢复策略为覆盖式恢复
- 删除车辆采用级联硬删除
- 插混首版仅做油、电分开统计，不做综合等效能耗

详情以 `docs/architecture/roadmemo-v0.1-freeze-checklist.md` 为准。

---

## 5. 项目结构

代码主目录：

```text
app/src/main/java/com/roadmemo/app/
├── data/
│   ├── export/
│   ├── local/
│   │   ├── dao/
│   │   ├── database/
│   │   ├── entity/
│   │   └── mapper/
│   ├── preferences/
│   └── repository/
├── di/
├── domain/
│   ├── model/
│   └── repository/
├── navigation/
├── ui/
│   ├── components/
│   ├── screens/
│   │   ├── home/
│   │   ├── records/
│   │   ├── statistics/
│   │   ├── settings/
│   │   ├── vehicle/
│   │   ├── reminder/
│   │   ├── energy/
│   │   ├── maintenance/
│   │   ├── expense/
│   │   └── renewal/
│   ├── theme/
│   └── util/
└── ui/RoadMemoApp.kt
```

结构约束：

- `domain` 不直接依赖 Room Entity
- `ui/screens/*` 每个目录只承载该页面相关 UI 与 ViewModel
- 通用组件统一放 `ui/components`
- 颜色、字形、圆角、间距统一放 `ui/theme`
- 导航统一从 `navigation` 维护，不允许页面内部自定义私有路由协议

---

## 6. 入口与路由规范

应用根入口：

- `ui/RoadMemoApp.kt`

导航入口：

- `navigation/RoadMemoNavHost.kt`
- `navigation/RoadMemoDestination.kt`

当前主导航固定为：

- `home`
- `records`
- `statistics`
- `settings`

当前隐藏业务页固定为：

- `vehicles`
- `reminders`
- `energy/add`
- `energy/edit/{recordId}`
- `maintenance/add`
- `maintenance/edit/{recordId}`
- `expense/add`
- `expense/edit/{recordId}`
- `renewal/add`
- `renewal/edit/{recordId}`

路由规范：

- 新增页面必须先在 `RoadMemoDestination` 中定义
- 页面跳转统一通过 `RoadMemoNavHost` 编排
- 不允许页面内部直接拼接未登记的匿名路由

---

## 7. 模块职责

### 7.1 data

职责：

- Room
- DataStore
- 导出 / 备份恢复
- Repository 实现

要求：

- 持久化细节不泄漏到 `ui`
- 金额字段统一以整数保存，不使用 `Double` 做金额存储

### 7.2 domain

职责：

- 业务模型
- 仓储接口
- 纯业务语义

要求：

- 不暴露 Room Entity
- 保持对 UI 和存储方案解耦

### 7.3 navigation

职责：

- 页面路由定义
- 页面装配
- 页面间参数传递

### 7.4 ui/components

职责：

- 通用组件
- 设计系统实现
- 页面骨架

要求：

- 任何可复用的视觉模式优先沉淀到这里
- 禁止多个页面长期复制相同结构后各自演化

### 7.5 ui/screens

职责：

- 页面级布局
- 页面状态消费
- 页面内交互逻辑

要求：

- 页面负责组合，不负责重复造轮子

### 7.6 ui/theme

职责：

- 主题
- 色板
- 字体
- 圆角
- 间距 token
- 图标语义入口

要求：

- 所有新视觉常量优先进入 token / theme 层
- 页面不允许随意散落新的视觉常量
- 图标语义统一收口到 `RoadMemoIcons`

---

## 8. 当前 Design System 约束

当前已沉淀的核心组件包括但不限于：

- `RoadMemoPrimaryButton`
- `RoadMemoSecondaryButton`
- `RoadMemoTextField`
- `RoadMemoDateField`
- `RoadMemoStatusBadge`
- `RoadMemoConfirmDialog`
- `RoadMemoFeedbackMessage`
- `RoadMemoSnackbarHost`
- `RoadMemoSkeletonBlock`
- `RoadMemoSkeletonCard`
- `RoadMemoHeroSurface`
- `RoadMemoMetricCard`
- `RoadMemoMiniInfoCard`
- `RoadMemoRecordCard`
- `RoadMemoInfoGroupCard`
- `RoadMemoEmptyStateCard`
- `RoadMemoEmptyListState`
- `RoadMemoTopBar`
- `RoadMemoPageScaffold`
- `RoadMemoScreenHeader`
- `RoadMemoListSectionHeader`
- `RoadMemoIcons`

使用规则：

- 新页面优先复用现有组件
- 如果发现某类结构在两个以上页面重复出现，应优先抽组件
- 不允许为了局部页面效果绕过现有组件系统长期堆特例
- 高风险确认操作统一使用 `RoadMemoConfirmDialog`
  - 例如：删除确认、恢复备份确认
- 涉及异步动作的按钮优先使用统一忙碌态
  - 例如：保存中、导出中、备份中、恢复中
- 通用结果反馈统一使用 `RoadMemoFeedbackMessage`
  - 例如：保存失败、导出成功、恢复失败
- 一次性轻提示统一使用 `RoadMemoSnackbarHost`
  - 例如：已调起系统分享、已忽略提醒、已标记完成
- 空状态统一使用轻图标头的 `RoadMemoEmptyStateCard / RoadMemoEmptyListState`
  - 避免回退到纯文本“暂无数据”样式
- 加载中状态优先使用 `RoadMemoSkeletonBlock / RoadMemoSkeletonCard`
  - 避免首屏加载期误显示“暂无数据”
- 图标语义统一从 `RoadMemoIcons` 获取
  - 主导航、空状态、按钮图标禁止长期散落直取 `Icons.*`

---

## 9. UI 风格冻结结论

RoadMemo 当前 UI 风格冻结为：

- 现代准扁平
- 商业工具化
- 轻品牌感
- 浅色主题优先
- 本地账本 / 数据工具气质

不是：

- 传统车机仪表盘
- 拟物
- 重阴影
- 重渐变
- 高饱和彩色拼贴

### 9.1 核心视觉关键词

- clean
- flat
- light
- whitespace
- premium commercial mobile app
- minimal shadow
- calm teal accent

### 9.2 色彩规则

- 主色：teal
- 中性色：白 / 灰 / 深灰
- 警示色：橙
- 图表色：限制在少量辅助色

禁止：

- 紫色主导
- 大面积高饱和撞色
- 默认深色沉重背景

### 9.3 层级规则

允许：

- 轻渐变 hero 区
- 极轻容器区分
- 极轻状态动效

禁止：

- 重拟物
- 重阴影
- 复杂异形卡片成为主流
- 全站铺满渐变

### 9.4 页面结构规则

主导航页：

- 使用 `RoadMemoScreenHeader`
- 首页 / 记录 / 统计 保持统一开场结构

隐藏业务页：

- 使用 `RoadMemoPageScaffold`
- 使用统一 top bar 与返回逻辑

列表页：

- 优先使用 `RoadMemoListSectionHeader`
- 空状态优先使用 `RoadMemoEmptyListState`

表单页：

- 优先使用 `RoadMemoTextField`
- 日期优先使用 `RoadMemoDateField`
- 错误反馈优先显示在字段级

---

## 10. 数据与交互规范

### 10.1 金额与数量

- 金额统一用 `amountInCent`
- 能源数量统一用 `quantityInThousandth`
- UI 层展示时再格式化

### 10.2 表单交互

- 数字字段优先使用数字 / 小数键盘
- 日期字段优先使用 `RoadMemoDateField`
- 错误提示优先字段级，不重复在底部大面积堆相同错误

### 10.3 提醒交互

- 续期事项与提醒来源自动联动
- 提醒页支持：
  - 忽略
  - 已处理
  - 跳回来源

---

## 11. 代码实现规则

### 11.1 新增代码前

- 先判断是否已有现成组件 / token / 页面骨架可复用
- 先判断是否会破坏已冻结的数据模型和交互边界

### 11.2 修改代码时

- 优先小步收口，避免页面各自分叉
- 能进入通用组件层的，不长期留在页面内私有实现
- 新增视觉常量优先进入 theme / token

### 11.3 测试与验证

- UI / 结构调整后至少执行：
  - `:app:assembleDebug`
- 涉及状态逻辑调整时，优先补 ViewModel / mapper / repository 测试

---

## 12. 文档同步规则

这是强制要求。

当出现以下任一变化时，必须同步更新相关文档，并在本次任务反馈中明确说明：

1. 项目结构变化
2. 路由或入口变化
3. 数据模型变化
4. 页面职责变化
5. 设计系统组件新增 / 废弃 / 重命名
6. UI 风格规范变化
7. 交互规则变化
8. 冻结边界变化

最低执行要求：

- 代码变了
- 规范变了
- 文档必须同步变
- 并且在任务结果里明确反馈“已更新哪些文档 / 文件”

禁止：

- 先改实现，长期不补文档
- 口头说“以后再补”
- 已经偏离冻结规范但不更新文件

---

## 13. 优先参考文档

后续工作如需判断产品 / 架构 / UI 边界，优先参考：

- `README.md`
- `docs/architecture/roadmemo-v0.1-freeze-checklist.md`
- `docs/architecture/roadmemo-v0.1-data-model.md`
- `docs/architecture/roadmemo-v0.1-interaction-spec.md`
- `docs/architecture/roadmemo-v0.1-visual-baseline.md`
- `docs/architecture/roadmemo-v0.1-ui-optimization-plan.md`
- `docs/architecture/roadmemo-v0.1-design-system-v1.md`
- `docs/architecture/roadmemo-v0.1-flat-design-guideline.md`

---

## 14. 最终原则

RoadMemo 的目标不是“继续堆功能”，而是：

- 在冻结边界内持续打磨
- 保持结构稳定
- 保持体验一致
- 让产品越来越像一个可信、专业、现代的车辆成本管理工具

任何新增实现都应服务于这个目标。
