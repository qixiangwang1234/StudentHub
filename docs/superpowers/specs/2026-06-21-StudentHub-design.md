# StudentHub — 学生智能助手 Android App 设计规格

> 一个完全离线的原生 Kotlin 安卓应用，集成 DeepSeek API 的 AI 能力，
> 提供课表管理、智能待办、成绩追踪三大核心功能。

---

## 1. 架构总览

### 1.1 技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 语言 | Kotlin | 原生 Android 开发 |
| UI | Jetpack Compose + Material3 | 现代声明式 UI，支持灵动动画 |
| 本地存储 | Room Database | SQLite 封装，所有数据存本地 |
| 网络 | OkHttp + DeepSeek API | 手机直接调用 AI API，无中间服务器 |
| 图片识别 | CameraX + ML Kit OCR | 拍照识别课表文字 |
| 依赖注入 | Hilt | 模块化管理依赖 |

### 1.2 核心原则

- **完全离线优先**：所有数据存储在手机本地 Room 数据库
- **不依赖任何后端服务器**：唯一网络请求是 DeepSeek API 调用（可选，无 API Key 也能正常使用除 AI 外的所有功能）
- **API Key 可配置**：在设置页面配置 DeepSeek API Key，配置后自动启用 AI 功能

---

## 2. 导航结构

### 2.1 底部导航栏（4 Tab）

```
| 首页  | 课表  | 待办  | 我的 |
|  🏠   |  📅  |  📋  |  👤  |
```

- **首页**：信息看板 + AI 助手入口
- **课表**：学期课程总览，点击课程进入详情
- **待办**：全部待办事项，可按课程筛选
- **我的**：设置（API Key 配置）+ 个人资料

### 2.2 页面层级

```
首页（Dashboard）
├── 今日课程卡片
├── 待办速览
├── 成绩动态摘要
└── AI 助手入口（底部悬浮输入框）

课表
├── 学期选择
├── 周视图（周一到周日 Tab）
│   └── 课程块（显示：课名、地点、时间）
│       └── 🔴 红点 = 该课程有关联待办
└── 拍课表识别入口

课程详情（从课表点击进入）
├── 基本信息（教室、教师、时间、周数）
├── 📊 成绩构成（自定义占比 + 期末填入分数）
│   ├── 条目列表（项目名 + 百分比 + 进度条）
│   └── 编辑模式（增删改项目/占比）
├── 📝 备注（自由文本 + AI 辅助撰写）
└── 📋 关联待办（该课程的所有待办）

待办列表
├── 筛选 Tab：全部 | 📚课程 | 📌其他
├── 课程相关待办（显示课程标签 + 截止日）
├── 独立待办（无课程关联）
└── ＋ 新建待办

我的
├── 个人资料
├── ⚙️ API 配置（DeepSeek Key）
│   ├── Key 输入（明文/密文切换）
│   ├── 连接测试按钮
│   └── 模型选择（deepseek-chat / deepseek-reasoner）
├── 数据管理（导出/导入/清除）
└── 关于
```

---

## 3. 功能模块详细设计

### 3.1 📅 课表管理

#### 3.1.1 手动创建课程

| 字段 | 类型 | 说明 |
|---|---|---|
| 课程名 | String | 必填 |
| 教师 | String | 选填 |
| 教室 | String | 选填 |
| 上课时间 | 星期 + 节次/时分 | 必填 |
| 下课时间 | 节次/时分 | 必填 |
| 周数范围 | IntRange（如 1-16） | 选填，默认全学期 |
| 颜色标签 | Color | 选填，课表块颜色 |
| 备注 | Text | 选填 |

#### 3.1.2 拍照识别课表

- 使用 CameraX 拍照或从相册选择
- ML Kit Text Recognition OCR 识别图片中的文字
- 智能解析：课程名 ↔ 时间 ↔ 地点的对应关系
- 解析结果预览，用户确认后批量导入
- 支持手动修正识别错误

#### 3.1.3 课表视图

- 周视图：顶部星期 Tab（周一~周日），点击切换
- 左侧时间轴，右侧课程块
- 每个课程块显示：课程名、教室
- 有关联待办的课程显示 🔴 小红点

### 3.2 📋 待办与提醒

#### 3.2.1 待办属性

| 字段 | 类型 | 说明 |
|---|---|---|
| 标题 | String | 必填 |
| 关联课程 | Course? | 可选，关联后课表显示红点 |
| 截止时间 | DateTime | 必填 |
| 提醒时间 | DateTime | 可选，到点系统通知 |
| 优先级 | 高/中/低 | 可选 |
| 完成状态 | Boolean | 默认 false |
| 备注 | Text | 选填 |
| 创建时间 | DateTime | 自动 |
| AI 生成标记 | Boolean | 标记是否由 AI 创建 |

#### 3.2.2 创建流程

1. 点击「+」进入新建页
2. 输入标题
3. **选择关联课程**（可选）：
   - 选课程 → 自动填入区域显示教师、地点、建议截止时间
   - 选"不关联" → 普通待办
4. 设置截止时间、提醒
5. 保存

#### 3.2.3 红点提示逻辑

- 课表中，某课程有**未完成**的关联待办 → 课程卡片右上角显示 🔴 红点
- 所有待办都完成后 → 红点消失
- 待办列表可按「课程相关 / 其他」筛选

#### 3.2.4 本地通知

- AlarmManager 或 WorkManager 实现定时通知
- 到提醒时间弹出系统通知
- 点击通知跳转到对应待办

