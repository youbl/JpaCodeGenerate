这个目录是otpcode两步验证密钥保存、实时code计算查看功能的后端接口

CREATE TABLE `otpcode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL DEFAULT '' COMMENT '归属用户',
  `title` varchar(50) NOT NULL DEFAULT '' COMMENT '该code的说明',
  `secure` varchar(500) NOT NULL DEFAULT '' COMMENT 'AES加密后的OtpCode的密钥',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_key` (`username`,`secure`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户储存的otpcode密钥列表';
