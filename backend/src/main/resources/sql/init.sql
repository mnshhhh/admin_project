CREATE DATABASE IF NOT EXISTS asset_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE asset_management;

-- ----------------------------
-- 部门表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID',
    ancestors   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '祖级列表',
    dept_name   VARCHAR(100) NOT NULL COMMENT '部门名称',
    order_num   INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '部门表';

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    dept_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '归属部门ID',
    username    VARCHAR(50)  NOT NULL COMMENT '账号',
    nickname    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '用户昵称',
    password    VARCHAR(200) NOT NULL COMMENT '加密密码',
    phone       VARCHAR(20)           DEFAULT NULL,
    email       VARCHAR(100)          DEFAULT NULL,
    avatar      VARCHAR(500)          DEFAULT NULL,
    status      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT = '用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    role_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name  VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_key   VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    data_scope CHAR(1)      NOT NULL DEFAULT '1' COMMENT '数据范围(1:全部 2:本部门及下级 3:本部门 4:仅本人)',
    status     CHAR(1)      NOT NULL DEFAULT '0',
    deleted    TINYINT      NOT NULL DEFAULT 0,
    remark     VARCHAR(500)          DEFAULT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '角色表';

-- ----------------------------
-- 菜单/权限点表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name   VARCHAR(100) NOT NULL COMMENT '菜单名称',
    perms       VARCHAR(200)          DEFAULT NULL COMMENT '权限标识',
    menu_type   CHAR(1)      NOT NULL COMMENT '类型(M:目录 C:菜单 F:按钮)',
    path        VARCHAR(200)          DEFAULT NULL COMMENT '路由地址',
    component   VARCHAR(200)          DEFAULT NULL COMMENT '组件路径',
    icon        VARCHAR(100)          DEFAULT '#' COMMENT '图标',
    order_num   INT          NOT NULL DEFAULT 0,
    visible     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '是否显示(0显示 1隐藏)',
    status      CHAR(1)      NOT NULL DEFAULT '0',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '菜单/权限点表';

-- ----------------------------
-- 角色-菜单关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE = InnoDB COMMENT = '角色-菜单关联表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE = InnoDB COMMENT = '用户-角色关联表';

-- ----------------------------
-- 审计日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_log (
    log_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '操作用户',
    username     VARCHAR(50)  NOT NULL,
    operation    VARCHAR(100) NOT NULL COMMENT '操作描述',
    method       VARCHAR(200) NOT NULL COMMENT '请求方法',
    request_url  VARCHAR(500) NOT NULL,
    request_ip   VARCHAR(50)           DEFAULT NULL,
    status       INT          NOT NULL DEFAULT 0 COMMENT '操作状态(0成功 1失败)',
    error_msg    TEXT                  DEFAULT NULL,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '审计日志表';

-- ----------------------------
-- 资产分类表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(100) NOT NULL COMMENT '分类名称',
    code        VARCHAR(50)  NOT NULL COMMENT '分类编码',
    order_num   INT          NOT NULL DEFAULT 0,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '资产分类表';

-- ----------------------------
-- 资产表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_asset (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资产ID',
    asset_code    VARCHAR(100) NOT NULL COMMENT '资产编码(唯一)',
    asset_name    VARCHAR(200) NOT NULL COMMENT '资产名称',
    category_id   BIGINT                DEFAULT NULL COMMENT '分类ID',
    brand         VARCHAR(100)          DEFAULT NULL COMMENT '品牌',
    model         VARCHAR(100)          DEFAULT NULL COMMENT '型号',
    serial_no     VARCHAR(200)          DEFAULT NULL COMMENT '序列号',
    purchase_date DATE                  DEFAULT NULL COMMENT '购置日期',
    purchase_price DECIMAL(12, 2)       DEFAULT NULL COMMENT '购置金额',
    current_value  DECIMAL(12, 2)       DEFAULT NULL COMMENT '当前账面价值',
    depreciation_years INT              DEFAULT NULL COMMENT '折旧年限',
    dept_id       BIGINT       NOT NULL COMMENT '归属部门ID',
    manager_id    BIGINT                DEFAULT NULL COMMENT '资产责任人ID',
    location      VARCHAR(200)          DEFAULT NULL COMMENT '存放位置',
    status        VARCHAR(20)  NOT NULL DEFAULT 'IDLE' COMMENT '状态(IDLE:闲置 IN_USE:在用 REPAIR:维修中 SCRAPPED:已报废)',
    qr_code_url   MEDIUMTEXT            DEFAULT NULL COMMENT '二维码图片Base64',
    remark        VARCHAR(500)          DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_asset_code (asset_code)
) ENGINE = InnoDB COMMENT = '资产表';

-- ----------------------------
-- 借用/归还申请表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_borrow (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_no    VARCHAR(50)  NOT NULL COMMENT '申请单号',
    asset_id     BIGINT       NOT NULL COMMENT '资产ID',
    asset_code   VARCHAR(100) NOT NULL,
    asset_name   VARCHAR(200) NOT NULL,
    applicant_id BIGINT       NOT NULL COMMENT '申请人ID',
    dept_id      BIGINT       NOT NULL COMMENT '申请人部门',
    purpose      VARCHAR(500)          DEFAULT NULL COMMENT '借用用途',
    borrow_time  DATETIME              DEFAULT NULL COMMENT '借用时间',
    expected_return_time DATETIME      DEFAULT NULL COMMENT '预计归还时间',
    actual_return_time   DATETIME      DEFAULT NULL COMMENT '实际归还时间',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING:待审批 APPROVED:已批准 REJECTED:已驳回 BORROWED:借用中 RETURNED:已归还)',
    approver_id  BIGINT                DEFAULT NULL,
    approve_time DATETIME              DEFAULT NULL,
    approve_remark VARCHAR(500)        DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_borrow_no (borrow_no)
) ENGINE = InnoDB COMMENT = '借用/归还申请表';

