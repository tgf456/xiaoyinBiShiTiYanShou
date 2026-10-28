# 素材创作台（素材制作网站）

一个可独立运行的全栈「素材制作」网站：浏览模板 → 上传图片 → 提交制作任务 → 后台异步多步骤处理 → 查看历史记录与结果。制作过程不调用真实 AI，使用**可控制耗时与指定阶段失败**的模拟生成器完成业务闭环。

- 前端：Next.js（App Router）+ React + TypeScript
- 后端：Java + Spring Boot + MyBatis + MySQL

---

## 1. 技术版本

| 组件 | 版本 |
| --- | --- |
| Java | 17（本地验证使用 JDK 21，兼容） |
| Spring Boot | 3.2.5 |
| MyBatis | mybatis-spring-boot-starter 3.0.3 |
| MySQL | 8.0 |
| Node.js | 20（CI / Docker 使用） |
| Next.js | 14.2.15 |
| React | 18.3.1 |
| TypeScript | 5.5.x |
| Vitest | 1.6.x |

## 2. 项目结构

```
xiaoyinBiShiTi/
├── backend/                          # Java 后端
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/materialmaker/
│       │   ├── config/               # 线程池、CORS、静态资源
│       │   ├── controller/           # REST 接口与全局异常处理
│       │   ├── dto/                  # 请求/响应模型
│       │   ├── exception/
│       │   ├── mapper/               # MyBatis Mapper 接口
│       │   ├── model/                # 实体与枚举、状态/阶段
│       │   ├── service/              # 业务、异步引擎、模拟器
│       │   └── util/
│       └── resources/
│           ├── application.yml
│           ├── db/01-schema.sql      # 建表（幂等）
│           ├── db/02-data.sql        # 模板种子（幂等）
│           ├── mapper/*.xml
│           └── static/demo/*.svg     # 模板封面示例
│   └── src/test/java/...             # 后端单测
├── frontend/                         # Next.js 前端
│   ├── package.json / package-lock.json
│   ├── Dockerfile / next.config.mjs / tsconfig.json / vitest.config.mts
│   └── src/
│       ├── app/                      # App Router 页面（列表/详情/历史）
│       ├── components/               # 卡片、上传位、任务卡等
│       ├── hooks/useTaskTracker.ts   # 轮询与异步状态管理
│       └── lib/                      # types / api / user
├── samples/                          # 可上传的样例图片（jpeg/png 模拟素材）
├── docker-compose.yml
├── .env.example
└── .github/workflows/ci.yml
```

## 3. 构建 / 测试 / 启动命令

### 一键启动（推荐，容器隔离）

```bash
docker compose up --build
```

启动后：

- 前端：http://localhost:3000
- 后端：http://localhost:8080
- MySQL：localhost:3306（数据库 `material_maker`）

### 本地分别启动（需自备 MySQL）

后端（首次启动会自动建表 + 注入模板种子数据）：

```bash
cd backend
mvn spring-boot:run            # 或 mvn -DskipTests package 后 java -jar target/*.jar
```

前端：

```bash
cd frontend
npm install
npm run dev                    # 开发
npm run build && npm start     # 生产
```

### 测试与检查

```bash
# 后端测试（步骤顺序与失败中断）
cd backend && mvn test

# 前端
cd frontend
npm run lint        # ESLint
npm run typecheck   # tsc --noEmit
npm run test        # Vitest
npm run build       # Next 生产构建
```

## 4. 前后端与数据库如何连接

- 前端通过 `NEXT_PUBLIC_API_BASE_URL`（默认 `http://localhost:8080`）直接调用 Java 后端；所有接口约定返回 JSON，错误统一为 `{ code, message, timestamp }`。
- 后端通过环境变量连接 MySQL：

