// 引入 Node.js 内置模块 path，用于处理文件路径
const path = require('path');
// 引入 React Native 提供的默认 Metro 配置方法
const { getDefaultConfig } = require('@react-native/metro-config');

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
module.exports = (async () => {
  // 动态导入 react-native-monorepo-config，用于 monorepo 环境的 Metro 配置增强
  const { withMetroConfig } = await import('react-native-monorepo-config');
  // 获取当前目录下的默认配置
  const root = path.resolve(__dirname, '..');
  // 获取当前目录下的默认配置
  const defaultConfig = getDefaultConfig(__dirname);
  // 使用 withMetroConfig 结合默认配置与 monorepo 配置进行合并
  const config = withMetroConfig(defaultConfig, {
    root,
    dirname: __dirname,
  });
  // ⚠️ 启用 Metro 对 package.json 中 "exports" 字段的支持（实验性）
  // 这个字段可以更灵活地指定模块的导出方式，适用于 ESM/CJS 等现代包
  // config.resolver.unstable_enablePackageExports = true;
  return config;
})();