-- ----------------------------
-- 跨部门调拨表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_allocate (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    allocate_no     VARCHAR(50)  NOT NULL COMMENT '调拨单号',
    asset_id        BIGINT       NOT NULL,
    asset_code      VARCHAR(100) NOT NULL,
    asset_name      VARCHAR(200) NOT NULL,
    from_dept_id    BIGINT       NOT NULL COMMENT '调出部门',
    to_dept_id      BIGINT       NOT NULL COMMENT '调入部门',
    applicant_id    BIGINT       NOT NULL COMMENT '发起人',
    reason          VARCHAR(500)          DEFAULT NULL COMMENT '调拨原因',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/FROM_CONFIRMED/TO_CONFIRMED/COMPLETED/REJECTED)',
    from_confirm_id BIGINT                DEFAULT NULL,
    from_confirm_time DATETIME            DEFAULT NULL,
    to_confirm_id   BIGINT                DEFAULT NULL,
    to_confirm_time DATETIME              DEFAULT NULL,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_allocate_no (allocate_no)
) ENGINE = InnoDB COMMENT = '跨部门调拨表';

-- ----------------------------
-- 报修/维修工单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_repair (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    repair_no    VARCHAR(50)  NOT NULL COMMENT '报修单号',
    asset_id     BIGINT       NOT NULL,
    asset_code   VARCHAR(100) NOT NULL,
    asset_name   VARCHAR(200) NOT NULL,
    reporter_id  BIGINT       NOT NULL COMMENT '报修人',
    dept_id      BIGINT       NOT NULL,
    description  TEXT                  COMMENT '故障描述',
    images       VARCHAR(2000)         DEFAULT NULL COMMENT '故障图片URL列表,逗号分隔',
    repair_man_id BIGINT               DEFAULT NULL COMMENT '维修人员',
    repair_cost   DECIMAL(10, 2)       DEFAULT NULL COMMENT '维修费用',
    repair_hours  DECIMAL(5, 1)        DEFAULT NULL COMMENT '维修工时',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING:待处理 IN_PROGRESS:维修中 COMPLETED:已完成 CLOSED:已关闭)',
    complete_time DATETIME              DEFAULT NULL,
    repair_remark VARCHAR(1000)         DEFAULT NULL COMMENT '维修备注',
    deleted       TINYINT      NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_repair_no (repair_no)
) ENGINE = InnoDB COMMENT = '报修/维修工单表';

-- ----------------------------
-- 盘点任务表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_check_task (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no      VARCHAR(50)  NOT NULL COMMENT '盘点任务编号',
    task_name    VARCHAR(200) NOT NULL COMMENT '任务名称',
    dept_id      BIGINT                DEFAULT NULL COMMENT '盘点部门(NULL=全校)',
    creator_id   BIGINT       NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态(DRAFT/IN_PROGRESS/COMPLETED)',
    start_time   DATETIME              DEFAULT NULL,
    end_time     DATETIME              DEFAULT NULL,
    remark       VARCHAR(500)          DEFAULT NULL,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_no (task_no)
) ENGINE = InnoDB COMMENT = '盘点任务表';

