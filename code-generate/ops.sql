/*Table structure for table `admin_log` */

CREATE TABLE `admin_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `loginUser` varchar(50) NOT NULL DEFAULT '' COMMENT '登录人',
  `method` varchar(10) NOT NULL DEFAULT '' COMMENT '请求方法',
  `url` varchar(200) NOT NULL DEFAULT '' COMMENT '请求地址',
  `query` varchar(2000) NOT NULL DEFAULT '' COMMENT '查询串',
  `ip` varchar(20) NOT NULL DEFAULT '' COMMENT '请求IP',
  `requestHeaders` text NOT NULL COMMENT '请求头',
  `requestBody` text NOT NULL COMMENT '请求体',
  `responseStatus` int(11) NOT NULL DEFAULT '0' COMMENT '响应状态',
  `responseHeaders` text NOT NULL COMMENT '响应头',
  `costMillis` int(11) NOT NULL DEFAULT '0' COMMENT '耗时ms',
  `exp` text NOT NULL COMMENT '异常',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`loginUser`),
  KEY `idx_url` (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';

/*Table structure for table `admin_menu_group` */

CREATE TABLE `admin_menu_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `saasId` varchar(20) NOT NULL DEFAULT '' COMMENT 'SaaS应用ID',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `url` varchar(1000) NOT NULL DEFAULT '' COMMENT '链接',
  `openMode` varchar(20) NOT NULL DEFAULT '_blank' COMMENT '打开方式',
  `show` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否展示',
  `sort` tinyint(4) NOT NULL DEFAULT '99' COMMENT '排序',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_title_saas` (`title`,`saasId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单分组表';

/*Table structure for table `admin_menus` */

CREATE TABLE `admin_menus` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `saasId` varchar(20) NOT NULL DEFAULT '' COMMENT 'SaaS应用ID',
  `groupId` int(11) NOT NULL DEFAULT '0' COMMENT '分组ID',
  `img` varchar(200) NOT NULL DEFAULT '' COMMENT 'icon图片地址',
  `url` varchar(1000) NOT NULL DEFAULT '' COMMENT '链接',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `memo` varchar(100) NOT NULL DEFAULT '' COMMENT '语言说明',
  `openMode` varchar(20) NOT NULL DEFAULT '_blank' COMMENT '打开方式',
  `popTitle` varchar(20) NOT NULL DEFAULT '' COMMENT '弹窗的标题',
  `popWidth` int(11) NOT NULL DEFAULT '350' COMMENT '弹窗的展示宽度',
  `show` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否展示',
  `sort` tinyint(4) NOT NULL DEFAULT '99' COMMENT '排序',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_title_saas` (`title`,`saasId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主菜单表';

/*Table structure for table `admin_submenus` */

CREATE TABLE `admin_submenus` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `saasId` varchar(20) NOT NULL DEFAULT '' COMMENT 'SaaS应用ID',
  `menuId` int(11) NOT NULL DEFAULT '0' COMMENT '所属主菜单ID-admin_menus表主键',
  `url` varchar(1000) NOT NULL DEFAULT '' COMMENT '链接',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `openMode` varchar(20) NOT NULL DEFAULT '_blank' COMMENT '打开方式',
  `show` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否展示',
  `sort` tinyint(4) NOT NULL DEFAULT '99' COMMENT '排序',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子菜单表';

/*Table structure for table `configIgnoreKeys` */

CREATE TABLE `configIgnoreKeys` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `app_key` varchar(1000) NOT NULL DEFAULT '' COMMENT 'app标识',
  `key` varchar(200) NOT NULL DEFAULT '' COMMENT 'app密钥',
  `create_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_appkey` (`app_key`(768))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置忽略信息表';

/*Table structure for table `linkinfo` */

CREATE TABLE `linkinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `link_type` varchar(20) NOT NULL DEFAULT 'mysql' COMMENT '类型：mysql/redis/nacos',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '说明',
  `address` varchar(500) NOT NULL DEFAULT '' COMMENT 'IP或域名、URL信息',
  `account` varchar(20) NOT NULL DEFAULT '' COMMENT '登录账号',
  `pwd` varchar(200) NOT NULL DEFAULT '' COMMENT 'AES加密后的登录密码',
  `port` int(11) NOT NULL DEFAULT '0' COMMENT '连接端口信息',
  `info` varchar(8000) NOT NULL DEFAULT '' COMMENT 'json结构，其它连接信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='连接信息表';

/*Table structure for table `otpcode` */

CREATE TABLE `otpcode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL DEFAULT '' COMMENT '归属用户',
  `title` varchar(50) NOT NULL DEFAULT '' COMMENT '该code的说明',
  `secure` varchar(500) NOT NULL DEFAULT '' COMMENT 'AES加密后的OtpCode的密钥',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `url` varchar(500) NOT NULL DEFAULT '' COMMENT '网址',
  `memo` varchar(3000) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_key` (`username`,`secure`),
  UNIQUE KEY `uq_user_title` (`title`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户储存的otpcode密钥列表';