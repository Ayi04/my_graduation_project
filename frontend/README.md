# 智能图书馆座位预约系统前端

## 项目简介

本项目是基于Vue 3 + Element Plus构建的智能图书馆座位预约系统前端，与Spring Boot后端配合使用，实现了图书馆座位的预约、管理和统计功能。

## 技术栈

- **框架**：Vue 3 + Vite
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **UI组件库**：Element Plus
- **图表库**：ECharts

## 功能模块

1. **登录与注册**：支持三角色登录（普通用户、工作人员、管理员）
2. **首页**：展示座位推荐、热力图、公告等
3. **座位管理**：浏览和预约座位
4. **预约管理**：查看和管理预约记录，支持签到和结束使用
5. **个人中心**：管理个人信息和修改密码
6. **反馈与投诉**：提交意见和投诉
7. **管理员面板**：管理用户、工作人员、数据统计和系统设置

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── assets/          # 资源文件
│   ├── components/      # 组件
│   ├── views/           # 页面
│   ├── router/          # 路由
│   ├── store/           # 状态管理
│   ├── services/        # API服务
│   ├── utils/           # 工具函数
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── package.json         # 依赖配置
├── vite.config.js       # Vue配置
└── index.html           # HTML入口
```

## 安装与运行

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

### 生产环境构建

```bash
npm run build
```

## 后端API接口

前端项目通过以下API与后端通信：

- **用户相关**：/api/user/*
- **座位相关**：/api/seat/*
- **预约相关**：/api/reservation/*
- **反馈相关**：/api/feedback/*
- **公告相关**：/api/notice/*

## 注意事项

1. 确保后端服务已经启动，默认端口为8080
2. 前端开发服务器默认端口为3000
3. 首次登录可以使用以下测试账号：
   - 管理员：admin/admin123
   - 工作人员：staff/staff123
   - 普通用户：user1/user123
