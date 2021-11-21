# 部署步骤
## 数据库配置修改
- 打开 项目里的application.yml,修改datasourceProd和datasourceTest的配置
- 点IDEA右侧Maven下面的package，构建并打包项目，得到jar文件
- 复制jar到服务器上，启动如下命令（最好用supervisorctl 托管）
