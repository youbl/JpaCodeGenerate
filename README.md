启动项目后，访问：
http://localhost:8080/index.html  

# Spring JPA代码生成工具
从MySQL读取表结构，并生成对应的entity、repository、service、controller、dto等类，
减少手工书写代码的麻烦。  

# MySql数据库结构对比工具
读取2个数据库的所有表结构、索引结构，并进行对比。  
用于发布前确认表结构已经同步。  
同时可以生成对应的刷库语句。

# Nacos配置对比工具
用于对比不同 nacos 的yml配置差异。
也是用于测试和生产的配置对比，避免发布遗漏。

# MySql数据库查询工具
在线的MySql连接查询工具。  
用于一些无法开放端口的MySQL查询，只读，不支持更新。

演示界面：
![image](https://github.com/youbl/JpaCodeGenerate/blob/master/demo.jpg?raw=true)
