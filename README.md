## 基于Webmagic的B站活跃用户数据爬虫（深度遍历) ##

### 技术栈说明 ###    
* 爬虫内核： <a herf="http://www.http://webmagic.io/" >Webmagic</a>  
* 持久层：JDBC  
* 数据库：MySQL  

### 程序说明 ###  
* 使用深度遍历的方式，爬取B站活跃用户数据；  
* B站用户数据接口：http://space.bilibili.com/ajax/member/GetInfo   
Post方式获取数据，参数 mid （用户B站ID）；  
* 数据跳转：http://space.bilibili.com/xxxxx/#!/fans/fans  （xxxxx为mid）  
            http://space.bilibili.com/xxxxx/#!/fans/follow  （xxxxx为mid）
* 详细爬取思路说明：<a href="http://blog.csdn.net/al_assad/article/details/70217621">Al-assad 's CSDN blog</a>  
* 关联项目：<a href="https://github.com/Al-assad/Spider-bilibiliuser-full">基于Webmagic的B站用户数据爬虫（广度遍历）</a>

### 爬取数据项 ###  
* mid：用户b站ID  
* nama：用户昵称  
* sex：用户性别  
* level：用户等级  
* sign：用户签名  
* faceUrl：用户头像图像URL  
* friends：用户关注数  
* fans：用户被关注数  
* playNum：用户播放视频数  
* birthday：用户生日  
* place：用户地点  

### 样本数据 ###
百度云 <a href="http://pan.baidu.com/s/1dFchDZj">http://pan.baidu.com/s/1dFchDZj 验证码：b2fi</a>  
