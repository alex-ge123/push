
-- ----------------------------
-- 基础服务数据库virsical_base中路由配置表sys_route_conf，加入服务路由配置
-- ----------------------------
INSERT INTO `sys_route_conf` VALUES (null, '推送服务', 'push', '[{\"args\": {\"_genkey_0\": \"/push/**\"}, \"name\": \"Path\"}]', '[]', 'lb://virsical-push', 0, '2019-07-03 10:00:00', '2019-07-03 10:00:00', '0');
INSERT INTO `sys_route_conf` VALUES (null, '推送长链接支持1', 'push-ws-1', '[{\"args\": {\"_genkey_0\": \"/push/ws/info/**\"}, \"name\": \"Path\"}]', '[]', 'lb://virsical-push', 0, '2019-07-03 10:00:00', '2019-07-03 10:00:00', '0');
INSERT INTO `sys_route_conf` VALUES (null, '推送长链接支持2', 'push-ws-2', '[{\"args\": {\"_genkey_0\": \"/push/ws/**\"}, \"name\": \"Path\"}]', '[]', 'lb:ws://virsical-push', 0, '2019-07-03 10:00:00', '2019-07-03 10:00:00', '0');


