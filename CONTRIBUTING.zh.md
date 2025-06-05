好的，以下是**保持原文格式完全一致**的中文翻译版本：

---

# 贡献指南

无论贡献大小，我们都非常欢迎！

我们希望这个社区是友善且相互尊重的，请在与项目的所有互动中保持这一点。在贡献之前，请阅读我们的 [行为准则](./CODE_OF_CONDUCT.md)。

## 开发工作流

该项目是一个使用 [Yarn workspaces](https://yarnpkg.com/features/workspaces) 管理的 monorepo，包含以下包：

- 根目录下的库包。
- `example/` 目录下的示例应用。

要开始项目开发，请在根目录运行 `yarn` 安装各个包所需的依赖：

```sh
yarn
```

> 由于项目依赖 Yarn workspaces，**请不要使用 [`npm`](https://github.com/npm/cli)** 进行开发。

[示例应用](/example/) 展示了该库的使用方式。你需要运行它来测试你所做的任何更改。

它已配置为使用本地版本的库，因此你对库源码的更改会直接反映在示例应用中。对 JavaScript 代码的更改无需重建即可生效，而原生代码的更改则需要重新构建示例应用。

如果你希望使用 Android Studio 或 XCode 编辑原生代码，可以分别打开 `example/android` 或 `example/ios` 目录。若要编辑 Objective-C 或 Swift 文件，请在 XCode 中打开 `example/ios/ImagePickerExample.xcworkspace`，并在 `Pods > Development Pods > react-native-image-picker` 中找到源码文件。

若要编辑 Java 或 Kotlin 文件，请在 Android Studio 中打开 `example/android`，在 `react-native-image-picker` 模块下的 `Android` 中找到源码文件。

你可以在根目录使用以下命令进行开发：

启动打包器：

```sh
yarn example start
```

在 Android 上运行示例应用：

```sh
yarn example android
```

在 iOS 上运行示例应用：

```sh
yarn example ios
```

要确认应用是否在新架构下运行，你可以在 Metro 日志中查找如下信息：

```sh
Running "ImagePickerExample" with {"fabric":true,"initialProps":{"concurrentRoot":true},"rootTag":1}
```

注意 `"fabric":true` 和 `"concurrentRoot":true` 这两个属性。

确保你的代码通过 TypeScript 和 ESLint 检查。运行以下命令进行验证：

```sh
yarn typecheck
yarn lint
```

修复格式错误：

```sh
yarn lint --fix
```

请尽可能为你的更改添加测试。运行单元测试：

```sh
yarn test
```

### 提交信息规范

我们遵循 [Conventional Commits](https://www.conventionalcommits.org/en) 的提交信息规范：

- `fix`: 修复 bug，例如修复由于已废弃方法导致的崩溃。
- `feat`: 新功能，例如为模块添加新方法。
- `refactor`: 重构代码，例如将类组件迁移为 hooks。
- `docs`: 文档修改，例如为模块添加使用示例。
- `test`: 添加或更新测试，例如使用 detox 添加集成测试。
- `chore`: 工具变更，例如修改 CI 配置。

我们配置了 pre-commit 钩子来验证你的提交信息是否符合该格式。

### Lint 和测试

我们使用 [TypeScript](https://www.typescriptlang.org/) 进行类型检查，使用 [ESLint](https://eslint.org/) 配合 [Prettier](https://prettier.io/) 进行代码风格检查与格式化，使用 [Jest](https://jestjs.io/) 进行测试。

pre-commit 钩子也会在你提交时检查 linter 和测试是否通过。

### 发布到 npm

我们使用 [release-it](https://github.com/release-it/release-it) 来简化版本发布流程。它会处理常见任务，如根据 semver 升级版本、创建标签和发布记录等。

发布新版本，请运行：

```sh
yarn release
```

### 脚本

`package.json` 文件中包含了用于常见任务的脚本：

- `yarn`: 安装项目依赖。
- `yarn typecheck`: 使用 TypeScript 进行类型检查。
- `yarn lint`: 使用 ESLint 检查代码。
- `yarn test`: 使用 Jest 运行单元测试。
- `yarn example start`: 启动示例应用的 Metro 服务。
- `yarn example android`: 在 Android 上运行示例应用。
- `yarn example ios`: 在 iOS 上运行示例应用。

### 提交 Pull Request

> **第一次提交 PR 吗？** 你可以通过这个免费的系列教程学习：[如何为 GitHub 上的开源项目贡献代码](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github)

当你提交 Pull Request 时：

- 尽量保持 PR 小而专注于一个变更点。
- 确保所有 linter 和测试都通过。
- 审查文档是否完整并正确。
- 打开 PR 时遵循模板格式。
- 若 PR 改动了 API 或底层实现，请先通过 issue 与维护者沟通。
