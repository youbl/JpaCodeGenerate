# 快速开始
- 用IDEA打开pom.xml，然后构建项目
- 把target\code-generate-0.0.1-SNAPSHOT.jar 和 target\lib目录，复制到服务器上，假设是`/data/app/`
- 在服务器上启动，命令参考：`/usr/java/jdk1.8.0_261/bin/java -Dserver.port=8808 -Dspring.profiles.active=prod -Dloader.path=/data/app/lib -jar /data/app/code-generate-0.0.1-SNAPSHOT.jar`
- 启动成功后，访问：`http://服务器IP:8808/index.html` 即可


# 功能介绍
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
用于对比不同 nacos 的yml配置差异。
也是用于测试和生产的配置对比，避免发布遗漏。

配置比对界面展示：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/compareNacosConfig.jpg?raw=true)

## MySql数据库查询工具
在线的MySql连接查询工具。  
用于一些无法开放端口的MySQL查询，只读，不支持更新。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo.jpg?raw=true)