-- ----------------------------
-- 盘点明细表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_check_detail (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id     BIGINT       NOT NULL COMMENT '盘点任务ID',
    asset_id    BIGINT       NOT NULL,
    asset_code  VARCHAR(100) NOT NULL,
    asset_name  VARCHAR(200) NOT NULL,
    expected_dept_id BIGINT  NOT NULL COMMENT '账面归属部门',
    check_result VARCHAR(20)          DEFAULT NULL COMMENT '盘点结果(NORMAL:正常 SURPLUS:盘盈 DEFICIT:盘亏)',
    checker_id  BIGINT               DEFAULT NULL COMMENT '盘点人',
    check_time  DATETIME             DEFAULT NULL,
    remark      VARCHAR(500)         DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '盘点明细表';

-- ----------------------------
-- 审批记录表（通用）
-- ----------------------------
CREATE TABLE IF NOT EXISTS audit_record (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type     VARCHAR(50)  NOT NULL COMMENT '业务类型(BORROW/ALLOCATE/REPAIR/APPLICATION/WAREHOUSE_ENTRY/SCRAP)',
    biz_id       BIGINT       NOT NULL COMMENT '业务ID',
    auditor_id   BIGINT       NOT NULL COMMENT '审批人',
    action       VARCHAR(20)  NOT NULL COMMENT '操作(APPROVE/REJECT)',
    remark       VARCHAR(500)          DEFAULT NULL,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = '审批记录表';

-- ----------------------------
-- 资产申请表
-- ----------------------------
CREATE TABLE IF NOT EXISTS asset_application (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no       VARCHAR(50)  NOT NULL COMMENT '申请单号',
    applicant_id   BIGINT       NOT NULL COMMENT '申请人ID',
    dept_id        BIGINT       NOT NULL COMMENT '申请部门',
    asset_name     VARCHAR(200) NOT NULL COMMENT '资产名称',
    specification  VARCHAR(200)          DEFAULT NULL COMMENT '规格型号',
    quantity       INT          NOT NULL DEFAULT 1 COMMENT '数量',
    estimated_amount DECIMAL(12,2)       DEFAULT NULL COMMENT '预估金额',
    reason         VARCHAR(500)          DEFAULT NULL COMMENT '申请理由',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING:待审批 APPROVED:已批准 REJECTED:已驳回 PROCUREMENT:采购中 WAREHOUSED:已入库 COMPLETED:已完成)',
    approver_id    BIGINT                DEFAULT NULL,
    approve_time   DATETIME              DEFAULT NULL,
    approve_remark VARCHAR(500)          DEFAULT NULL,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_apply_no (apply_no)
) ENGINE = InnoDB COMMENT = '资产申请表';

-- ----------------------------
-- 采购单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS purchase_order (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no         VARCHAR(50)  NOT NULL COMMENT '采购单号',
    application_id   BIGINT       NOT NULL COMMENT '关联申请ID',
    external_partner VARCHAR(200)          DEFAULT NULL COMMENT '外部合作方',
    contact_info     VARCHAR(200)          DEFAULT NULL COMMENT '联系方式',
    total_amount     DECIMAL(12,2)         DEFAULT NULL COMMENT '采购总金额',
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING:待采购 ORDERED:已下单 DELIVERED:已到货 COMPLETED:已完成)',
    expected_date    DATE                  DEFAULT NULL COMMENT '预计到货日期',
    actual_date      DATE                  DEFAULT NULL COMMENT '实际到货日期',
    remark           VARCHAR(500)          DEFAULT NULL,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) ENGINE = InnoDB COMMENT = '采购单表';

-- ----------------------------
-- 入库单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS warehouse_entry (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_no         VARCHAR(50)  NOT NULL COMMENT '入库单号',
    purchase_order_id BIGINT               DEFAULT NULL COMMENT '关联采购单ID',
    application_id   BIGINT       NOT NULL COMMENT '关联申请ID',
    asset_name       VARCHAR(200) NOT NULL COMMENT '资产名称',
    specification    VARCHAR(200)          DEFAULT NULL COMMENT '规格型号',
    quantity         INT          NOT NULL DEFAULT 1 COMMENT '入库数量',
    unit_price       DECIMAL(12,2)         DEFAULT NULL COMMENT '单价',
    total_amount     DECIMAL(12,2)         DEFAULT NULL COMMENT '总金额',
    entry_date       DATE                  DEFAULT NULL COMMENT '入库日期',
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING:待审批 APPROVED:已审批 REJECTED:已驳回)',
    approver_id      BIGINT                DEFAULT NULL,
    approve_time     DATETIME              DEFAULT NULL,
    approve_remark   VARCHAR(500)          DEFAULT NULL,
    remark           VARCHAR(500)          DEFAULT NULL,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_entry_no (entry_no)
) ENGINE = InnoDB COMMENT = '入库单表';

