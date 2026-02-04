# 天气可视化分析系统

基于 Spring Boot 3 + Vue 3 + TypeScript + Vite 的现代化天气可视化分析系统，支持实时天气数据获取、可视化图表展示、AI 智能对话、个性化配置等功能。

## 功能特性

- 🌤️ 实时天气数据展示（温度、湿度、风速、气压、能见度等）
- 📊 丰富的可视化图表（24小时趋势、7天预报、空气质量仪表盘）
- 🤖 通义千问 AI 智能对话助手
- ⭐ 城市收藏与快速切换
- ⚙️ 个性化配置（温度单位、风速单位、模块显示控制）
- 🚨 天气预警信息展示
- 🎨 现代化 UI 设计（卡片化、响应式、渐变色主题）
- 🔧 Mock 数据兜底机制（无 API Key 也能正常运行）

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database（内存数据库）
- OkHttp（HTTP 客户端）
- Lombok
- SpringDoc OpenAPI（Swagger 文档）

### 前端
- Vue 3.4.0
- TypeScript 5.3.3
- Vite 5.0.8
- Pinia（状态管理）
- Vue Router 4.2.5
- Element Plus 2.4.4（UI 组件库）
- ECharts 5.4.3（图表库）
- Axios 1.6.2（HTTP 客户端）
- Day.js（日期处理）

## 快速开始

### 前置要求

- JDK 17+
- Node.js 18+
- Maven 3.6+

### 后端启动

1. 进入后端目录
```bash
cd BackEnd
```

2. 配置环境变量（可选）

创建 `application-local.yml` 或直接设置环境变量：

```yaml
# application-local.yml
app:
  mock-enabled: false  # 是否强制使用 mock 数据

weather:
  provider: OPENWEATHER
  base-url: https://api.openweathermap.org/data/3.0/onecall
  api-key: your-openweather-api-key  # 在 https://openweathermap.org/api 获取

ai:
  qwen:
    api-key: your-qwen-api-key  # 在 https://dashscope.console.aliyun.com/ 获取
    model: qwen-turbo
```

或使用环境变量：

```bash
export APP_MOCK_ENABLED=false
export WEATHER_API_KEY=your-openweather-api-key
export QWEN_API_KEY=your-qwen-api-key
```

3. 启动后端服务
```bash
# 使用 Maven
mvn spring-boot:run

# 或使用 Maven Wrapper
./mvnw spring-boot:run
```

4. 访问 Swagger 文档
```
http://localhost:8080/swagger-ui.html
```

5. 访问 H2 控制台
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:weatherdb
用户名: sa
密码: (留空)
```

### 前端启动

1. 进入前端目录
```bash
cd FrontEnd
```

2. 安装依赖
```bash
npm install
```

3. 配置环境变量（可选）

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```env
VITE_API_BASE=http://localhost:8080
VITE_APP_TITLE=Weather Visualizer
VITE_THEME=light
```

4. 启动开发服务器
```bash
npm run dev
```

5. 访问应用
```
http://localhost:5173
```

## 配置参数说明

### 后端 ARG 参数

所有参数支持三种配置方式：
1. `application.yml` 配置文件
2. 环境变量
3. 启动参数：`--param.name=value`

| 参数名 | 环境变量 | 默认值 | 说明 |
|--------|-----------|---------|------|
| `app.mock-enabled` | `APP_MOCK_ENABLED` | 空 | 是否强制使用 mock 数据。不配置时，根据 API Key 是否存在自动判断 |
| `weather.provider` | `WEATHER_PROVIDER` | `OPENWEATHER` | 天气数据提供商 |
| `weather.base-url` | `WEATHER_BASE_URL` | 官方地址 | 天气 API 基础 URL |
| `weather.api-key` | `WEATHER_API_KEY` | 空 | 天气 API 密钥 |
| `ai.qwen.api-key` | `QWEN_API_KEY` | 空 | 通义千问 API 密钥 |
| `ai.qwen.model` | `QWEN_MODEL` | `qwen-turbo` | 通义千问模型名称 |
| `server.port` | `APP_PORT` | `8080` | 后端服务端口 |

**配置示例：**

```bash
# 使用环境变量
export WEATHER_API_KEY=your-key
export QWEN_API_KEY=your-key
export APP_PORT=9090

# 使用启动参数
java -jar app.jar --weather.api-key=your-key --ai.qwen.api-key=your-key --app.port=9090
```

### 前端 ARG 参数

| 参数名 | 默认值 | 说明 |
|--------|---------|------|
| `VITE_API_BASE` | `http://localhost:8080` | 后端 API 基础 URL |
| `VITE_APP_TITLE` | `Weather Visualizer` | 应用标题 |
| `VITE_THEME` | `light` | 主题（预留） |

**配置示例：**

```env
VITE_API_BASE=http://localhost:8080
VITE_APP_TITLE=Weather Visualizer
VITE_THEME=light
```

## Mock 模式说明

系统内置了完整的 Mock 数据兜底机制，确保在没有 API Key 的情况下也能正常运行。

### 自动 Mock 判断

