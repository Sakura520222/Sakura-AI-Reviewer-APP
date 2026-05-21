# 项目概述文档

## 1. 项目简介
Sakura-AI-Reviewer-APP 是一个基于 Android 的 AI 审查助手应用，旨在通过智能技术提供应用审查和分析功能。

## 2. 技术栈
- **主要语言**：Kotlin（项目代码主要使用 Kotlin 编写，总计约 426,071 行代码）
- **构建工具**：Gradle（使用 Gradle Kotlin DSL，包括 build.gradle.kts 和 settings.gradle.kts）
- **依赖管理**：通过 gradle/libs.versions.toml 管理库版本
- **代码混淆**：配置了 ProGuard 规则（proguard-rules.pro）
- **环境配置**：使用 gradle.properties 和 local.properties.template 进行项目配置

## 3. 项目结构
- **app/**：应用核心模块，包含应用的主代码、构建配置（build.gradle.kts）和混淆规则。
  - src/：源代码目录，具体结构未详细列出，但通常包含 Kotlin 源文件。
- **docs/**：文档目录，包含 API 参考（api-v1-reference.md）和开发进度记录（progress.md）。
- **gradle/**：Gradle 配置目录，包括版本管理（libs.versions.toml）和包装器（wrapper/）。
- **根目录文件**：包括 Git 忽略文件（.gitignore）、Gradle 包装器脚本（gradlew 和 gradlew.bat）以及项目设置文件（settings.gradle.kts）。

## 4. 开发约定
- **构建配置**：项目使用 Gradle Kotlin DSL（.kts 文件）进行构建配置，表明采用现代 Kotlin 构建实践。
- **依赖版本管理**：通过 libs.versions.toml 统一管理依赖库版本，确保一致性。
- **代码保护**：配置了 ProGuard 混淆规则，可能用于发布版本以保护代码。
- **文档维护**：设有专门的文档目录，记录 API 参考和开发进度，体现文档驱动的开发习惯。
- **环境隔离**：提供 local.properties.template 模板，避免将敏感配置提交到版本控制，增强安全性。