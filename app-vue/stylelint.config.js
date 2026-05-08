// Stylelint 17.x 配置 — CSS 样式检查规则
export default {
  extends: ['stylelint-config-standard'],  // 基础配置
  rules: {
    'at-rule-no-unknown': true,                    // 禁止未知 at-rule（如拼写错误）
    'block-no-empty': true,                        // 禁止空样式块
    'color-no-invalid-hex': true,                  // 禁止无效十六进制颜色值
    'custom-property-empty-line-before': null,      // 允许自定义属性前无空行
    'color-function-notation': null,                // 接受两种颜色函数表示法
    'alpha-value-notation': null,                   // 接受数字和百分比两种透明度表示
    'color-function-alias-notation': null,           // 接受两种颜色函数别名表示
    'color-hex-length': null,                       // 不限制十六进制颜色长度
    'declaration-block-single-line-max-declarations': null,  // 不限制单行声明数
    'at-rule-prelude-no-invalid': null,              // 允许非标准 at-rule
    'declaration-block-no-shorthand-property-overrides': null,  // 不禁用手写属性覆盖
    'no-descending-specificity': null,               // 不强制选择器特异性降序
    'selector-class-pattern': null,                  // 允许自定义 class 命名（项目使用 kebab-case + BEM 混合）
    'keyframes-name-pattern': null,                  // 允许自定义动画命名
    'no-duplicate-selectors': null                   // 允许选择器重复（Vue scoped 样式常见）
  }
}
