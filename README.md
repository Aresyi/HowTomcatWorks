# HowTomcatWorks
《How Tomcat Works》 every chapter demo .
<br/>
Here's my ebook：https://yuedu.baidu.com/ebook/ac92f0d35122aaea998fcc22bcd126fff7055d60
<br/>
Part of the UML diagram is as follows.
<br/>
## 1.The default connector class diagram:
![4.1](https://raw.githubusercontent.com/Aresyi/HowTomcatWorks/master/doc/pic/ex4.1.jpg)
<br/>
## 2.The class diagram of Container and its related types:
![5.1](https://raw.githubusercontent.com/Aresyi/HowTomcatWorks/master/doc/pic/ex5.1.jpg)

### 在路上
2006年的暑假，抱着早点接触社会的心态，从合肥坐24小时绿皮车跑到广州打暑假工。当时感触到：学电气自动化的似乎天天要和那些油乎乎的机械打交道；哪一天如果自己想干点什么，初期投入买个设备都要很多钱。
广州回来后，一天下午我在家里菜园马路边的草丛中捡了一部摩托罗拉手机。手机里面的游戏启动屏是冒着热气的茶杯，还有着`JAVA`4个字母，第一眼就甚是喜欢那个图案——与君初相识，犹如故人归。

2007年，一次在同学的宿舍里发现一本封面带有`Java`字样的书，煞有一种“这妹妹我曾见过”的感觉。后来问单片机老师：从事软件好，还是硬件好。老师推荐走软件路线。如果自己想干点什么，有台电脑就可以了~~也这就这样，我选择了偏软件，选择了似有一见钟情的Java切入。
那会觉得Java比C有意思多了：C天天只会在DOS下面打印输出，一点也没Java可以用Swing做个贪吃蛇、做个象棋好玩。

2008年，全球金融危机，多数公司裁员，不招人。记得，一次房东要卖阳台上的矿泉水瓶，人家说：“别说卖了，就是送给我，我也不要！小工厂倒闭的多，没人收了！”。苦苦寻觅、等待，最终通过Java找到饭碗！甭说多爱死Java了！从我当年注册CSDN的ID可见[LoveJavaYDJ](http://blog.csdn.net/LoveJavaYDJ)。如今看，觉得多俗气O(∩_∩)O哈！

2009年，工作以来，一直默默用Java使用着Tomcat做着Web开发，慢慢慢慢觉得好无趣：天天就是Action -> Service -> Dao流水线式业务开发。一时以为Java只能做做Web开发，还觉得学Java没出息。还是C或C++牛掰，你看我们Java程序员整天
用着他们用C或C++写的Tomcat~

2010年，偶尔一次在网上发现《How tomcat works》这本英文电子书，当时还没中文版实物书卖。我靠，原来Tomcat是用Java写的！原来是自己的无知，是自己的功力太浅！然后就满血复活般地啃噬一把。

2013年，我参考Tomcat体系结构，使用`Netty`开发搭建了公司使用的[PushServer](https://github.com/Aresyi/pushServer)推送服务器。算是个人开发的第一个纯Java而非Web的中间件~当时还挺有成就感。

在公司面试中，遇到培训速成出来或工作2/3年的对Servlet规范、HTTP协议、HTTP请求过程这些基础几乎都不了解；也遇到过不乏有很多使用Java五六年以上的同学，在谈及很熟悉Tomcat时，往往也只是停留在基本配置，调调内存参数上面。
加之，QQ群或社区中也经常有同学问如何学习设计模式，如何看源码（其实，我在学校时也问过老师，上班后也请教过老大）。我想到了自己学习看的《How tomcat works》，所以整理推荐给大家（推荐看[原版](http://www.brainysoftware.com/)）。
Tomcat中有不少设计模式值得借鉴学习，如门面、观察者。还比如，作为Servlet程序员都知道Filter，但我们知道Filter在Tomcat的哪一环节出现当然更好了。

那，Tomcat4/5版本老吗？是老，但，我不觉得这会影响我们学习、演练。因为我们应该先从大处着手——先学习方法。再者，Tomcat大的体系架构基本没变，如共有4种容器：Engine（引擎）、Host（主机）、Context（上下文）和 Wrapper（包装器）。
后面版本更多是优化如NIO、线程池，以及遵循新的HTTP协议和Servlet规范。 

我觉得：写代码，犹如我们学习写文章一样，想写出好的文章，那就得先学习、分析已经存在的好文章——先积累输入再会有良好地输出。共勉！




### 本书简介
本书以Tomcat4和5为基础，从最基本的HTTP请求和一个最简单的web服务器例子开始，循序渐进，分解介绍Tomcat中各个容器和组件
如Engine、Host、Context和Wrapper。另，还详细介绍了如何管理Session，以及如何在分布式环境下扩展Session；Tomcat如何处理配制文件server.xml，
以及又是如何通过Digester库将XML元素转换Java对象；如何通过使用"关闭钩子"Runtime.getRuntime().addShutdownHook()优雅地停止服务器。层层深入直至使用JMX技术管理Tomcat中的应用程序。

- 第1~2章，首先对HTTP协议和Socket通讯以及Servlet规范做了介绍，然后编写演示了一简单的Servlet容器。
- 第3~4章，改进前章节中的Demo，详细介绍了Tomcat中默认连接器（Connector）的实现，讲解了如何创建请求链接，以及Tomcat中如何解析请求，如HTTP请求头Header、Cookie等。
- 第5章，系统介绍了容器(Container)，容器是一用来处理servlet请求并填充返回对象给web客户端的模块。共有4种容器：引擎（Engine）, 主机（Host）, 上下文（Context）,包装器（Wrapper）。本章先介绍了Context和Wrapper。
- 第6~10章，介绍了Tomcat中五个重要模块组件：生命周期管理、日志系统、类加载机制、会话管理、安全控制。
- 第11~13章，依次详细介绍了Tomcat中核心容器标准实现：Wrapper、Context、Host、Engine。
- 第14章，介绍了容器之上的另两个重要的管理组件Server和Service，展示了如何配置多个连接器（Connector），如何优雅地启动和停止Servlet容器。
- 第15章，展示了Tomcat中如何借助Digester避免硬编码，使其可灵活配置。
- 第16~17章，讲解Tomcat的启动和关闭机制，介绍了Shell和Bat脚本。
- 第18章，讲解了Tomcat中负责部署和装载WEB应用的Deployer组件。
- 第19~20章，分别介绍了通过ContainerServlet接口和JMX技术如何控制管理部署在Tomcat中的应用。


每一章配有相关内容的Demo代码，这既是对理论内容的具体展现，也是帮助大家更好地理解，并可以逐步动手实现自己的Java Web服务器。



### 阅读建议
首先，大家对Java基础应该有所掌握，如Thread、Socket、I/O。

大家在阅读时，应着重把握Tomcat作者的设计思想，在此基础上，再针对具体问题进行深入地学习和研究。

带着好奇心，边读书，边看源码（尤其对比Tomcat[最新代码](https://github.com/apache/tomcat)。对比新旧代码，可见其更替优化的场景和过程），更应该亲手画相应的UML图和流程图。最好Debug一遍每章示例Demo——实践出真知。

最后，进行知识关联梳理和整理，比如：Tomcat中用到了哪些设计模式？Tomcat中的管道（pipeline）和阀门（valve）和Servlet中的Filter以及Springmvc中拦截器对比，等等。



### 勘误&支持
由于能力有限，虽然找了些朋友帮忙校队，但书中难免会出现一些错误，也请读者朋友批评指正。大家可以留言反馈错误和建议，我会积极提供解答。

另，也欢迎加我个人QQ：369415359,进行技术&产品&管理切磋交流。

进步始于交流，收获源于分享