| 变量 | 默认 | 说明 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | 数据库地址 |
| `DB_PORT` | `3306` | 端口 |
| `DB_NAME` | `material_maker` | 库名 |
| `DB_USER` | `maker` | 用户名 |
| `DB_PASSWORD` | `maker123` | 密码 |
| `MYSQL_ROOT_PASSWORD` | `root123` | Compose 中 MySQL root 密码 |
| `STORAGE_ROOT` | `./data` | 素材/结果存储根目录 |

上述均为无真实凭据的示例值，可复制 `.env.example` 为 `.env` 覆盖。

## 5. 数据库初始化与素材准备

- 建表与种子数据位于 `backend/src/main/resources/db/`：`01-schema.sql`（`CREATE TABLE IF NOT EXISTS`）、`02-data.sql`（`INSERT IGNORE`）。后端配置 `spring.sql.init.mode=always`，**首次启动自动、幂等初始化**，无需手工执行 SQL。
- 模板封面为后端 classpath 下的 `static/demo/*.svg`，通过 `/demo/**` 访问；种子数据中的 `cover_url` 即指向这些本地文件。
- 用户上传的图片保存到 `${STORAGE_ROOT}/uploads/`，模拟「结果」复制到 `${STORAGE_ROOT}/results/`，分别经 `/uploads/**`、`/results/**` 静态访问。目录在 Docker 中挂载为数据卷 `storage-data`。
- `samples/` 目录提供 4 张可上传的样例 PNG（纯色），便于直接演示上传。

## 6. 主要接口

所有业务请求建议携带请求头 `X-User-Id`（用户身份，见第 8 节）。

| 方法 | 路径 | 说明 | 关键字段 |
| --- | --- | --- | --- |
| GET | `/api/templates` | 模板列表，可选 `?category=` | 返回数组，字段见下 |
| GET | `/api/templates/{id}` | 模板详情 | 见下 |
| POST | `/api/assets` | 上传素材（multipart） | `file`、`templateId`；返回 `{ id, url, originalName, sizeBytes }` |
| POST | `/api/tasks` | 提交制作任务 | `{ templateId, assetIds[] }`；返回任务对象 |
| GET | `/api/tasks` | 个人历史 | 返回任务数组 |
| GET | `/api/tasks/{id}` | 查询单个任务 | 返回任务对象 |
| GET | `/api/simulator/config` | 查看模拟配置 | `{ stageDelayMs, failStage }` |
| PUT | `/api/simulator/config` | 修改模拟配置 | `{ stageDelayMs, failStage }` |
| POST | `/api/simulator/reset` | 恢复默认模拟配置 | — |

模板字段（与需求示例一致、字段名自行设计）：

```json
{
  "id": "demo-postcard",
  "title": "旅行明信片",
  "category": "创意图片",
  "coverUrl": "/demo/postcard-cover.svg",
  "description": "上传图片，模拟生成一张明信片",
  "requiredImageCount": 2,
  "acceptedMimeTypes": ["image/jpeg", "image/png"],
  "maxFileSizeBytes": 5242880
}
```

任务字段：

```json
{
  "id": "xxx",
  "userId": "xxx",
  "templateId": "demo-postcard",
  "status": "PROCESSING",
  "currentStage": "GENERATING",
  "failStage": null,
  "failReason": null,
  "resultUrl": null,
  "createdAt": "2026-10-28T19:00:00",
  "updatedAt": "2026-10-28T19:00:05"
}
```

错误响应示例：`{ "code": "NOT_FOUND", "message": "模板不存在: xxx", "timestamp": "..." }`。

### 任务状态与阶段

**整体状态 `status`（TaskStatus）**：`WAITING`（等待处理）→ `PROCESSING`（处理中）→ `SUCCESS` / `FAILED`。

**当前阶段 `currentStage` / 失败阶段 `failStage`（TaskStage）**，后台按顺序执行：

1. `VALIDATING` 素材检查
2. `GENERATING` 模拟生成
3. `FINALIZING` 结果整理