### 3.3 📊 成绩追踪

#### 3.3.1 成绩构成模型

```
课程
└── 成绩构成（用户自定义）
    ├── 作业    20%  ← 用户可增删改项目名和占比
    ├── 期中    30%
    ├── 期末    50%
    └── (可添加更多项目，如实验 10%)
```

- 占比总和无需强制等于 100%（用户自定义）
- 期末出分后逐项填入分数
- 系统自动按权重计算加权总分

#### 3.3.2 成绩在 UI 中的位置

- 入口在「课程详情」页面中部
- 未出分时显示虚线框提示："期末出分后 → 点击输入成绩"
- 已录入后显示：各项目得分 + 进度条 + 加权总分
- 期末可编辑/修改

#### 3.3.3 成绩动态展示

- 首页 Dashboard 显示"暂无成绩数据"友好提示
- 待录入成绩后，首页显示最新成绩摘要

### 3.4 🤖 AI 助手（DeepSeek API）

#### 3.4.1 触发方式

- 首页底部悬浮输入框（✨ "AI 帮我..."）
- 点击后进入全屏 AI 对话页
- 课程详情页的备注编辑区也有 AI 辅助入口

#### 3.4.2 AI 能力

| 能力 | 说明 |
|---|---|
| 自然语言创建待办 | "下周三高数期中考帮我创建复习待办" → 自动解析课程、时间、生成多条待办 |
| 智能填写备注 | "给高数加个备注：考试范围第六章第七章" → 自动填入课程备注 |
| 成绩分析建议 | "我作业85期中78期末要考多少才能上90" → 计算并给出建议 |
| 课表问答 | "我周五有几节课" → 从本地数据回答 |
| 文本润色 | 美化备注文本 |

#### 3.4.3 API 调用机制

- 仅需用户配置：DeepSeek API Key
- 模型可选：deepseek-chat / deepseek-reasoner
- 支持 Stream 模式，打字机效果输出
- 调用失败优雅降级：显示错误提示，不影响其他功能
- **无 API Key 时**：AI 输入框隐藏或显示"需配置 API 以启用"提示

#### 3.4.4 System Prompt 设计

AI 的系统提示包含：
- 用户课表数据摘要（课程列表）
- 待办列表摘要（未完成事项）
- 成绩构成摘要
- 指令风格：简洁、直接、执行优先

### 3.5 🏠 首页 Dashboard

#### 3.5.1 页面布局（从上到下）

1. **问候区**：👋 早上好 + 日期 + 头像
2. **今日课程**：下一节课（紫色渐变卡片）+ 今日总节数
3. **待办速览**：最近 2 项待办 + 截止日标签，点击"查看全部"跳转待办页
4. **成绩动态**：成绩摘要或"暂无数据"提示
5. **AI 输入框**（底部悬浮，固定）

---

## 4. 数据模型

### 4.1 Room Entity

```kotlin
@Entity
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val teacher: String? = null,
    val classroom: String? = null,
    val dayOfWeek: Int,          // 1=Mon ... 7=Sun
    val startTime: String,       // "08:00"
    val endTime: String,         // "09:35"
    val weekStart: Int = 1,
    val weekEnd: Int = 20,
    val colorHex: String? = null,
    val notes: String? = null
)

@Entity
data class GradeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val name: String,            // "作业", "期中", "期末"
    val weight: Int,             // 20, 30, 50
    val score: Float? = null,    // 实际得分，期末填入
    val totalScore: Float = 100f // 满分，默认100
)

@Entity
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val courseId: Long? = null,  // null = 独立待办
    val dueDate: Long,           // timestamp
    val remindAt: Long? = null,  // timestamp, null = 不提醒
    val priority: Int = 0,       // 0=低, 1=中, 2=高
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false
)
```

---

## 5. 非功能需求

### 5.1 性能

- Room 数据库操作均在 IO 线程（Coroutine + Flow）
- AI API 调用异步执行，支持取消
- 课表列表使用 LazyColumn，大量课程数据不卡顿

### 5.2 UI/UX

- Material3 Design 规范
- 页面转场使用共享元素动画 + 淡入淡出
- 列表项删除/完成带交互动画
- 支持深色模式
- 中文字体优化

### 5.3 安全

- API Key 使用 EncryptedSharedPreferences 存储
- 无网络权限时 AI 功能自然降级
- 所有数据仅在本地，不自动上传

### 5.4 兼容性

- 最低支持 Android 8.0 (API 26)
- 适配手机和平板
- 支持中英文界面

---

## 6. 实施路线图

| 阶段 | 内容 | 预计工作 |
|---|---|---|
| **Phase 1** | 项目脚手架、Room 数据库、底部导航、课表 CRUD | 基础 |
| **Phase 2** | 待办模块、本地通知、红点逻辑 | 基础 |
| **Phase 3** | 成绩构成自定义 + 成绩录入 + 自动计算 | 基础 |
| **Phase 4** | 拍照识别课表（CameraX + ML Kit OCR） | 复杂 |
| **Phase 5** | AI 助手集成（DeepSeek API 调用 + 对话 UI） | 复杂 |
| **Phase 6** | 首页 Dashboard、设置页面、数据导出/导入 | 中等 |
| **Phase 7** | 动画打磨、深色模式、测试、APK 打包 | 优化 |

---

## 7. 开放问题

- 课表拍照识别后的批量导入流程是否需要二次确认页面？
- AI 功能是否要支持自定义 System Prompt？
- 数据导出格式：JSON 还是 CSV？
