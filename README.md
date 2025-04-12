ThePitMeltdown
===========

> 一个致力于添加更多功能的 [ThePitPremium](https://github.com/ThePitCommunity/ThePitPremium) 分支。

## 如何使用 (服务器管理员)
你可以从 [此处](https://github.com/AstralStudio/ThePitMeltdown/releases/latest) 下载 ThePitMeltdown 的最新构建版本。

也可以通过 [此处](#构建此插件) 的构建指南自行构建。

## 如何使用 (第三方插件开发者)
ThePitMeltdown-API:

如果你要将 ThePitMeltdown 作为依赖,那么你必须进行 [自行构建](#构建此插件)。

## 构建此插件

你需要最低 JDK 11 和一个可以正常访问各种 git/maven 库的网络。

克隆此代码库, 在您的IDE里对此项目通过Gradle进行编译，即Tasks中的shadowJar。

随后,你可以在 `./build/libs` 找到构建完成的插件。

## 联系我们

请添加QQ群773449670以与我们取得联系。

### 特别感谢

##### [BaiJiuSama](https://github.com/BaiJiuSama)
##### [Starry_Killer](https://github.com/AstralStalyer)

### 关于部分代码说明
同步了部分在项目库 "ThePitAlternative" 中经过修改的附魔属性。

原 ThePitAddon - BaiJiuSama 的部分附魔, 现已移动至 cn.charlotte.pit.enchantment.type.addon 包下。

原 ThePitAlternative - Starry_Killer 的部分附魔, 现已移动至 cn.charlotte.pit.enchantment.type.alternative 包下。

ThePitAlternative 中与 ThePitAddon 产生冲突的附魔, 将保留附魔至 alternative 包下。


### 疑问解答
Q: 这与 ThePitPremium, 来自MrDarkNess462/Mher Zaqaryan的 ThePit 有何区别?

A: 好吧, 本质上, 这个项目是一个基于 ThePitPremium 的分支, 其继承了来自 ThePitPremium 的功能, 并添加了更多的内容, 并且我们正在尽力优化其损耗的性能。

Q: 与其他 ThePitPremium 分支的不同?

A: 因为此项目处于开源状态, 因此您可以将代码下载至您的电脑, 由您自己进行修改, 并且我们正在最大程度上保持代码与原项目相同, 并且, 我们断定此项目的生命周期将会比预期来的更久。

Q: 代码库会于何时转为私人状态?

A: 此代码库将永远不会被转为私人状态, 除非我们的授权被原制作组收回。

Q: 我不清楚要如何编译此插件

A: 我们在每个大型更新时会发布版本, 其包含一个jar文件, 您可以通过Release页面直接下载jar文件使用。

Q: 我想要为项目贡献代码

A: 我们欢迎所有人对此项目贡献代码, 当您联系开发组并说明来意, 我们将会处理您的请求。

### 「代码的生命不在于商业存活，而在于技术精神的延续」
###### 此代码库遵循GPL v3开源协议。
