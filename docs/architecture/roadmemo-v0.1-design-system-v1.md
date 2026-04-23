# RoadMemo Design System v1

## 1. 目标

RoadMemo 当前已经具备基础视觉方向，但 UI 仍然偏「页面手写拼装」。
V1 设计系统的目标不是引入一套外部视觉库，而是基于 `Material 3` 底座，
沉淀一套属于 RoadMemo 自己的扁平化组件层。

核心原则：

- 保留 `Material 3` 的交互成熟度和平台适配能力
- RoadMemo 自己定义视觉 token、组件外观和页面结构
- 让首页、记录页、统计页、表单页共享同一套组件语言

---

## 2. 当前技术判断

当前 UI 形态：

- 底层组件：`MaterialTheme`、`Card`、`FilledTonalButton`、
  `OutlinedTextField`
- 项目内包装：`RoadMemoSection`、`RoadMemoHeroSurface`、
  `RoadMemoVehicleSummaryCard`
- 主题基础：`Color`、`Type`、`Shape`、`RoadMemoTokens`

结论：

- 当前不是纯手写，也不是现成商业 UI 库
- 目前是 `Material 3 + 自定义页面样式`
- 下一步应升级为 `Material 3 + RoadMemo Design System`

---

## 3. 推荐技术路线

### 3.1 保留官方底座

继续使用：

- Jetpack Compose
- Material 3

原因：

- 稳定
- 官方维护
- 无障碍和交互细节成熟
- Android 适配风险最低

### 3.2 不做整站式第三方 UI 替换

当前不建议：

- 为了追求“更高级”整体切换第三方视觉库
- 大量依赖一个非官方设计系统去主导产品风格

原因：

- Android Compose 生态里缺少真正成熟、通用、强商业风格的
  扁平化 UI 视觉库
- 切换成本高
- 风格最终仍需自己收口

### 3.3 可关注但不急着全量引入的方向

可关注：

- Compose Unstyled

适合未来使用场景：

- Popover / Menu / Bottom Sheet 等复杂交互
- 需要摆脱 Material 默认外观时

但 V0.1 阶段不建议全量切换。

---

## 4. 设计系统分层

```text
Theme Token Layer
├── Color
├── Typography
├── Iconography
├── Radius
├── Spacing
└── Elevation / State（后续）

Primitive Component Layer
├── Button
├── TextField
├── Dialog
├── Badge
├── Feedback
├── Snackbar
├── Surface
└── Dialog Action

Composite Component Layer
├── Hero Card
├── Metric Card
├── Record Card
├── Skeleton
├── Summary Card
├── Empty State
└── Vehicle Summary Card

Screen Pattern Layer
├── Home Hero
├── Statistics Hero
├── Record List
├── Form Section
└── Settings Group
```

---

## 5. V1 组件清单

### 5.1 Token 层

已具备：

- `RoadMemoTokens`
- `RoadMemoShapes`
- `RoadMemoTypography`
- `RoadMemoIcons`

V1 继续补齐：

- 状态色 token
- 按钮高度 token
- 输入框间距 token
- 图标语义与页面映射规则

### 5.2 Primitive 组件

优先级最高：

1. `RoadMemoPrimaryButton`
2. `RoadMemoSecondaryButton`
3. `RoadMemoTextField`
4. `RoadMemoStatusBadge`
5. `RoadMemoConfirmDialog`
6. `RoadMemoFeedbackMessage`
7. `RoadMemoSnackbarHost`

价值：

- 统一按钮层级
- 统一表单输入外观
- 统一列表标签和状态表达
- 统一删除确认、恢复确认等高风险动作弹窗
- 统一保存失败、导出结果、恢复结果等反馈展示
- 统一一次性轻提示反馈，避免页面各自手写 Snackbar / Toast

### 5.3 Composite 组件

优先级最高：

1. `RoadMemoRecordCard`
2. `RoadMemoEmptyStateCard`
3. `RoadMemoInfoGroupCard`
4. `RoadMemoSkeletonCard`

