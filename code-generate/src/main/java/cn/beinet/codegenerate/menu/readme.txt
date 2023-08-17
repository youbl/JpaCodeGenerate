这个目录是快捷菜单的后端接口，对应前端页面：menu.html
依赖表结构：
===================================================================
/* 菜单分组表，区分不同的大组，如内部站点、外部站点 */
CREATE TABLE `admin_menu_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `url` varchar(1000) NOT NULL DEFAULT '' COMMENT '链接',
  `openMode` varchar(20) NOT NULL DEFAULT '_blank' COMMENT '打开方式',
  `show` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否展示',
  `sort` tinyint(4) NOT NULL DEFAULT '99' COMMENT '排序',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单分组表';

insert  into `admin_menu_group`(`title`,`url`,`openMode`,`show`,`sort`) values
('内部系统导航','','_blank',1,97),
('运维系统导航','','_blank',1,98),
('外部站点','','_blank',1,99);

/* 主菜单表，区分不同的平台，如git站点（每个项目在子菜单表）、语雀站点、K8S站点等 */
CREATE TABLE `admin_menus` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `groupId` int(11) NOT NULL DEFAULT '0' COMMENT '分组ID',
  `img` varchar(200) NOT NULL DEFAULT '' COMMENT 'icon图片地址',
  `url` varchar(1000) NOT NULL DEFAULT '' COMMENT '链接',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `memo` varchar(100) NOT NULL DEFAULT '' COMMENT '语言说明',
  `openMode` varchar(20) NOT NULL DEFAULT '_blank' COMMENT '打开方式',
  `popTitle` varchar(20) NOT NULL DEFAULT '' COMMENT '弹窗的标题',
  `popWidth` int(11) NOT NULL DEFAULT '150' COMMENT '弹窗的展示宽度',
  `show` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否展示',
  `sort` tinyint(4) NOT NULL DEFAULT '99' COMMENT '排序',
  `createDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主菜单表';

/* 子菜单表，每个平台下的细站点，如A项目在git站的地址，B项目在语雀站的地址 */
CREATE TABLE `admin_submenus` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
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