/**
 * ESLint 10.x Flat Config 配置。
 * 使用 typescript-eslint + eslint-plugin-vue + @stylistic/eslint-plugin 进行 JS/TS/Vue 代码检查。
 */
import globals from 'globals'
import vueParser from 'vue-eslint-parser'
import tsParser from '@typescript-eslint/parser'
import vuePlugin from 'eslint-plugin-vue'
import tsPlugin from '@typescript-eslint/eslint-plugin'
import stylistic from '@stylistic/eslint-plugin'

export default [
  {
    ignores: ['node_modules/**', 'dist/**']  // 忽略目录（构建产物和依赖）
  },
  // Vue 单文件组件解析配置
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tsParser,
        ecmaFeatures: {
          jsx: true
        },
        extraFileExtensions: ['.vue']
      },
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    plugins: {
      vue: vuePlugin
    },
    rules: {}
  },
  // TypeScript 和 JavaScript 文件配置
  {
    files: ['**/*.ts', '**/*.tsx', '**/*.js'],
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module'
      },
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    plugins: {
      '@typescript-eslint': tsPlugin
    },
    rules: {
      // TypeScript 推荐规则，no-unused-vars 委托给 @typescript-eslint 处理
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': 'warn',
      'no-console': 'off'
    }
  },
  // Stylistic 代码风格规则
  {
    plugins: {
      '@stylistic': stylistic
    },
    rules: {
      ...stylistic.configs.recommended.rules,
      // 当前为严控阶段，后续可调松
      '@stylistic/comma-dangle': 'warn',        // 尾逗号风格
      '@stylistic/member-delimiter-style': 'warn',  // 成员分隔符风格
      '@stylistic/eol-last': 'warn',            // 文件末尾换行
      '@stylistic/indent': ['warn', 2],         // 缩进 2 空格
      '@stylistic/quotes': ['warn', 'single'],  // 单引号
      '@stylistic/semi': ['warn', 'never']      // 无分号
    }
  }
]
