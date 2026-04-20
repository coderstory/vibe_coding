## 纯AI编程产出的后台管理系统

> 这是一个基于spring boot 4和 VUE3 的后端管理系统。
> 
> 目前使用minimax官方的2.7模块，配合opencode客户端开发。
> 
> 主要使用[superpower](https://github.com/obra/superpowers)和[GSD](https://github.com/gsd-2/get-shit-done)技能链完成需求分析到功能验收的完整闭环
> 
> 当前使用`Trae CN`作为`IDE`

## 开发目的

1. 学习vibe coding
2. 学习主流编程 agent
3. 学习skill mcp rule 能概念和实际的功能开发
4. 学习如何解决编程过程中的各种问题，积累实战经验


## 开发流程 （基于GSD的方案）
1. 提出问题,初步分析 `/gsd:new-project`
2. 拆解问题，讨论方案，产出多个需求 `/gsd:discuss-phase N`
3. 针对单个需求，详细设计 `/gsd:plan-phase N`
4. 针对单个需求，执行编码 `/gsd:execute-phase N`
5. 针对每个模块，功能测试 `/gsd:verify-work N`
6. 完成全部需求开发和验证，结束本轮开发 `/gsd:complete-milestone`
7. 开始新的一轮 `/gsd:new-milestone`

## 分析复杂的场景
1. 使用 **/brainstorming**进行头脑风暴
 
## 中文问题
1. agents.md里已经写了相应规则，部分场景能输出中文
2. 目前使用的skill都是英文的导致，导致和skill内容强相关的都是输出英文，可以直接汉化，但是后续更新后又会变成英文

## 缺陷
1. 目前opencode没法接管日志输出，看不到异常，导致需要手动处理（让他跑程序会卡死，看上去是在等命令完成输出，但明显是不可能的）
2. 缺少broswer-use 技能

## 数据库更新
1. 后端装了flyway框架，让他数据库变更生成对应的SQL文件，springboot启动的时候自动执行

## 直接访问数据
1. 让agent有直接访问数据库能力，方便分析一些问题，直接使用，配置在opencode.json里，自己改下地址和账号

## 技能
1. GSD 是直接安装到了.opencode目录下 `npx gsd-opencode@latest`
2. superpower 汉化版本 `npx superpowers-zh`
3. 浏览器访问能力 `npm install -g @playwright/cli@latest` `playwright-cli install --skills`
## 特殊说明
1. 配置npm镜像仓库，避免下载插件失败 `npm config set registry https://registry.npmmirror.com`
2. [国内的一个技能仓库](https://skillhub.cn/)