- 如果 `weather.api-key` 为空，天气相关接口自动返回 Mock 数据
- 如果 `ai.qwen.api-key` 为空，AI 对话接口自动返回 Mock 数据
- 如果 `app.mock-enabled` 显式设置为 `true`，强制所有接口返回 Mock 数据

### Mock 数据特点

- **天气数据**：包含温度、湿度、风速等完整信息，数据随时间变化合理
- **城市搜索**：内置 20+ 常见城市，支持模糊匹配
- **AI 对话**：基于天气数据提供智能建议回复
- **预警信息**：随机生成预警信息，模拟真实场景

### 使用 Mock 模式

```bash
# 方式1：不配置任何 API Key（自动 Mock）
export WEATHER_API_KEY=
export QWEN_API_KEY=

# 方式2：显式启用 Mock
export APP_MOCK_ENABLED=true

# 方式3：启动参数
java -jar app.jar --app.mock-enabled=true
```

## API 接口文档

启动后端服务后，访问 Swagger 文档查看完整 API 文档：

```
http://localhost:8080/swagger-ui.html
```

### 主要接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/health` | GET | 健康检查 |
| `/api/cities/search?keyword=xxx` | GET | 搜索城市 |
| `/api/weather/current?lat=..&lon=..&city=..` | GET | 获取当前天气 |
| `/api/weather/forecast/hourly?lat=..&lon=..&city=..` | GET | 获取24小时预报 |
| `/api/weather/forecast/daily?lat=..&lon=..&city=..` | GET | 获取7天预报 |
| `/api/weather/alerts?lat=..&lon=..&city=..` | GET | 获取天气预警 |
| `/api/preferences` | GET | 获取用户偏好设置 |
| `/api/preferences` | PUT | 更新用户偏好设置 |
| `/api/favorites` | GET | 获取收藏城市列表 |
| `/api/favorites` | POST | 添加收藏城市 |
| `/api/favorites?name=..&country=..&lat=..&lon=..` | DELETE | 删除收藏城市 |
| `/api/ai/chat` | POST | AI 对话 |

## 项目结构

### 后端结构

```
BackEnd/
├── src/main/java/com/example/weather/
│   ├── config/              # 配置类
│   ├── common/              # 通用类
│   ├── controller/           # 控制器
│   ├── domain/
│   │   ├── dto/           # 数据传输对象
│   │   └── entity/        # 实体类
│   ├── repository/          # 数据访问层
│   ├── service/            # 业务逻辑层
│   └── client/            # 外部 API 客户端
└── src/main/resources/
    └── application.yml     # 配置文件
```

### 前端结构

```
FrontEnd/
├── src/
│   ├── components/
│   │   ├── layout/        # 布局组件
│   │   └── weather/       # 天气相关组件
│   ├── views/             # 页面视图
│   ├── stores/            # Pinia 状态管理
│   ├── services/          # API 服务
│   ├── router/            # 路由配置
│   ├── types/             # TypeScript 类型定义
│   ├── styles/            # 全局样式
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## 常见问题

### 1. 后端启动失败

**问题**：`java.lang.ClassNotFoundException`

**解决**：确保 JDK 版本为 17+，运行 `mvn clean install` 重新构建

### 2. 前端无法连接后端

**问题**：`Network Error` 或 `CORS Error`

**解决**：
- 检查后端是否正常启动
- 检查 `.env` 中的 `VITE_API_BASE` 配置
- 确保后端端口（默认 8080）未被占用

### 3. 天气数据加载失败

**问题**：显示"加载天气数据失败"

**解决**：
- 检查是否配置了有效的 `WEATHER_API_KEY`
- 或启用 Mock 模式：`export APP_MOCK_ENABLED=true`

### 4. AI 对话无响应

**问题**：AI 助手一直显示 loading

**解决**：
- 检查是否配置了有效的 `QWEN_API_KEY`
- 或启用 Mock 模式

### 5. H2 控制台无法访问

**问题**：访问 `/h2-console` 显示 404

**解决**：
- 检查 `application.yml` 中 `spring.h2.console.enabled=true`
- 确保使用正确的 JDBC URL：`jdbc:h2:mem:weatherdb`

### 6. 前端构建失败

**问题**：`npm run build` 报错

**解决**：
- 运行 `npm install` 重新安装依赖
- 检查 Node.js 版本是否为 18+

## 开发指南

### 添加新的天气数据源

1. 在 `WeatherClient` 中添加新的数据源调用方法
2. 在 `WeatherService` 中集成新的数据源
3. 在 `application.yml` 中添加配置项

### 添加新的图表组件

1. 在 `src/components/weather/` 下创建新组件
2. 使用 ECharts 或其他图表库实现
3. 在 `Dashboard.vue` 中引入并使用

### 自定义主题

修改 `src/styles/theme.css` 中的 CSS 变量：

```css
:root {
  --primary-color: #667eea;
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  /* ... 其他变量 */
}
```

## 生产部署

### 后端部署

```bash
# 构建
mvn clean package

# 运行
java -jar target/weather-1.0.0.jar \
  --server.port=8080 \
  --weather.api-key=your-key \
  --ai.qwen.api-key=your-key
```

### 前端部署

```bash
# 构建
npm run build

# dist 目录即为构建产物，可部署到任何静态服务器
```

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue。
