---
name: "git-release-standards"
description: "Defines Git branching, commit, PR, and release rules. Invoke when setting team Git workflow, preparing PR/merge, or doing a production-style release."
---

# Git 上线规范（团队统一）

## 何时使用
- 需要制定/确认分支策略、提交规范、PR 规范、合并策略时
- 准备把功能从开发分支合并到 main、准备“上线/演示/答辩版本”时
- 发生冲突、回滚、热修复，需要统一操作口径时

## 目标
- 保证 main 分支始终可用、可演示、可回溯
- 降低多人协作冲突与返工
- 让每次合并都有可追踪的需求与变更范围

## 分支策略（Branching）
### 分支命名
- 主分支：main（只接受 PR 合并，不直接 push）
- 前端长期分支：frontend/main
- 后端长期分支：backend/main
- 功能分支：feature/<module>-<short-desc>
- 缺陷分支：fix/<module>-<short-desc>
- 紧急修复：hotfix/<short-desc>

### 开发流转
- 需求/任务从对应长期分支切功能分支：
  - 前端：从 frontend/main 切 feature/*
  - 后端：从 backend/main 切 feature/*
- 功能完成后发 PR 合并回对应长期分支（frontend/main 或 backend/main）
- 前后端联调通过后，再由负责人发 PR 把长期分支合并到 main

## 提交规范（Commit）
### 基本要求
- 一个提交只做一件事（避免“大而全”提交）
- 提交信息必须可读、可回溯，禁止 “update / fix bug / wip” 这种无意义描述

### 推荐格式（简化版 Conventional Commits）
- feat: 新功能
- fix: 缺陷修复
- docs: 文档
- refactor: 重构（不改变功能）
- chore: 构建/工具/配置

示例：
- feat(consult): add consult thread list page
- fix(auth): handle 401 and redirect to login
- docs(prd): refine enums and routing rules

## PR 规范（Pull Request）
### PR 必填项
- 关联需求：写清楚对应 PRD 章节/模块（例如：咨询模块/预约模块/测评模块）
- 变更范围：新增/修改了哪些页面、接口、数据表（可列要点）
- 验收方式：如何本地验证（接口/页面路径/测试用例）
- 风险点：权限/数据兼容/迁移脚本影响

### 合并策略
- 默认 Squash Merge（压缩为一个提交，保证历史干净）
- 禁止强推（force push）到 main 与长期分支

## 发布/上线（Release）
这里的“上线”指：形成一个可演示、可答辩、可回滚的版本快照。

### 上线前检查清单
- main 分支可正常拉取、可正常启动
- 关键流程可演示：登录鉴权、咨询、预约、测评、通知、后台
- 数据库变更有脚本（如有），且可重复执行或有版本管理

### 打标签（Tag）建议
- 里程碑版本：v1.0.0-mvp、v1.0.1-mvp-hotfix1
- 合并到 main 后再打 tag，tag 指向 main 的合并提交

## 回滚与热修复
- 回滚优先使用 revert（生成反向提交），避免 reset 改写历史
- 热修复从 main 切 hotfix/*，修复后 PR 回 main，同时同步 cherry-pick 到 frontend/main 或 backend/main（按影响范围）

## 冲突处理原则
- 冲突先在功能分支解决，不在 main 上解决
- 合并前先 rebase（或 merge）最新目标分支，减少最终 PR 冲突

## 与本项目文档的对齐规则
- PRD：docs/PRD.md
- 前端排期：docs/FRONTEND_SCHEDULE.md
- 后端对齐：docs/BACKEND_SPEC.md
- 文档变更与实现变更要同步：先改文档再改代码，或同 PR 一起提交