价值：

- 记录页不再手写每条流水
- 空状态不再每页自己拼
- 设置页、提醒页、统计页可共享信息卡
- 空状态统一使用轻图标头，增强页面完成度但不引入重装饰
- 首页、统计页、记录页在真实数据返回前优先展示骨架屏

---

## 6. 页面映射策略

### 首页

继续使用：

- `RoadMemoHeroSurface`
- `RoadMemoMetricCard`

后续替换：

- 快捷操作按钮 -> `RoadMemoSecondaryButton`
- 提醒摘要标签 -> `RoadMemoStatusBadge`

### 记录页

优先改造：

- 列表项 -> `RoadMemoRecordCard`
- 顶部动作 -> `RoadMemoPrimaryButton / SecondaryButton`

### 表单页

优先改造：

- `OutlinedTextField` -> `RoadMemoTextField`
- 保存区按钮 -> `RoadMemoPrimaryButton`

### 提醒页 / 设置页

优先改造：

- 状态标签 -> `RoadMemoStatusBadge`
- 说明卡 -> `RoadMemoInfoGroupCard`

---

## 7. 视觉规范摘要

### 按钮

- 主按钮：强主色、用于保存/创建/确认主动作
- 次按钮：浅色容器、用于编辑/查看/次级入口
- 危险按钮：保留文本或警示色，不做高饱和大面积铺色
- 异步动作统一使用忙碌态按钮，不在页面层手写“保存中...”文案切换

### 输入框

- 使用统一圆角
- 强调扁平、轻边框、留白
- 错误态用清晰文本反馈，不依赖重边框和重阴影

### 徽标 / 标签

- 标签小而克制
- 只承载状态或类型，不承担主信息
- 颜色不超过三类：
  - 主色
  - 中性灰
  - 警示橙

### 反馈提示

- 页面级反馈统一使用反馈卡，不直接裸放文本
- 成功、信息、错误三类反馈使用固定 tone
- 表单字段错误优先贴近字段，非字段级错误再落到反馈卡
- 一次性轻提示优先使用 Snackbar Host，不直接散落 Toast

### 记录卡

- 主信息：标题 + 金额
- 辅信息：时间 / 补充说明
- 类型标签退到辅助层
- 操作按钮固定在底部右侧

### 空状态

- 统一使用轻图标头 + 标题 + 说明三段式结构
- 图标只做弱提示，不抢主信息
- 禁止退回到单行“暂无数据”裸文本

### 加载状态

- 首屏加载优先展示 skeleton，而不是直接显示空状态
- skeleton 使用弱对比平面块，不引入 shimmer 或重动画
- 首页、统计页、记录页优先使用统一骨架卡和骨架块

---

## 8. 实施顺序

### 第一阶段

- `RoadMemoPrimaryButton`
- `RoadMemoSecondaryButton`
- `RoadMemoTextField`
- `RoadMemoStatusBadge`

### 第二阶段

- `RoadMemoRecordCard`
- `RoadMemoEmptyStateCard`

### 第三阶段

- 首页 / 记录页 / 表单页全面接入
- 提醒页 / 设置页统一状态与信息卡

---

## 9. 阶段性结论

RoadMemo 接下来最应该做的不是“换一个库”，而是：

**用官方底座，沉淀自己的品牌组件系统。**

这条路线最适合当前项目：

- 风格可控
- 方便基于准扁平规范持续扩展
- 技术风险低
- 可以持续迭代
- 最终能形成自己的产品辨识度

---

## 10. 关联规范

设计系统默认受以下文档约束：

- `roadmemo-v0.1-visual-baseline.md`
- `roadmemo-v0.1-ui-optimization-plan.md`
- `roadmemo-v0.1-flat-design-guideline.md`

其中：

- 视觉基线定义整体气质和配色边界
- UI 优化方案定义页面级方向
- Flat Design Guideline 定义扁平化与准扁平的应用边界