-- ===========================
-- 初始化种子数据
-- ===========================

-- 部门
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num) VALUES
(1, 0, '0', '某高校', 0),
(2, 1, '0,1', '信息学院', 1),
(3, 1, '0,1', '机械学院', 2),
(4, 2, '0,1,2', '计算机系', 1),
(5, 2, '0,1,2', '软件工程系', 2);

-- 角色
INSERT INTO sys_role (role_id, role_name, role_key, data_scope) VALUES
(1, '超级管理员', 'admin', '1'),
(2, '部门管理员', 'dept_admin', '2'),
(3, '普通师生', 'common', '4'),
(4, '维修人员', 'repair_man', '4');

-- 用户（密码均为 123456，BCrypt加密）
INSERT INTO sys_user (user_id, dept_id, username, nickname, password, status) VALUES
(1, 1, 'admin', '超级管理员', '$2a$10$v4/P5uaoS2hnRSJEV0xaf.wbJhW7wYF78NtWNy6QLO..B1HtcDq2i', '0'),
(2, 2, 'dept_mgr', '信息学院管理员', '$2a$10$v4/P5uaoS2hnRSJEV0xaf.wbJhW7wYF78NtWNy6QLO..B1HtcDq2i', '0'),
(3, 4, 'teacher01', '张老师', '$2a$10$v4/P5uaoS2hnRSJEV0xaf.wbJhW7wYF78NtWNy6QLO..B1HtcDq2i', '0'),
(4, 4, 'student01', '李同学', '$2a$10$v4/P5uaoS2hnRSJEV0xaf.wbJhW7wYF78NtWNy6QLO..B1HtcDq2i', '0'),
(5, 1, 'repair01', '维修师傅', '$2a$10$v4/P5uaoS2hnRSJEV0xaf.wbJhW7wYF78NtWNy6QLO..B1HtcDq2i', '0');

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1,1),(2,2),(3,3),(4,3),(5,4);

