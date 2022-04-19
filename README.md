# GiteeTodo - 码云待办

GiteeTodo 是一款基于 compose + viewmodel + Retrofit 使用 Gitee（码云）的 issue 作为储存仓库的 TODO 应用。 

## 使用方法

### 常规用法
由于本程序基于 Gitee 的 issue 系统，所以推荐的正确姿势是使用本应用来管理你的 Gitee 仓库 issue，方便随时新建、查看、修改 issue。

### 其他用法
还有一个比较 *“异端”* 的用法是将本程序作为一个纯粹的 TODO 程序，新建或使用你已有的 Gitee 仓库作为储存库，然后将你的 TODO 事项写入其中，一个完善的 TODO 应用应有的功能特性，本程序都有。 

## 如何登录
目前不提供离线使用，使用时必须联网，且为了保证正常使用，需要授权登录码云账户。

### 登录方式
我们推荐使用第 2 或 第 3 种登录方式，非必要不推荐使用第 1 种方式。

本程序绝对不会滥用用户授权的权限，仅使用本程序提到的功能，也不会读取或修改用户的其他任何信息。

如果不放心，欢迎查看源码或自行使用源码编译使用。

1. 账号密码登录
直接使用码云账号密码登录，使用时会将你的账号密码加密后离线储存在你的设备上，我们不会将该账号密码明文储存也不会将该密码暴露在互联网中（除登录获取 Token 外）；储存账号密码是因为 Gitee 获取的 Token 有效期只有一天， Token 过期后必须重新获取。
   
**我们不推荐使用该登录方式**

2. 私人令牌登录
登录码云（必须是桌面版）后，依次点击 右上角头像 - 设置 - 安全设置 - 私人令牌 - 生成新令牌 - 勾选所需要的权限（`user_info projects issues notes`） - 提交即可。
   
使用私人令牌的优势： 不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权。


3. OAuth2 授权登录
使用码云官方 OAuth2 认证授权。
   
使用 OAuth2 的优势：不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权、token有效期只有一天。