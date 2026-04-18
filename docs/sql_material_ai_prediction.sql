-- AI prediction persistence table for stock warning report
CREATE TABLE IF NOT EXISTS `jsh_material_ai_prediction` (
  `material_id` bigint(20) NOT NULL COMMENT '商品ID',
  `depot_id` bigint(20) NOT NULL COMMENT '仓库ID(-1代表所有仓库汇总)',
  `forecast_qty` decimal(24,6) DEFAULT NULL COMMENT '未来7天预测销量',
  `suggest_qty` decimal(24,6) DEFAULT NULL COMMENT '建议补货量',
  `ai_analysis` varchar(500) DEFAULT NULL COMMENT 'AI专家备注说明诊断',
  `tenant_id` bigint(20) DEFAULT NULL COMMENT '租户id',
  `delete_flag` varchar(1) DEFAULT '0' COMMENT '删除标记，0未删除，1删除',
  PRIMARY KEY (`material_id`, `depot_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='商品AI预测预警持久化明细表';

