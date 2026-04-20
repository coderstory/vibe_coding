---
name: write-file
description: 写入文件内容，替代 agent 内置的 write tool
---

<objective>
将指定内容写入到文件中，支持创建父目录。
</objective>

<execution_context>
@./.opencode/skills/write-file/write_file.py
</execution_context>

<process>
使用 Python 脚本写入文件：

**命令格式：**
```
python .opencode/skills/write-file/write_file.py "<file_path>" "<content>" [--create-dirs]
```

**参数说明：**
- `file_path`: 目标文件路径（绝对或相对路径）
- `content`: 要写入的内容
- `--create-dirs`: 可选，创建父目录如果不存在

**使用场景：**
- 当用户要求写入文件内容时使用
- 自动处理 UTF-8 编码
- 支持创建多层嵌套目录

**返回结果：**
- success: 布尔值表示是否成功
- file: 写入文件的绝对路径
- bytes: 写入字节数
- error: 失败时的错误信息
</process>

**示例：**

写入简单文件：
```
python .opencode/skills/write-file/write_file.py "output.txt" "Hello World"
```

写入并创建目录：
```
python .opencode/skills/write-file/write_file.py "src/components/Button.vue" "<template>..." --create-dirs
```
