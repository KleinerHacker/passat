<div class="passat-hero" markdown>
![Passat](assets/images/icon.png){ alt="Passat" }
</div>

# Passat

<p class="passat-tagline">基于 IntelliJ 平台构建的开放平台 Object Pascal IDE。</p>

Passat 为 IntelliJ 平台带来一流的 **Object Pascal** 支持。它构建在**免费的 Object Pascal 编译器**之上，
以 **Free Pascal 编译器（FPC）**作为参考工具链。编译器与语言版本的处理经过抽象，因此日后可以接入
其他免费的 Object Pascal 编译器。

Passat 采用**插件优先**的开发方式：它以插件形式发布，可安装到现有的 JetBrains IDE 中；在后续阶段，
还会基于同一套代码库打包出**独立的 IDE** 发行版。

## 功能特性

- **专属的 Pascal 项目与模块类型** —— 如同 Java 和 Kotlin 模块一样，每个 Pascal 模块都包含目标编译器
  （注册为 SDK 的 FPC 安装）、目标语言版本（Object Pascal 方言级别）以及各自的依赖列表。
- **完整的语言支持** —— 词法分析器与语法分析器生成 PSI 树，提供语法高亮、代码补全、导航（跳转到声明 /
  查找用法）、结构视图、重构和代码检查。
- **构建与运行** —— 通过专用的运行配置，针对所配置的编译器编译并运行你的项目。
- **调试** —— 完整的 IDE 调试能力：断点、单步执行、变量查看与表达式求值。
- **双重交付** —— 一套代码库，两种产品：既是可安装到现有 JetBrains IDE 的插件，也是独立的 Passat IDE。

## 路线图

Passat 分阶段构建：

1. **语言解析器与 PSI** —— 词法分析器、语法、PSI 以及基础高亮。
2. **项目 / 模块模型** —— 带有编译器 SDK、语言版本和依赖的 Pascal 模块类型。
3. **构建与运行** —— 通过 FPC 编译并执行；运行配置。
4. **调试** —— 完整的调试功能集。
5. **独立 IDE 打包** —— 在插件之外，将 Passat 作为独立 IDE 发布。
