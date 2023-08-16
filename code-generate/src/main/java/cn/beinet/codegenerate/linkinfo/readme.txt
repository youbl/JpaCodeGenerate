这个目录是通用连接信息的后端接口，把所有工具所需的mysql、redis、nacos连接信息统一保存，避免前端泄露
依赖表结构：
CREATE TABLE `linkinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `link_type` varchar(20) NOT NULL DEFAULT 'mysql' COMMENT '类型：mysql/redis/nacos',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '说明',
  `address` varchar(500) NOT NULL DEFAULT '' COMMENT 'IP或域名、URL信息',
  `account` varchar(20) NOT NULL DEFAULT '' COMMENT '登录账号',
  `pwd` varchar(50) NOT NULL DEFAULT '' COMMENT '登录密码',
  `port` int(11) NOT NULL DEFAULT '0' COMMENT '连接端口信息',
  `info` varchar(8000) NOT NULL DEFAULT '' COMMENT 'json结构，其它连接信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='连接信息表';