-- 菜单/权限点（顶级目录 menu_type=M，菜单 C，按钮 F）
INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, menu_type, path, component, icon, order_num) VALUES
-- 一级目录
(1, 0, '工作台', NULL, 'M', '/dashboard', NULL, 'dashboard', 1),
(2, 0, '资产管理', NULL, 'M', '/asset', NULL, 'asset', 2),
(3, 0, '运维管理', NULL, 'M', '/ops', NULL, 'tool', 3),
(4, 0, '审批中心', NULL, 'M', '/audit', NULL, 'check', 4),
(5, 0, '报表中心', NULL, 'M', '/report', NULL, 'chart', 5),
(6, 0, '系统管理', NULL, 'M', '/system', NULL, 'setting', 6),
-- 工作台
(10, 1, '仪表盘', 'report:asset', 'C', 'index', 'dashboard/index', 'dashboard', 1),
-- 资产管理子菜单
(20, 2, '资产列表', 'asset:list', 'C', 'list', 'asset/list', 'list', 1),
(21, 2, '新增资产', 'asset:add', 'F', NULL, NULL, NULL, 2),
(22, 2, '编辑资产', 'asset:edit', 'F', NULL, NULL, NULL, 3),
(23, 2, '删除资产', 'asset:remove', 'F', NULL, NULL, NULL, 4),
(24, 2, '导入资产', 'asset:import', 'F', NULL, NULL, NULL, 5),
(25, 2, '导出资产', 'asset:export', 'F', NULL, NULL, NULL, 6),
(26, 2, '借用资产', 'asset:borrow', 'F', NULL, NULL, NULL, 7),
(27, 2, '归还资产', 'asset:return', 'F', NULL, NULL, NULL, 8),
(28, 2, '跨部门调拨', 'asset:allocate', 'C', 'allocate', 'asset/allocate', 'swap', 9),
(29, 2, '扫码入口', 'asset:scan', 'F', NULL, NULL, NULL, 10),
(30, 2, '资产盘点', 'asset:check', 'C', 'check', 'asset/check', 'check-circle', 11),
-- 运维管理
(40, 3, '报修申请', 'repair:add', 'C', 'repair/apply', 'ops/repair/apply', 'warning', 1),
(41, 3, '维修工单池', 'repair:manage', 'C', 'repair/manage', 'ops/repair/manage', 'tool', 2),
-- 审批中心
(50, 4, '待办审批', 'audit:approve', 'C', 'todo', 'audit/todo', 'bell', 1),
(51, 4, '审批历史', 'audit:view', 'C', 'history', 'audit/history', 'history', 2),
-- 报表中心
(60, 5, '资产统计', 'report:asset', 'C', 'asset', 'report/asset', 'pie-chart', 1),
(61, 5, '折旧报表', 'report:depreciation', 'C', 'depreciation', 'report/depreciation', 'fund', 2),
-- 系统管理
(70, 6, '用户管理', 'sys:user:list', 'C', 'user', 'system/user', 'user', 1),
(71, 6, '新增用户', 'sys:user:add', 'F', NULL, NULL, NULL, 2),
(72, 6, '编辑用户', 'sys:user:edit', 'F', NULL, NULL, NULL, 3),
(73, 6, '删除用户', 'sys:user:remove', 'F', NULL, NULL, NULL, 4),
(74, 6, '角色管理', 'sys:role:manage', 'C', 'role', 'system/role', 'team', 5),
(75, 6, '部门管理', 'sys:dept:manage', 'C', 'dept', 'system/dept', 'apartment', 6),
(76, 6, '审计日志', 'sys:log:view', 'C', 'log', 'system/log', 'file-text', 7),
-- 资产申请
(80, 2, '资产申请', 'asset:apply', 'C', 'application', 'asset/application/index', 'document-add', 12),
(81, 2, '申请审批', 'asset:apply:approve', 'F', NULL, NULL, NULL, 13),
(82, 4, '资产申请审批', 'asset:apply:approve', 'C', 'application', 'asset/application/approval', 'document-checked', 3),
(83, 2, '采购管理', 'asset:po:manage', 'C', 'purchase', 'asset/application/purchase', 'shopping-cart', 14),
(84, 2, '资产入库', 'asset:entry:manage', 'C', 'warehouse', 'asset/application/warehouse', 'box', 15),
(85, 2, '入库审批', 'asset:entry:approve', 'F', NULL, NULL, NULL, 16),
(86, 6, '通知中心', 'notification:list', 'C', 'notification', 'system/notification', 'message', 8);

-- 角色-菜单关联（管理员拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- 部门管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2,1),(2,2),(2,10),(2,20),(2,21),(2,22),(2,24),(2,25),(2,26),(2,27),(2,28),(2,29),(2,30),
(2,3),(2,40),(2,41),
(2,4),(2,50),(2,51),(2,82),
(2,5),(2,60),
(2,80),(2,81),(2,83),(2,84),(2,85),(2,86);

-- 普通师生权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3,1),(3,10),(3,2),(3,20),(3,26),(3,27),(3,29),(3,3),(3,40),(3,4),(3,51),
(3,80),(3,86);

-- 维修人员权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4,1),(4,10),(4,3),(4,41),(4,4),(4,51),(4,86);

-- 资产分类
INSERT INTO asset_category (id, parent_id, name, code, order_num) VALUES
(1, 0, '教学设备', 'TEACH', 1),
(2, 0, '办公设备', 'OFFICE', 2),
(3, 0, '实验仪器', 'LAB', 3),
(4, 1, '多媒体设备', 'TEACH_MEDIA', 1),
(5, 2, '计算机类', 'OFFICE_PC', 1),
(6, 2, '打印设备', 'OFFICE_PRINT', 2);

-- 示例资产数据
INSERT INTO sys_asset (asset_code, asset_name, category_id, brand, model, purchase_date, purchase_price, current_value, depreciation_years, dept_id, manager_id, location, status) VALUES
('AST-2024-000001', '笔记本电脑', 5, '联想', 'ThinkPad E14', '2024-01-10', 6500.00, 5500.00, 5, 2, 2, '信息楼301', 'IDLE'),
('AST-2024-000002', '投影仪', 4, '爱普生', 'CB-X06', '2023-09-01', 4200.00, 3800.00, 8, 2, 2, '信息楼201教室', 'IN_USE'),
('AST-2024-000003', '台式电脑', 5, '戴尔', 'OptiPlex 5090', '2022-06-15', 5800.00, 4300.00, 5, 4, 3, '计算机系机房A', 'IDLE'),
('AST-2024-000004', '激光打印机', 6, '惠普', 'LaserJet Pro M404n', '2023-03-20', 2800.00, 2400.00, 5, 4, 3, '计算机系办公室', 'IDLE'),
('AST-2024-000005', '示波器', 3, '泰克', 'TBS1104', '2021-11-05', 12000.00, 8000.00, 10, 3, 2, '机械实验室', 'IN_USE');

