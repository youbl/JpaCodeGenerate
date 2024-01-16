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
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `url` varchar(500) NOT NULL DEFAULT '' COMMENT '网址',
  `memo` varchar(3000) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_key` (`username`,`secure`),
  UNIQUE KEY `uq_user_title` (`title`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户储存的otpcode密钥列表';

CREATE TABLE `clean_config` (
    `id` int NOT NULL AUTO_INCREMENT,
    `linkinfo_id` int NOT NULL DEFAULT '0' COMMENT 'mysql连接信息表id',
    `enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用，0否，1是',
    `db` varchar(100) NOT NULL DEFAULT '' COMMENT '要清理的表，所在的数据库名',
    `back_db` varchar(100) NOT NULL DEFAULT '' COMMENT '全局备份库名，备份数据放到哪个数据库名（同一IP实例下的另一个数据库），为空使用表所在数据库',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据清理的配置表';

CREATE TABLE `clean_tables` (
    `id` int NOT NULL AUTO_INCREMENT,
    `config_id` int NOT NULL DEFAULT '0' COMMENT '所属实例，即clean_config表id',
    `enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用，0否，1是',
    `table_name` varchar(100) NOT NULL DEFAULT '' COMMENT '要清理的表名,必填',
    `time_field` varchar(100) NOT NULL DEFAULT '' COMMENT '用于时间筛选的字段名,通常为记录入库时间或更新时间,必填',
    `force_index_name` varchar(100) NOT NULL DEFAULT '' COMMENT '可空，有时mysql会不使用索引，有此配置表示强制使用该索引进行查询。注：索引必须存在，否则会报错',
    `key_field` varchar(100) NOT NULL DEFAULT 'id' COMMENT '主键字段名，只支持自增字段 或 雪花算法这种递增值，只支持单个key，可空，默认值id',
    `other_condition` varchar(500) NOT NULL DEFAULT '' COMMENT '进行备份或删除的数据，需要的其它sql筛选条件, 可以以and或or开头，也可以不加开头',
    `keep_days` smallint NOT NULL DEFAULT '90' COMMENT '要保留的记录天数，超过该时长的数据，进行删除, 可空，默认90',
    `run_hours` varchar(100) NOT NULL DEFAULT '' COMMENT '执行清理的小时范围，[0,4-10,15-23] 表示0点，4点到10点，15到23点; 为空表示0-23',
    `need_backup` tinyint NOT NULL DEFAULT '0' COMMENT '1表示先备份再删除，0表示不备份直接删除',
    `back_db` varchar(100) NOT NULL DEFAULT '' COMMENT '备份库名，为空使用clean_config表配置',
    `partition_num` smallint NOT NULL DEFAULT '0' COMMENT '表的分区数量，设置该值，可以更高效的对分区表进行清理，会对每个分区单独清理，非分区表请填写0或1，或置空',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据清理的子配置';