每一步开始都会把 `currentStage` 持久化到 MySQL（独立可观察的执行边界）；某一步失败会写入 `failStage` 与 `failReason` 并停止后续步骤。

### 后台执行方式

- 后端使用 `ThreadPoolTaskExecutor`（core = max = 4，队列近似无界）；`POST /api/tasks` 完成校验与建库后**立即返回 taskId，不等待制作结束**。
- 异步线程按 `素材检查 → 模拟生成 → 结果整理` 顺序执行，每阶段按配置 sleep 后检查失败注入；结果整理成功时复制首张素材生成 `/results/**` 结果地址。
- **查询接口只读数据库状态，不驱动任务推进**；完成任务不会因后续查询变回处理中。
- **重启恢复（断点续跑）**：`TaskRecoveryRunner` 在应用启动时扫描 `WAITING` / `PROCESSING` 任务并重新入队。三阶段流水线本身幂等（阶段状态为覆盖写、结果文件 `REPLACE_EXISTING` 复制），因此采用"整单重跑"而非精确断点续传——重启不会造成任务悬挂。注意：恢复依赖数据库中的状态记录，多实例部署仍不安全（见已知限制）。

## 7. 演示方法（成功 / 失败 / 耗时 / 多任务 / 刷新）

> `BASE` 为 `http://localhost:8080`。

1. **成功流程**：打开 http://localhost:3000 → 选模板 → 上传 `samples/*.png`（满足数量）→ 提交 → 进入「历史记录」看到任务经历 素材检查 → 模拟生成 → 结果整理，最终显示成功与结果预览图。

2. **指定阶段失败（可重复触发）**：

```bash
curl -X PUT "$BASE/api/simulator/config" \
  -H "Content-Type: application/json" \
  -d '{"failStage": "GENERATING"}'
```

   之后提交的任务会在「模拟生成」阶段失败并在历史记录显示失败原因。恢复成功：`-d '{"failStage": null}'` 或 `curl -X POST "$BASE/api/simulator/reset"`。

3. **调整处理耗时**：

```bash
curl -X PUT "$BASE/api/simulator/config" \
  -H "Content-Type: application/json" \
  -d '{"stageDelayMs": {"VALIDATING": 5000, "GENERATING": 8000, "FINALIZING": 3000}}'
```

   便于观察“处理中”与阶段推进。

4. **多任务排队/并行**：连续提交多个任务，历史页同时展示它们各自的阶段与状态，互不串扰；未完成任务自动轮询，全部结束后停止轮询。

5. **刷新恢复**：提交后刷新浏览器（或重新进入历史页），任务从 Java 后端恢复，未结束任务继续自动跟踪。

6. **重启恢复（断点续跑）**：先把耗时调长（如 `GENERATING: 15000`），提交任务并观察其进入"处理中"；然后重启后端（`docker compose restart backend` 或重启 Java 进程）。启动日志出现"服务重启恢复：已重新入队 N 个未完成任务"，任务重新执行三阶段并最终成功，前端轮询无需刷新即可看到任务由处理中继续走向成功。

## 8. 用户身份与个人记录设计

采用**轻量 Header 身份**方案：

- 前端首次访问在浏览器 `localStorage` 生成一个 UUID，之后所有请求通过 `X-User-Id` 请求头携带；
- 后端 `app_user` 表对首次出现的 id 幂等登记（`INSERT IGNORE`）；
- 任务记录以 `user_id` 隔离，历史查询只返回当前 `X-User-Id` 的记录。

**演示方法**：清除浏览器 `localStorage` 中的 `material-maker-user-id`（或换无痕窗口）即为“新用户”，其历史记录为空、与前一用户隔离。

## 9. CI 与测试

GitHub Actions（`.github/workflows/ci.yml`）在 push / pull_request 时执行：

- 后端：`mvn test` + `mvn -DskipTests package`
- 前端：`npm install` → `lint` → `typecheck` → `test` → `build`