-- ----------------------------
-- 通知表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_notification (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id      BIGINT       NOT NULL COMMENT '接收用户ID',
    type         VARCHAR(50)  NOT NULL COMMENT '通知类型',
    title        VARCHAR(200) NOT NULL COMMENT '通知标题',
    content      VARCHAR(500)          DEFAULT NULL COMMENT '通知内容',
    related_type VARCHAR(50)           DEFAULT NULL COMMENT '关联业务类型',
    related_id   BIGINT               DEFAULT NULL COMMENT '关联业务ID',
    is_read      TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    read_time    DATETIME              DEFAULT NULL COMMENT '阅读时间',
    deleted      TINYINT     NOT NULL DEFAULT 0,
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE = InnoDB COMMENT = '通知表';

-- ============================================================
-- 演示数据（仅演示用途，各状态均覆盖）
-- ============================================================

-- ----------------------------
-- 1. 资产借用记录（覆盖多种状态）
-- ----------------------------
INSERT INTO asset_borrow (id, borrow_no, asset_id, asset_code, asset_name, applicant_id, dept_id, purpose, borrow_time, expected_return_time, actual_return_time, status, approver_id, approve_time, approve_remark, deleted, create_time, update_time) VALUES
(1, 'BOR-20250301001', 1, 'AST-2024-000001', '笔记本电脑', 3, 4, '教学使用', '2025-03-01 09:00:00', '2025-03-15 18:00:00', NULL, 'BORROWED', 2, '2025-03-01 10:00:00', '同意借用', 0, '2025-03-01 09:00:00', '2025-03-01 10:00:00'),
(2, 'BOR-20250305001', 2, 'AST-2024-000002', '投影仪', 3, 4, '会议室演示', '2025-03-05 14:00:00', '2025-03-10 18:00:00', '2025-03-10 17:30:00', 'RETURNED', 2, '2025-03-05 15:00:00', '请爱护设备', 0, '2025-03-05 14:00:00', '2025-03-10 17:30:00'),
(3, 'BOR-20250310001', 3, 'AST-2024-000003', '台式电脑', 4, 4, '课程实训', '2025-03-10 08:30:00', '2025-03-20 18:00:00', NULL, 'PENDING', NULL, NULL, NULL, 0, '2025-03-10 08:30:00', '2025-03-10 08:30:00'),
(4, 'BOR-20250312001', 4, 'AST-2024-000004', '激光打印机', 4, 4, '打印材料', '2025-03-12 10:00:00', '2025-03-25 18:00:00', NULL, 'PENDING', NULL, NULL, NULL, 0, '2025-03-12 10:00:00', '2025-03-12 10:00:00');

-- ----------------------------
-- 2. 资产维修记录（覆盖多种状态）
-- ----------------------------
INSERT INTO asset_repair (id, repair_no, asset_id, asset_code, asset_name, reporter_id, dept_id, description, images, repair_man_id, repair_cost, repair_hours, status, complete_time, repair_remark, deleted, create_time, update_time) VALUES
(1, 'REP-20250306001', 4, 'AST-2024-000004', '激光打印机', 3, 4, '卡纸严重，进纸不稳', NULL, 5, NULL, NULL, 'IN_PROGRESS', NULL, NULL, 0, '2025-03-06 10:00:00', '2025-03-08 14:00:00'),
(2, 'REP-20250303001', 2, 'AST-2024-000002', '投影仪', 3, 4, '投影模糊，灯泡疑似衰减', NULL, 5, 350.00, 2.5, 'COMPLETED', '2025-03-10 10:00:00', '已更换灯泡，测试正常', 0, '2025-03-03 09:00:00', '2025-03-10 10:00:00'),
(3, 'REP-20250311001', 1, 'AST-2024-000001', '笔记本电脑', 4, 4, '键盘部分按键失灵', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL, 0, '2025-03-11 16:00:00', '2025-03-11 16:00:00'),
(4, 'REP-20250309001', 5, 'AST-2024-000005', '示波器', 3, 3, '屏幕黑块，显示异常', NULL, 5, 800.00, 4.0, 'COMPLETED', '2025-03-15 09:00:00', '主板维修完成', 0, '2025-03-09 11:00:00', '2025-03-15 09:00:00');

-- ----------------------------
-- 3. 资产调拨记录
-- ----------------------------
INSERT INTO asset_allocate (id, allocate_no, asset_id, asset_code, asset_name, from_dept_id, to_dept_id, applicant_id, reason, status, deleted, create_time, update_time) VALUES
(1, 'ALLOC-20250305001', 3, 'AST-2024-000003', '台式电脑', 4, 2, 3, '计算机系机房A调整，调拨至信息学院', 'COMPLETED', 0, '2025-03-04 09:00:00', '2025-03-05 11:00:00'),
(2, 'ALLOC-20250312001', 5, 'AST-2024-000005', '示波器', 3, 4, 3, '实验课程需要，临时调拨至机械系', 'PENDING', 0, '2025-03-12 14:00:00', '2025-03-12 14:00:00');

-- ----------------------------
-- 4. 资产盘点任务
-- ----------------------------
INSERT INTO asset_check_task (id, task_no, task_name, dept_id, creator_id, status, start_time, end_time, remark, deleted, create_time, update_time) VALUES
(1, 'CHECK-2025-001', '信息学院2025年第一季度资产清查', 2, 2, 'COMPLETED', '2025-03-01 09:00:00', '2025-03-01 18:00:00', '信息楼201教室一台投影仪盘亏，已上报', 0, '2025-02-28 10:00:00', '2025-03-01 18:00:00'),
(2, 'CHECK-2025-002', '机械系2025年3月资产例行盘点', 4, 3, 'IN_PROGRESS', '2025-03-10 09:00:00', '2025-03-18 18:00:00', '盘点中，预计3月18日完成', 0, '2025-03-10 09:00:00', '2025-03-15 10:00:00');

INSERT INTO asset_check_detail (id, task_id, asset_id, asset_code, asset_name, expected_dept_id, check_result, checker_id, check_time, remark, create_time) VALUES
(1, 1, 2, 'AST-2024-000002', '投影仪', 2, 'DEFICIT', 2, '2025-03-01 17:00:00', '实物未找到，已申请报损', '2025-03-01 09:00:00'),
(2, 1, 1, 'AST-2024-000001', '笔记本电脑', 2, 'NORMAL', 2, '2025-03-01 16:00:00', NULL, '2025-03-01 09:00:00'),
(3, 2, 3, 'AST-2024-000003', '台式电脑', 4, 'NORMAL', 3, '2025-03-15 10:00:00', NULL, '2025-03-10 09:00:00'),
(4, 2, 4, 'AST-2024-000004', '激光打印机', 4, 'NORMAL', 3, '2025-03-15 10:00:00', NULL, '2025-03-10 09:00:00');

-- ----------------------------
-- 5. 资产采购申请（覆盖完整生命周期）
-- ----------------------------
INSERT INTO asset_application (id, apply_no, applicant_id, dept_id, asset_name, specification, quantity, estimated_amount, reason, status, approver_id, approve_time, approve_remark, deleted, create_time, update_time) VALUES
-- 待审批 (teacher01 提交)
(1, 'APP-20250325001', 3, 4, '无线投影仪', '爱普生 CB-1795F / 5000流明', 2, 16000.00, '多媒体教室升级改造', 'PENDING', NULL, NULL, NULL, 0, '2025-03-25 08:30:00', '2025-03-25 08:30:00'),
-- 审批通过，采购中 (teacher01 提交，admin 审批)
(2, 'APP-20250320001', 3, 4, '机械键盘', 'Cherry MX Board 3.0', 10, 3500.00, '计算机实验室外设更新', 'PROCUREMENT', 1, '2025-03-21 14:00:00', '同意采购', 0, '2025-03-20 10:00:00', '2025-03-21 14:00:00'),
-- 入库中：审批通过，采购完成待入库 (student01 提交)
(3, 'APP-20250315001', 4, 4, '降噪耳机', 'Sony WH-1000XM5', 5, 12500.00, '语音室建设需要', 'APPROVED', 1, '2025-03-16 11:00:00', '通过', 0, '2025-03-15 09:00:00', '2025-03-16 11:00:00'),
-- 已完成：领取完毕 (teacher01 提交)
(4, 'APP-20250310001', 3, 4, '便携显示器', '15.6寸 4K IPS', 3, 6000.00, '移动办公需求', 'COMPLETED', 1, '2025-03-11 09:00:00', '同意', 0, '2025-03-10 14:00:00', '2025-03-12 10:00:00'),
-- 已驳回 (student01 提交，admin 驳回)
(5, 'APP-20250322001', 4, 4, '台式主机', 'i9-14900K / RTX4090', 2, 50000.00, '科研需求', 'REJECTED', 1, '2025-03-23 15:00:00', '预算超标，建议分批采购', 0, '2025-03-22 11:00:00', '2025-03-23 15:00:00');

-- ----------------------------
-- 6. 采购单（对应 APP-20250320001 机械键盘，状态=采购中）
-- ----------------------------
INSERT INTO purchase_order (id, order_no, application_id, external_partner, contact_info, total_amount, status, expected_date, actual_date, remark, deleted, create_time, update_time) VALUES
(1, 'PO-20250321001', 2, '京东慧采', '400-606-9966', 3200.00, 'ORDERED', '2025-03-25', NULL, '已下单，等待发货', 0, '2025-03-21 15:00:00', '2025-03-22 10:00:00');

-- ----------------------------
-- 7. 入库单（对应 APP-20250315001 降噪耳机，状态=已批准待入库）
-- ----------------------------
INSERT INTO warehouse_entry (id, entry_no, purchase_order_id, application_id, asset_name, specification, quantity, unit_price, total_amount, entry_date, status, approver_id, approve_time, approve_remark, remark, deleted, create_time, update_time) VALUES
(1, 'WE-20250323001', NULL, 3, '降噪耳机', 'Sony WH-1000XM5', 5, 2300.00, 11500.00, '2025-03-23', 'PENDING', NULL, NULL, NULL, '已到货，待质检入库', 0, '2025-03-23 09:00:00', '2025-03-23 09:00:00');

-- ----------------------------
-- 8. 审批记录
-- ----------------------------
INSERT INTO audit_record (id, biz_type, biz_id, auditor_id, action, remark, create_time) VALUES
(1, 'APPLICATION', 2, 1, 'APPROVE', '同意采购', '2025-03-21 14:00:00'),
(2, 'APPLICATION', 3, 1, 'APPROVE', '通过', '2025-03-16 11:00:00'),
(3, 'APPLICATION', 4, 1, 'APPROVE', '同意', '2025-03-11 09:00:00'),
(4, 'APPLICATION', 5, 1, 'REJECT', '预算超标，建议分批采购', '2025-03-23 15:00:00'),
(5, 'BORROW', 1, 2, 'APPROVE', '同意借用', '2025-03-01 10:00:00'),
(6, 'BORROW', 2, 2, 'APPROVE', '请爱护设备', '2025-03-05 15:00:00'),
(7, 'ALLOCATE', 1, 1, 'APPROVE', '同意调拨', '2025-03-05 11:00:00');

-- ----------------------------
-- 9. 通知数据（模拟入库通知、借用通知）
-- ----------------------------
INSERT INTO sys_notification (id, user_id, type, title, content, related_type, related_id, is_read, read_time, deleted, create_time) VALUES
-- student01 的入库通知（未读）
(1, 4, 'WAREHOUSE_ENTRY', '资产入库通知', '您申请的资产「降噪耳机」已到货，请联系管理员办理入库领取。', 'APPLICATION', 3, 0, NULL, 0, '2025-03-23 09:05:00'),
-- teacher01 的借用通过通知（已读）
(2, 3, 'AUDIT', '借用申请通过', '您申请的借用「笔记本电脑」（单号BOR-20250301001）已通过审批。', 'BORROW', 1, 1, '2025-03-01 10:30:00', 0, '2025-03-01 10:00:00'),
-- student01 的借用通过通知（已读）
(3, 4, 'AUDIT', '借用申请通过', '您申请的借用「投影仪」（单号BOR-20250305001）已通过审批。', 'BORROW', 2, 1, '2025-03-05 16:00:00', 0, '2025-03-05 15:00:00'),
-- teacher01 的报修处理通知（未读）
(4, 3, 'REPAIR', '维修进度通知', '您上报的「示波器」维修已完成，设备已返回实验室。', 'REPAIR', 4, 0, NULL, 0, '2025-03-15 09:10:00');
