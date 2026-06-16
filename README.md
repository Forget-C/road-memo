<p align="center">
  <img src="docs/assets/roadmemo-app-icon-v1.png" alt="RoadMemo" width="120" />
</p>

<h1 align="center">RoadMemo · 长路有记</h1>

<p align="center">
  <strong>本地优先 · 隐私至上 · 开源 Android 车主记账</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-0.1.0-teal" alt="Version" />
  <img src="https://img.shields.io/badge/minSdk-26-green" alt="Min SDK" />
  <img src="https://img.shields.io/badge/targetSdk-36-blue" alt="Target SDK" />
  <img src="https://img.shields.io/badge/license-MIT-orange" alt="License" />
</p>

---

## 关于

RoadMemo 是一款专注**车辆全生命周期记账**的 Android 应用。记录加油、充电、保养、费用和续期事项，提供首页摘要、智能提醒和基础统计——全部数据存储在本地，无需登录，无需联网。

> 当前为 `V0.1` MVP 阶段，主线功能已开发完成，进入收尾优化与内测准备。

### 设计原则

- **本地优先** — 所有数据存储在设备上，不上传任何服务器
- **隐私至上** — 无需账号，无需登录，你的数据只属于你
- **数据可迁移** — 支持 CSV 导出与 JSON 完整备份恢复
- **多能源车型** — 汽油 / 柴油 / 油混 / 插混 / 纯电，一车一档

## 功能

| 模块 | 能力 |
|------|------|
| 车辆管理 | 新增车辆、默认车辆切换、动力类型管理 |
| 能源记录 | 加油 / 充电记录，支持新增、编辑、删除 |
| 保养记录 | 保养记录管理，下次保养日期 / 里程提醒 |
| 费用记录 | 过路费、停车费、保险等杂项费用管理 |
| 续期事项 | 年检、保险续期管理，自动生成提醒 |
| 提醒系统 | 查看提醒、标记已处理、忽略、跳回来源记录 |
| 首页摘要 | 本月费用汇总、最近记录、油耗 / 电耗概览 |
| 统计分析 | 分类汇总、近 6 个月趋势、能耗表现 |
| 数据能力 | CSV 导出、JSON 完整备份与恢复 |

## 截图

> TODO: 补充应用截图

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Navigation Compose |
| 数据库 | Room |
| DI | Hilt |
| 存储 | DataStore |
| 后台 | WorkManager |
| 异步 | Coroutines + Flow |

## 运行环境

- **minSdk**: 26 (Android 8.0)
- **compileSdk / targetSdk**: 36
- **Java**: 17

## 本地构建

```bash
# 克隆仓库
git clone https://github.com/road-memo/road-memo.git
cd road-memo

# 构建 Debug APK
GRADLE_USER_HOME=$PWD/.gradle-home ./gradlew --no-configuration-cache :app:assembleDebug
```

构建产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 项目结构

```text
app/src/main/java/com/roadmemo/app/
├── data/           # Room、导出备份、仓储实现
├── di/             # Hilt 依赖注入
├── domain/         # 领域模型、仓储接口、业务计算
├── navigation/     # 路由与导航图
└── ui/
    ├── components/ # 通用 Compose 组件
    ├── screens/    # 页面（首页、记录、统计、设置等）
    └── theme/      # 主题、配色、排版
```

## 架构文档

- [数据模型](docs/architecture/roadmemo-v0.1-data-model.md)
- [交互规格](docs/architecture/roadmemo-v0.1-interaction-spec.md)
- [视觉基线](docs/architecture/roadmemo-v0.1-visual-baseline.md)
- [设计系统](docs/architecture/roadmemo-v0.1-design-system-v1.md)
- [能耗功能规划](docs/architecture/roadmemo-v0.1-consumption-feature-plan.md)
- [冻结清单](docs/architecture/roadmemo-v0.1-freeze-checklist.md)
- [交叉审查报告](docs/architecture/roadmemo-v0.1-cross-review-report.md)

## 路线图

### V0.1 (MVP) — 已完成

- [x] 多能源车型支持
- [x] 能源 / 保养 / 费用 / 续期四类记录
- [x] 首页摘要与提醒
- [x] 基础统计与趋势
- [x] CSV 导出与 JSON 备份恢复
- [x] 油耗 / 电耗计算

### 后续规划

- [ ] 自动化测试补充
- [ ] 正式通知权限与 WorkManager 调度链
- [ ] 更丰富的统计图表
- [ ] 图片附件
- [ ] 云同步（可选，本地优先）
- [ ] OCR 票据识别

## 贡献

项目目前处于早期阶段，欢迎 Issue 和 PR。

## License

MIT