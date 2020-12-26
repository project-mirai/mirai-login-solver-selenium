<div align="center">
   <img width="160" src="http://img.mamoe.net/2020/02/16/a759783b42f72.png" alt="logo"></br>


   <img width="95" src="http://img.mamoe.net/2020/02/16/c4aece361224d.png" alt="title">

----
Mirai 是一个在全平台下运行，提供 QQ 协议支持的高效率机器人库

这个项目的名字来源于
<p><a href = "http://www.kyotoanimation.co.jp/">京都动画</a>作品<a href = "https://zh.moegirl.org/zh-hans/%E5%A2%83%E7%95%8C%E7%9A%84%E5%BD%BC%E6%96%B9">《境界的彼方》</a>的<a href = "https://zh.moegirl.org/zh-hans/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5">栗山未来(Kuriyama <b>Mirai</b>)</a></p>
<p><a href = "https://www.crypton.co.jp/">CRYPTON</a>以<a href = "https://www.crypton.co.jp/miku_eng">初音未来</a>为代表的创作与活动<a href = "https://magicalmirai.com/2019/index_en.html">(Magical <b>Mirai</b>)</a></p>
图标以及形象由画师<a href = "">DazeCake</a>绘制
</div>

# mirai-login-solver-selenium

[ ![Download](https://api.bintray.com/packages/karlatemp/mirai/mirai-login-solver-selenium/images/download.svg) ](https://bintray.com/karlatemp/mirai/mirai-login-solver-selenium/_latestVersion)

该模块负责处理滑动验证码, `mirai-core` 并不强制要求使用 `mirai-login-solver-selenium`

使用时添加该模块至运行时 classpath 即可

## 运行平台支持

| OS      | Browser | 是否支持 |
| ------- | -----   | -----  |
| Windows | Chrome  | Yes    |
| Windows | FireFox | No     |
| Linux   | ------- | No     |
| MacOS   | ------- | No     |

## 手动完成滑动验证码

完成滑动验证码需要 Chrome 扩展插件支持(`下载地址见下文`).
完成扩展安装后添加 JVM 属性 `mirai.slider.captcha.supported` 至 mirai 运行时即可手动验证

## 下载 Chrome 扩展插件

打开 [本链接](https://dl.bintray.com/karlatemp/mirai/net/mamoe/mirai-login-solver-selenium/)
进入最新版本下载 `.crx` 结尾的文件

## 加载 Chrome 扩展插件

把下载的插件从 `.crx` 改名成 `.zip`, 并创建任意一个文件夹解压内容

打开 `chrome://extensions/`, 开启 `开发者模式`, 选择 `加载已解压的扩展程序`

打开 DevTools, 在右上角中的 `More Tools` 找到 `Network conditions`, 将 `User agent` 修改成以下值

```text
Mozilla/5.0 (Linux; Android 7.1.1; MIUI ONEPLUS/A5000_23_17; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045426 Mobile Safari/537.36 V1_AND_SQ_8.3.9_0_TIM_D QQ/3.1.1.2900 NetType/WIFI WebP/0.3.0 Pixel/720 StatusBarHeight/36 SimpleUISwitch/0 QQTheme/1015712
```

## 在 MiraiConsole 中使用

### Download

```shell script
# 注: 自行更换对应版本号

# Download mirai-login-solver-selenium

curl -L https://maven.aliyun.com/repository/public/net/mamoe/mirai-login-solver-selenium/1.0-dev-4/mirai-login-solver-selenium-dev-4-all.jar -o mirai-login-solver-selenium-1.0-dev-4.jar

```

