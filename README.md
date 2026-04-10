 # 校友社交平台

## 项目介绍

校友社交平台是一个专为校友设计的社交网络系统，旨在帮助校友之间建立联系、分享信息、参与活动，促进校友 community 的发展。

### 主要功能

- **用户认证**：登录、注册、验证码验证
- **校友档案**：个人资料管理、身份认证、档案完善
- **社交互动**：动态发布、评论、点赞、关注
- **活动管理**：活动发布、报名、管理
- **消息系统**：私信、系统通知
- **管理后台**：用户管理、内容审核、数据统计
- **WebSocket**：实时通信
- **AI 助手**：智能问答、内容生成

## 技术栈

### 前端技术栈

- **框架**：Vue 3
- **构建工具**：Vite
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP 客户端**：Axios
- **图表库**：ECharts

### 后端技术栈

- **框架**：Spring Boot 3
- **ORM**：MyBatis Plus
- **数据库**：MySQL
- **缓存**：Redis
- **认证**：JWT
- **实时通信**：WebSocket
- **文件存储**：MinIO
- **AI 集成**：Spring AI + 阿里云百炼
- **安全**：Spring Security

## 快速开始

### 环境要求

- **前端**：Node.js 18+、npm 9+
- **后端**：JDK 21+、Maven 3.8+
- **数据库**：MySQL 8.0+
- **缓存**：Redis 7.0+
- **文件存储**：MinIO

### 前端启动

1. 进入前端目录

```bash
cd frontend
```

2. 安装依赖

```bash
npm install
```

3. 启动开发服务器

```bash
npm run dev
```

前端服务将运行在 http://localhost:3000

### 后端启动

1. 配置数据库

在 `src/main/resources/application.yml` 中配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/social_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
```

2. 配置 Redis

```yaml
spring:
  data:
    redis:
      host: 192.168.88.139
      timeout: 5000ms
```

3. 配置 MinIO

```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: myadmin123
  secretKey: mypassword123456
  bucketName: avatar-bucket
```

4. 启动后端服务

```bash
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8120/api

## 项目结构

### 前端结构

```
frontend/
├── src/
│   ├── api/          # API 请求封装
│   ├── components/   # 通用组件
│   ├── router/       # 路由配置
│   ├── stores/       # Pinia 状态管理
│   ├── styles/       # 样式文件
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   │   ├── admin/    # 管理后台页面
│   │   ├── Activity*.vue   # 活动相关页面
│   │   ├── FeedView.vue    # 动态 feed
│   │   ├── HomeView.vue    # 首页
│   │   ├── LoginView.vue   # 登录页
│   │   ├── RegisterView.vue # 注册页
│   │   └── Profile*.vue    # 个人资料页面
│   ├── App.vue       # 根组件
│   └── main.js       # 入口文件
├── dist/            # 构建输出
├── index.html       # HTML 模板
├── package.json     # 项目配置
└── vite.config.js   # Vite 配置
```

### 后端结构

```
src/main/java/com/run/runsocialplatform/
├── common/          # 通用模块
│   ├── config/      # 全局配置
│   ├── constant/    # 常量定义
│   ├── enums/       # 枚举类
│   ├── exception/   # 异常处理
│   ├── page/        # 分页工具
│   ├── result/      # 响应结果
│   └── utils/       # 工具类
├── module/          # 业务模块
│   ├── activity/    # 活动管理
│   ├── admin/       # 管理后台
│   ├── alumni/      # 校友档案
│   ├── auth/        # 认证授权
│   ├── avatar/      # 头像管理
│   ├── follow/      # 关注系统
│   ├── message/     # 消息系统
│   └── post/        # 动态管理
├── security/        # 安全模块
│   ├── config/      # 安全配置
│   ├── filter/      # 过滤器
│   ├── handler/     # 处理器
│   ├── model/       # 安全模型
│   ├── service/     # 安全服务
│   └── utils/       # 安全工具
├── websocket/       # WebSocket 模块
└── RunSocialPlatformApplication.java  # 应用入口
```

## API 文档

后端提供了完整的 RESTful API，可通过以下方式访问 API 文档：

- **Swagger UI**：http://localhost:8120/api/swagger-ui.html
- **OpenAPI 文档**：http://localhost:8120/api/v3/api-docs

## 核心功能模块

### 1. 用户认证模块

- 登录/注册/登出
- 验证码发送与验证
- JWT 令牌管理
- 密码重置

### 2. 校友档案模块

- 个人资料管理
- 身份认证
- 档案完善
- 校友搜索

### 3. 动态模块

- 发布动态
- 评论与回复
- 点赞
- 动态列表

### 4. 活动模块

- 活动发布
- 活动报名
- 活动管理
- 活动统计

### 5. 消息模块

- 私信
- 系统通知
- 未读消息提醒
- 消息历史

### 6. 关注模块

- 关注/取消关注
- 关注列表
- 粉丝列表

### 7. 管理后台

- 用户管理
- 内容审核
- 活动管理
- 数据统计

### 8. AI 助手

- 智能问答
- 内容生成
- 聊天记忆

## 部署指南

### 前端部署

1. 构建生产版本

```bash
cd frontend
npm run build
```

2. 将 `dist` 目录部署到 Nginx 或其他静态文件服务器

### 后端部署

1. 构建可执行 jar 包

```bash
mvn clean package
```

2. 运行 jar 包

```bash
java -jar target/run-social-platform-1.0-SNAPSHOT.jar
```

## 开发规范

### 前端开发规范

- 使用 Vue 3 Composition API
- 组件命名使用 PascalCase
- 变量命名使用 camelCase
- 常量命名使用 UPPER_CASE
- 使用 ESLint 进行代码检查
- 提交前运行 `npm run build` 确保构建通过

### 后端开发规范

- 遵循 Spring Boot 最佳实践
- 使用 Lombok 简化代码
- 异常处理统一使用 `BusinessException`
- 响应结果统一使用 `Result` 包装
- 数据库操作使用 MyBatis Plus
- 提交前运行 `mvn compile` 确保编译通过

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者：[RUN]
- 邮箱：[3206201719@qq.com]
- 项目地址：[https://github.com/yourusername/alumni-social-platform](https://github.com/yourusername/alumni-social-platform)

---

**感谢使用校友社交平台！** 🎉