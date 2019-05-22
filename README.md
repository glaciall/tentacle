## 基于Java AWT、SpringBoot、websocket、canvas的跨平台远程桌面实现
> 本分支为UDP版本，由于UDP在发包方面有问题，暂时抽不出时间来解决，先从主分支里抽出来

<img src="./additional/tentacle.png" />
<img src="./additional/keyboard.png" />
<img src="./additional/fmanager.png" />

## 模块划分
1. common 公共开发库
2. tentacle-server 服务器端，主入口：`cn.org.hentai.server.app.ServerApp`。
3. tentacle-client 远程主机端（受控端），主入口：`cn.org.hentai.client.app.Tentacle`。

## 使用
1. 修改配置文件的相应项目，比如监听端口，以及`${rds.access.password}`远程控制访问密码
2. 使用`mvn package`编绎打包，服务器端使用`original-tentacle-server-1.5.9.RELEASE.jar`，被控制的主机端使用`tentacle-client-1.0-SNAPSHOT.jar`。
3. 通过`java -jar original-tentacle-server-1.5.9.RELEASE.jar`启动服务器端
4. 在需要被控制的机器上，使用`java -jar tentacle-client-1.0-SNAPSHOT.jar`来启动主机端，注意控制台输出，显示`INFO: Connected to server...`即表示己成功的连接到服务器端。
5. 在浏览器里输入`http://server_ip:server_port/`，输入`${rds.access.password}`开始远程桌面控制。

## 传输协议
### 基础包结构
```
# 协议标识头
48 45 4E 54 41 49       H E N T A I
01                      指令：心跳
00 00 00 05             数据包长度5字节
48 45 4C 4C 4F          H E L L O
```
### 流程

## 画面传输
### 压缩
1. 行程编码，对于大画积的连续的同色区域压缩率相当显著。
2. 通过简易的位运算的方式，对非灰度颜色值（RR == GG == BB），进行与0b111100001111000011110000按位与运算，变相的对颜色进行了有损压缩，但是因为完全保留了灰度色，对于绝大部分屏幕画面影响很小，对于图像影响很明显。
3. 通过对比两祯间的画面，同一位置的颜色值如果相等，则保留透明色，否则则保留新画面祯的颜色值，如此以来，对于变化较小的画面祯，整个画面的数据包，几乎只传输了变化部分的内容（画面不变则不传输数据）。
4. 目前javascript的解压缩实现与后台的java压缩实现，在`Google Chrome`及`Firefox`上表现相当好，很少有单祯画面解压时间超过10毫秒，未来将进一步考虑更加慢的高压缩比算法，进一步的控制流量的消耗。

### 流程

## 鼠标/键盘交互
因为浏览器端脚本不能百分百的拦截所有的组合键，所以不是所有的组合键都能够用于远程控制端，比如`ALT+TAB`的切换窗口（tentacle在窗口失焦时，将释放所有己按下的按键）。
> 注：向远程主机发送CTRL+ALT+DELETE组合键不起作用，估计使用的`java.awt.Robot`类有安全权限控制。

## 剪切板
因为浏览器安全策略上的原因，故不做剪切板的直接访问，只提供了获取与设置远程主机的剪切板的操作界面与功能。

## TODO
1. 压缩率优化
2. 文件上传
3. windows平台系统解锁/登陆

## 系统使用指南
### windows平台
在windows平台上，实际上有两个desktop，一个是正常使用时的desktop，就叫它`workbench desktop`吧，我们的程序可以与之交互，另外一个是专用于登陆/验证的desktop，这个就叫它`logon desktop`吧。
当系统处于未登陆、锁定中或是UAC提示时，`logon desktop`将切换到前台来，在这个情况下，我们的程序是无法进行截屏的，也无法发送按键，控制鼠标等。针对于这个问题，目前己经测试过如下方案：
1. 命令行自动登陆/解锁
    1. [Logon](http://www.softtreetech.com/24x7/archive/51.htm)，只支持windows 7以前的操作系统。
    2. [Logon Expert](https://www.logonexpert.com/)，可以完成自动登陆解锁，但是是收费的。
2. [Mirror Driver](http://www.demoforge.com/dfmirage.htm)，可以截取锁屏界面的画面，但是截取不到登陆输入框。
3. [pGina](http://pgina.org/)，自动登陆方案，好像对系统用户有不可知的限制或要求，暂不可行。
4. 其它命令行截屏工具，对于锁屏界面是全线溃败，没有一个能打的。。。

目前来说，windows平台的远程桌面比较苦逼，还在想办法，本项目短期内不会继续更新，找到解决方案了再说。
比较可行的办法是，设置系统用户[自动登陆](https://zhidao.baidu.com/question/118873767.html)，然后在设置屏幕保护界面不要勾选“**在恢复时显示登陆屏幕**”，然后就可以一直用了。

### linux平台
linux平台对扩展开发比较友好（或者是我比较了解linux的缘故吧），登陆界面上没有像windows那样的限制，比较好弄。
如果要在命令行模式下运行，或是想要设置自启动，有可能会碰到`java.awt.AWTException: headless environment`报错，这是因为缺少`$DISPLAY`环境变量所导致的`java awt`找不到相应桌面，进而无法截屏或控制鼠标，解决方法如下：
1. 确定`$DISPLAY`变量的值，可以通过在桌面环境下，打开`终端terminal`，输入`echo $DISPLAY`来查看，一般不是`:0`就是`:1`
2. 设置`$DISPLAY`环境变量，执行`export DISPLAY=:0`，你可以忽略掉第1步，直接使用`:0`或`:1`试试。
3. 这个时候就可以正常启动`tentacle-client`了

## 诚证合作小伙伴
目前在如下两个方面上尚有不足之处，有兴趣的小伙伴可以留言或加QQ65827536联系我。
1. windows用户解锁
2. 压缩率优化
