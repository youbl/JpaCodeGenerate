# 功能清单
- MySql数据库查询工具，只读查询支持
- Redis查询工具，只读查询支持
- MySql数据库表结构对比工具，用于发布前的测试环境和生产环境 表结构比对
- MySql数据库索引对比工具
- Nacos配置对比工具，用于发布前的测试环境和生产环境 配置比对
- Spring-config-server配置对比工具，用于发布前的测试环境和生产环境 配置比对
- MFA(OTP-Code)保存和生成工具
- Telnet和Ping工具
- Mybatis-plus代码生成工具，带vue的前端页面和后端CRUD代码
- 阿里云CDN缓存清理功能，要先去[这里](https://help.aliyun.com/document_detail/121541.html)下载安装工具
- 操作日志，以上操作的日志记录
- job：自动备份能力
- job：自动mysql数据备份并删除的能力

# 快速部署和启动
- 创建MySQL数据库，在新数据库里建表，使用`ops.sql`
- 用IDEA打开pom.xml，修改application.yml里的数据库连接信息、LDAP认证服务器信息  
注：如没有LDAP服务，可以修改代码`LdapLoginFilter.validateFromLDAP`,在那边添加你的自定义认证逻辑
- 然后点IDEA界面右侧：`Maven->Lifecycle->package`构建项目
- 把target目录下 code-generate-0.0.1-SNAPSHOT.jar文件 和 lib目录，复制到服务器上，假设复制到这个目录下`/data/app/`
- 在服务器上启动，命令参考：  
`/usr/java/jdk-11/bin/java -Dserver.port=8808 -Dspring.profiles.active=prod -Dloader.path=/data/app/lib -jar /data/app/code-generate-0.0.1-SNAPSHOT.jar`
- 启动成功后，访问：`http://服务器IP:8808/sql/index.html` 即可
- 注1：建议安装为系统服务，如果是windows环境可以使用nssm：
```
nssm install sqlCompare java -Dspring.profiles.active=prod -Dserver.port=8808 -Dloader.path=D:\lib\ -jar D:\app\code-generate-0.0.1-SNAPSHOT.jar 
nssm set sqlCompare AppStdout d:\app\code-generate.log
nssm set sqlCompare AppStderr d:\app\code-generate-err.log
```
Linux使用systemd：
  - 1、编辑配置文件: vim /etc/systemd/system/sql-compare.service
内容参考：
```
[Unit]
Description=sql compare java service
After=syslog.target network.target

[Service]
SuccessExitStatus=143
User=ubuntu
Type=simple

Environment="JAVA_HOME=/usr/local/jdk-21.0.5"
WorkingDirectory=/data/app/
ExecStart=${JAVA_HOME}/bin/java -Dspring.profiles.active=prod -Dserver.port=8808 -Dloader.path=/data/app/lib -jar /data/app/code-generate-0.0.1-SNAPSHOT.jar
ExecStop=/bin/kill -15 $MAINPID

[Install]
WantedBy=multi-user.target
```
  - 2、重新加载配置，并启用服务：
    systemctl daemon-reload
    systemctl enable sql-compare.service
  - 3、启动服务：
    systemctl start sql-compare && systemctl status sql-compare
  - 4、看状态：
    systemctl status sql-compare.service
    看日志  journalctl -f -n 1000 -u sql-compare
  - 5、停止服务：
    systemctl stop sql-compare && systemctl status sql-compare
- 注2：建议前端增加nginx转发，nginx配置参考：
```
server {
    listen       80;
    server_name  _;
    location /ip {
      default_type text/plain;
      return 200 "$remote_addr";
    }
    location /sql-cb/ {
      proxy_pass http://localhost:8808;  # 8808后无斜杠，表示转发url带有sql这一截字符串
      proxy_set_header Host $host; 
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;  
      proxy_connect_timeout 300; #单位秒
      proxy_send_timeout 300; #单位秒
      proxy_read_timeout 300; #单位秒
    }
```


# 功能介绍

## MySql数据库查询工具
在线的MySql连接查询工具。  
用于一些无法开放端口的MySQL查询，只读，不支持更新。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo.jpg?raw=true)

## Redis查询工具
在线的Redis连接查询工具。  
用于一些无法开放端口的Redis查询，只能输入key键名，进行查询，不支持info之外的其它命令操作。  
注：输入key后，该工具会自动判断key的类型，是String还是Hash、Set，并自动按对应的结构进行展示。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo_redis.png?raw=true)

## MySql数据库表结构对比工具（不含索引）
读取2个数据库的所有表结构，并进行对比。  
用于发布前确认表结构已经同步。  
同时可以生成对应的刷库语句。

表结构比对界面展示：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/compareTable.jpg?raw=true)

## MySql数据库索引对比工具
读取2个数据库的所有表的索引结构，并进行对比。  
用于发布前确认索引已经同步。  
同时可以生成对应的刷库语句。

索引比对界面展示：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/compareIndex.jpg?raw=true)

## Nacos配置对比工具
如果使用nacos作为配置中心时，可以在这里对比不同 nacos 的yml配置差异。
也是用于测试和生产的配置对比，避免发布遗漏。

配置比对界面展示：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/compareNacosConfig.jpg?raw=true)

## Spring-config-server配置对比工具
如果使用spring-config-server作为配置中心时，可以在这里对比不同配置中心的yml配置差异。
也是用于测试和生产的配置对比，避免发布遗漏。

配置比对界面展示：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/compareSpringConfigServer.png?raw=true)

## OTP-Code保存和生成工具
基于安全，现在很多平台或应用，都会在输入账号密码后，要求进行二次认证，就是使用OTP-Code,  
这个工具，可以帮你保存生成二次认证code的密钥，并与登录用户关联，加密后入库。  
注：界面上每次会生成5个code，理论上都是可以用的，因为一般平台在验证时，通常会对比最近的多个验证码。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo_otp.png?raw=true)

## Telnet和Ping工具
在线对指定IP或域名，进行ping连通性检查，或对指定端口进行telnet连通性检查的工具，先排除是否网络问题。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo_telnet.png?raw=true)

## Mybatis-plus代码生成工具
从MySQL读取表结构，并生成对应的Java源码文件和对应的前端VUE编辑/列表页面，可以直接复制到项目中，
直接使用，减少手工书写代码的麻烦，生成内容如下：
- entity
- mapper
- service
- controller
- dto
- entity与dto互转的mapstruct-mapper转换类
- 基于VUE2.0的前端html页面  
- 注意：**表主键请使用int或long类型的id**

生成说明：
- 点击页面的`下载文件`, 并解压
- 解压的文件，除html，其它文件复制到你的SpringBoot项目对应的java目录下
- html复制到你的SpringBoot项目的`resources\static`目录下
- 复制项目根目录下的`res.zip`，就是html页面依赖的js、css、字体等文件，解压到`resources\static`目录下
- 在你的项目pom.xml里添加mapstruct的如下2个引用，并进行maven构建：
  * <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId></dependency>
  * <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId></dependency>
- OK，可以启动你的项目，访问生成的html验证了

操作界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/generate.jpg?raw=true)

生成的增删改查结果界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/generateResult.png?raw=true)

## Spring JPA代码生成工具
从MySQL读取表结构，并生成对应的entity、repository、service、controller、dto等类，
减少手工书写代码的麻烦。  
todo: 未完成