测试覆盖：

- 后端 `TaskProcessorTest`：三阶段执行顺序 `VALIDATING → GENERATING → FINALIZING → 成功`；在指定阶段注入失败后中断、不再推进后续阶段。`TaskRecoveryRunnerTest`：启动恢复会重新入队 `WAITING` / `PROCESSING` 任务、无未完成任务时不动作。
- 前端 `TaskCard.test.tsx`：多任务状态/失败原因/结果互不串扰；`HistoryClient.test.tsx`：查询接口异常与“任务失败”严格区分（查询不可用时保留任务状态并提供重试，不误标失败）。

## 10. 部署与回滚

**部署**：`docker compose up --build`（首次构建 + 启动前端/后端/MySQL 并自动初始化数据）。

**回滚**：

```bash
# 仅重建服务（保留数据卷）
git checkout <上一版本 commit>
docker compose up --build

# 需要完全重置（清空数据库与素材，慎用）
docker compose down -v
docker compose up --build
```

## 11. 完成度、已知限制与实际投入

**已完成**：R-01 技术栈与职责；R-02 模板列表与种子数据；R-03 详情/上传/前后端校验；R-04 受理与异步；R-05 三阶段状态与查询；R-06 可重复失败与耗时控制；R-07 前端状态与请求异常区分；R-08 多任务与轮询生命周期；R-09 刷新恢复与历史结果；R-10 轻量身份；R-11 界面与交互；R-12 CI 与关键测试；R-14 容器交付；扩展项：重启后未完成任务自动恢复（整单重跑式断点续跑，见第 6 节）。

**已知限制 / 未实现（均为题设明确“不要求”）**：

- 重启恢复为“整单重跑”而非精确断点续传：已执行的阶段会重新执行（流水线幂等，结果一致）；如需精确从失败/中断的断点继续，可在任务表记录每阶段完成标记，本实现未做；
- 任务受理不幂等（无请求去重键，前端已防重复点击，后端重复提交会创建两个任务）；
- 仅单实例部署安全：未实现 DB 抢占式领取或分布式锁，多实例会重复执行任务；
- 「制作结果」为模拟：复制首张素材作为结果图，不做真实图像处理；
- 无真实 AI、支付、会员、第三方账号、国际化等；
- CORS 当前放开所有来源（仅用于演示/验收环境）。

**实际投入**：以“先搭骨架再逐模块实现”推进，先后完成后端数据层与异步引擎、后端测试、前端页面与轮询状态管理、前端测试、Docker/CI、素材与文档，并分别通过 `mvn test / package`、`vitest / lint / typecheck / build` 验证。

## 12. AI 使用说明

本项目使用 AI 编程工具完成。工作流：先通读需求沉，把它拆成「后端数据层 → 异步引擎 → 前端状态管理 → 工程交付」等任务清单；对每个模块给出明确的接口契约与表结构上下文，再生成/修改代码；随后通过 `mvn test`、`tsc --noEmit`、`vitest`、`next build` 等命令验证并修正问题。

示例：后端初版 `TaskProcessor` 使用 `Callable<Void>` 表达“可抛异常的阶段动作”，编译报错「lambda 返回类型错误」。修正方式：将其改为自定义 `@FunctionalInterface interface StepAction { void run() throws Exception; }`，从而让 `() -> checkAssets(task)` 这类 void lambda 正常匹配，重新 `mvn test` 通过。

## 13. 脚手架与素材复用来源

- 后端基于 Spring Boot 官方 starter 与 MyBatis Spring Boot Starter 的项目结构（非脚手架生成，手工组织包结构）。
- 前端基于 Next.js App Router 的官方项目结构（未使用 `create-next-app`，配置文件与目录手工组织）。
- 模板封面为自绘 SVG，样例上传图片为本地脚本生成的纯色 PNG，均不依赖付费或公司内部服务。