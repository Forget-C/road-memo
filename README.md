# RoadMemo · 长路有记

一款面向车主的 Android 本地记账 App，用来记录车辆能源、保养、
费用和续期事项，并提供首页摘要、提醒和基础统计。

当前仓库对应 `V0.1 / 0.1.0` MVP 阶段，重点验证：

- 多能源车型支持：汽油 / 柴油 / 油混 / 插混 / 纯电
- 本地优先：不依赖登录，不依赖云端
- 快速记录：能源 / 保养 / 费用 / 续期四类主链路
- 数据可迁移：CSV 导出、JSON 备份与恢复

## 当前能力

当前已经落地的核心功能：

- 车辆管理
  - 新增车辆
  - 默认车辆切换
  - 车辆动力类型管理
- 能源记录
  - 新增 / 编辑 / 删除
  - 支持加油和充电
- 保养记录
  - 新增 / 编辑 / 删除
  - 支持下次保养日期 / 里程
- 费用记录
  - 新增 / 编辑 / 删除
- 续期事项
  - 新增 / 编辑 / 删除
  - 自动生成提醒来源
- 提醒系统
  - 查看提醒
  - 标记已处理
  - 忽略
  - 跳回来源记录
- 首页与统计
  - 本月摘要
  - 最近记录
  - 分类汇总
  - 最近 6 个月趋势
- 数据能力
  - CSV 导出
  - JSON 完整备份
  - 备份恢复覆盖

## 技术栈

- Kotlin
- Jetpack Compose
- Navigation Compose
- Room
- Hilt
- DataStore
- WorkManager
- Coroutines + Flow

## 运行环境

- Android `minSdk 26`
- `compileSdk 36`
- `targetSdk 36`
- Java 17

版本信息见：

- [app/build.gradle.kts](/Users/extreme/Projects/github.com/road-memo/app/build.gradle.kts)

## 本地构建

在项目根目录执行：

```bash
GRADLE_USER_HOME=$PWD/.gradle-home ./gradlew --no-configuration-cache :app:assembleDebug
```

当前调试包输出位置：

- [app-debug.apk](/Users/extreme/Projects/github.com/road-memo/app/build/outputs/apk/debug/app-debug.apk)

## 项目结构

```text
app/src/main/java/com/roadmemo/app/
├── data/           # Room、导出备份、仓储实现
├── di/             # Hilt 注入
├── domain/         # 领域模型与仓储接口
├── navigation/     # 路由与导航
└── ui/
    ├── components/ # 通用 Compose 组件
    ├── screens/    # 页面
    └── theme/      # 主题与配色
```

## 主要页面

- 首页
- 记录页
- 统计页
- 设置页
- 车辆管理页
- 提醒页
- 能源 / 保养 / 费用 / 续期四类表单页

应用主入口：

- [RoadMemoApp.kt](/Users/extreme/Projects/github.com/road-memo/app/src/main/java/com/roadmemo/app/ui/RoadMemoApp.kt)

## 产品与设计文档

设计和架构文档位于：

- [roadmemo-v0.1-data-model.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-data-model.md)
- [RoadMemoDomainModelDraft.kt](/Users/extreme/Projects/github.com/road-memo/docs/architecture/RoadMemoDomainModelDraft.kt)
- [roadmemo-v0.1-interaction-spec.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-interaction-spec.md)
- [roadmemo-v0.1-visual-baseline.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-visual-baseline.md)
- [roadmemo-v0.1-visual-reference-map.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-visual-reference-map.md)
- [roadmemo-v0.1-freeze-checklist.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-freeze-checklist.md)
- [roadmemo-v0.1-cross-review-report.md](/Users/extreme/Projects/github.com/road-memo/docs/architecture/roadmemo-v0.1-cross-review-report.md)

## 当前已知边界

当前仍属于 MVP 阶段，以下能力尚未落地：

- 登录与云同步
- 图片附件
- OCR 票据识别
- 多设备同步
- 收入 / 路线 / 网约车经营场景
- 更精细的统计图表
- 正式通知权限申请与后台提醒执行链路

## 建议的下一步

- 补充自动化测试
- 接正式通知权限与 WorkManager 调度链
- 增加记录详情页
- 打磨发布前文案、图标与隐私说明
- 准备首版内测包
