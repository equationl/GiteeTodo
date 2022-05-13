# GiteeTodo - 码云待办

## 简介
GiteeTodo 是一款基于 compose + viewmodel + Retrofit 实现的 MVI 架构 TODO 应用；使用 Gitee（码云）的 issue 作为储存仓库。

### 主要功能
- 查看仓库列表（仅获取类型为 *个人* ，且登录账号为 *创建者* 的仓库）
- 新建仓库
- 根据仓库查看 ISSUE 列表（支持筛选）
- 新建 ISSUE
- 快速标记 ISSUE 完成状态
- 查看某个 ISSUE 详情
- 编辑某个 ISSUE 详情
- 支持标签（label）管理
- 支持查看、新建、编辑、删除 ISSUE 评论
- 适配深色模式

### 截图
| ![1](./docs/img/Screenshot_1.png) | ![2](./docs/img/Screenshot_2.png) |
| --------------------------------- | --------------------------------- |
| ![3](./docs/img/Screenshot_3.png) | ![4](./docs/img/Screenshot_4.png) |
| ![5](./docs/img/Screenshot_5.png) | ![6](./docs/img/Screenshot_6.png) |

### 项目结构图
![包结构](./docs/img/pkg.png)

![架构](./docs/img/framework.png)

### 使用的技术栈及第三方库
*基本架构： [MVI](https://juejin.cn/post/7048980213811642382)*

- viewmodel： ViewModel 类旨在以注重生命周期的方式存储和管理界面相关的数据。
- Navigation-Animation： 为 Navigation 提供动画支持的库。
- paging： 使用 Paging 库，您可以更轻松地在应用的 RecyclerView 中逐步妥善地加载数据。
- retrofit2：网络请求库。
- datastore： 以异步、一致的事务方式存储数据，克服了 SharedPreferences 的一些缺点。
- coil：由 Kotlin Coroutines 支持的 Android 图像加载库。
- Pager：一个用于在 Jetpack Compose 中构建分页布局的库，类似于 ViewPager。
- compose-richtext： 用于在 compose 中显示富文本（包括 markdown）。
- lottie： 在 Android 、 iOS、 Web 和 React Native 上原生渲染 After Effects 动画。
- compose-material-dialogs： Jetpack Compose 的MD对话框。

### 下载
APK： *待补充*

国内源码镜像：[https://gitee.com/equation/GiteeTodo](https://gitee.com/equation/GiteeTodo)

## 使用方法

### 编译运行
首先你需要有自己的 client_id 和  client_secret。

请前往Gitee [注册](https://gitee.com/oauth/applications/new) 获取（详情请查阅 [创建应用流程](https://gitee.com/api/v5/oauth_doc#list-item-3)）。

注册信息请根据自己情况随意填写，但是**应用回调地址必须填写** `giteetodoapp://authed` 否则将无法登录。

然后在项目根目录下的 `local.properties` 文件中写入你创建的 id 和 密钥:
```
CLIENT_ID = "xxxxxx"
CLIENT_SECRET = "xxxxxx"
```

最后按照正常安卓程序的编译流程编译即可。

### 常规用法
由于本程序基于 Gitee 的 issue 系统，所以推荐的正确姿势是使用本应用来管理你的 Gitee 仓库 issue，方便随时新建、查看、修改 issue。

### 其他用法
还有一个比较 *“异端”* 的用法是将本程序作为一个纯粹的 TODO 程序，新建或使用你已有的 Gitee 仓库作为储存库，然后将你的 TODO 事项写入其中，一个完善的 TODO 应用应有的功能特性，本程序都有。 

## 如何登录
目前不提供离线使用，使用时必须联网，且为了保证正常使用，需要授权登录码云账户。

### 登录方式
本程序支持以下三种登录方式：

1. 账号密码登录
2. 私人令牌登录
3. OAuth2 授权登录
        
我们推荐使用第 2 或 第 3 种方式登录，不建议使用第 1 种方式登录。

#### 账号密码登录
直接使用码云账号密码登录，我们不会储存你的账号密码，该账号密码仅用于当前获取 token 这一过程，使用完毕会立即销毁。  

**我们不推荐使用该登录方式**

#### 私人令牌登录

登录码云（必须是桌面版）后，依次点击 右上角头像 - 设置 - 安全设置 - 私人令牌 - 生成新令牌 - 勾选所需要的权限（`user_info projects issues notes`） - 提交即可。
   
*使用私人令牌的优势： 不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权。*

#### OAuth2 授权登录

使用码云官方 OAuth2 认证授权。
   
*使用 OAuth2 的优势：不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权、token有效期只有一天。*

**注意**：无论使用什么方式登录，我们都会储存你的授权 Token ， 便于下次直接登录。

该 Token 你随时可以在 Gitee（码云）个人设置中查看并取消授权或删除、修改。

本程序绝对不会滥用用户授权的权限，仅使用本程序提到的功能，也不会读取或修改用户的其他任何信息。

如果不放心，欢迎查看源码或自行使用源码编译使用。

## 关于
由于我是第一次接触 MVI 架构，所以我也不确定这个程序是否符合 MVI 规范，如有错误，还望海涵并希望大佬们能不吝赐教。

该程序使用 API 来自于 Gitee 的 [OpenAPI](https://gitee.com/api/v5/swagger) 。

**请勿滥用 Gitee 资源。**

该程序在撰写过程中参照了 [shenzhen2017](https://github.com/shenzhen2017) 的 [wanandroid-compose](https://github.com/shenzhen2017/wanandroid-compose)


### 联系我
email: admin@likehide.com

website: [www.likehide.com](http://www.likehide.com)

其他APP：[app.likehide.com](http://app.likehide.com)