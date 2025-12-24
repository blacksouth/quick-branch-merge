# Quick Branch Merge - IntelliJ IDEA 插件

一键将当前分支合并到指定目标分支的便捷工具。

---

## ✨ 功能特性

| 功能       | 说明                                        |
| ---------- | ------------------------------------------- |
| 🔄 一键合并 | 自动执行完整的 Git 合并流程                 |
| 📝 自动提交 | 检测未提交更改，弹窗提示输入 commit message |
| 📤 自动推送 | 先推送当前分支到远程，再合并到目标分支      |
| 🔍 变更检测 | 无更改时自动提示"无需操作"                  |
| 📋 实时日志 | 在底部控制台实时显示命令执行结果            |
| ⚙️ 可配置   | 支持自定义目标分支，默认为 dev              |

---

## 📦 安装方法

**支持 IDEA 版本**：2023.3 ~ 2025.3（build 233 ~ 253）

1. 打开 IntelliJ IDEA
2. **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. 选择 `quick-branch-merge-1.0.0.zip`
4. 重启 IDEA

---

## ⚙️ 配置目标分支

**Settings** → **Tools** → **Quick Branch Merge**

可配置合并的目标分支，默认为 `dev`

<img width="1801" height="1018" alt="QQ_1766548374725" src="https://github.com/user-attachments/assets/64095480-9d55-4de2-a692-7dafad895d51" />


---

## 🚀 使用方法

### 右键菜单入口

在编辑器或项目文件中右键 → **Quick Merge**

<img width="653" height="514" alt="97b3cbcc-c67a-49ce-850e-861437274a88" src="https://github.com/user-attachments/assets/cef61bdf-5334-4435-b302-a7ed4a7cc0e7" />


### Git 菜单入口

顶部菜单 **Git** → **Quick Merge**

<img width="608" height="591" alt="ec5c5efa-8f46-4728-bc66-611c3fa698a0" src="https://github.com/user-attachments/assets/a02a93a9-ac1b-4aa2-b527-da6c72b48da5" />


---

## 📋 实时日志控制台

执行时底部会打开 "**Quick Merge**" 控制台，实时显示每个命令执行结果：

<img width="1784" height="811" alt="QQ_1766548465551" src="https://github.com/user-attachments/assets/ef3e5a64-005f-4e65-aa95-cdfa7784e709" />


---

## 🔄 执行流程

| 步骤 | 命令                        | 说明                                  |
| ---- | --------------------------- | ------------------------------------- |
| 1    | 检查更改                    | 检测未提交更改 + 本地领先远程的提交数 |
| 2    | `git add . && commit`       | 提交所有更改（如有）                  |
| 3    | `git push origin 当前分支`  | 推送当前分支到远程                    |
| 4    | `git fetch origin 目标分支` | 获取远程目标分支                      |
| 5    | `git checkout 目标分支`     | 切换到目标分支                        |
| 6    | `git pull origin 目标分支`  | 拉取最新代码                          |
| 7    | `git merge 当前分支`        | 合并当前分支                          |
| 8    | `git push origin 目标分支`  | 推送合并结果                          |
| 9    | `git checkout 当前分支`     | 切回原分支                            |
