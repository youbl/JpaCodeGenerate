# 部署步骤
## 数据库配置修改
- 打开 项目里的resources\static\下的相关html文件,修改里面的连接串
  - diff/mysql.html 修改文件里的sqlConns、 selectTestDbName、selectProdDbName
  - diff/mysqlIndex.html 修改文件里的sqlConns、 selectTestDbName、selectProdDbName
  - diff/nacos.html 修改文件里的searchCond
  - redisTool/redis.html 修改文件里的selectConn、redisConnList
  - sqlTool/dml.html 修改文件里的selectDbName、sqlConns
  - sqlTool/mysql.html 修改文件里的selectDbName、sqlConns

- 点IDEA右侧Maven下面的package，构建并打包项目，得到jar文件
- 复制jar到服务器上，启动如下命令（最好用supervisorctl 托管）


## 启动
- 因为分离了依赖库，在启动时要增加参数loader.path，例如：
```
c:\jdk-8\jdk1.8.0_301\bin\java -Dspring.profiles.active=prod -Dserver.port=8808 -Dloader.path=d:\gitlab\lib -jar d:\gitlab\code-generate-0.0.1-SNAPSHOT.jar
```