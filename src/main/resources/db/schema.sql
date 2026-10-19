-- =========================================================
-- 食谱点菜系统 数据库脚本（MySQL 8.0）
-- 核心：食谱(Recipe) 1:N 烹饪步骤 / 食材 / 调料 / 烹饪技巧
-- =========================================================
CREATE DATABASE IF NOT EXISTS system_cookies
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE system_cookies;

-- ----------------------------
-- 食谱分类
-- ----------------------------
CREATE TABLE IF NOT EXISTS recipe_category
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0为顶级',
    icon        VARCHAR(255)          DEFAULT NULL COMMENT '分类图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_category_parent (parent_id),
    KEY idx_category_sort (sort)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='食谱分类';

-- ----------------------------
-- 食谱主表
-- ----------------------------
CREATE TABLE IF NOT EXISTS recipe
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '所属分类ID',
    name           VARCHAR(128) NOT NULL COMMENT '食谱名称',
    subtitle       VARCHAR(255)          DEFAULT NULL COMMENT '副标题/一句话介绍',
    cover_url      VARCHAR(512)          DEFAULT NULL COMMENT '封面图',
    description    TEXT COMMENT '菜品简介',
    cuisine        VARCHAR(32)           DEFAULT NULL COMMENT '菜系：川菜/粤菜/家常菜...',
    taste          VARCHAR(32)           DEFAULT NULL COMMENT '口味：微辣/酸甜/清淡...',
    cook_method    VARCHAR(32)           DEFAULT NULL COMMENT '主要烹饪方法：炒/蒸/炖/烤/煎/煮/凉拌',
    difficulty     TINYINT      NOT NULL DEFAULT 1 COMMENT '难度：1简单 2中等 3困难',
    cook_time      INT          NOT NULL DEFAULT 0 COMMENT '预计总耗时（分钟）',
    servings       INT          NOT NULL DEFAULT 1 COMMENT '份量：适合几人份',
    calories       INT                   DEFAULT NULL COMMENT '热量（千卡/份）',
    price          DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '点菜价格',
    original_price DECIMAL(10, 2)        DEFAULT NULL COMMENT '原价',
    stock          INT          NOT NULL DEFAULT 0 COMMENT '今日可售份数',
    sales          INT          NOT NULL DEFAULT 0 COMMENT '累计销量',
    is_recommend   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否推荐：0否 1是',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_recipe_category (category_id),
    KEY idx_recipe_status (status),
    KEY idx_recipe_sales (sales)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='食谱主表';

-- ----------------------------
-- 烹饪步骤（Recipe 1:N）
-- ----------------------------
CREATE TABLE IF NOT EXISTS cooking_step
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    step_no     INT          NOT NULL COMMENT '第几步，从1开始',
    title       VARCHAR(128)          DEFAULT NULL COMMENT '步骤标题，如：处理食材',
    content     TEXT         NOT NULL COMMENT '步骤正文：该步怎么做',
    image_url   VARCHAR(512)          DEFAULT NULL COMMENT '步骤配图',
    duration    INT          NOT NULL DEFAULT 0 COMMENT '本步耗时（分钟）',
    tip         VARCHAR(255)          DEFAULT NULL COMMENT '本步注意要点',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_step_recipe_no (recipe_id, step_no),
    KEY idx_step_recipe (recipe_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='烹饪步骤';

-- ----------------------------
-- 食材清单（Recipe 1:N）
-- ----------------------------
CREATE TABLE IF NOT EXISTS recipe_ingredient
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    name        VARCHAR(64)  NOT NULL COMMENT '食材名称',
    amount      VARCHAR(32)           DEFAULT NULL COMMENT '用量描述，如 200 / 适量 / 1个',
    unit        VARCHAR(16)           DEFAULT NULL COMMENT '计量单位：g / ml / 个 / 根',
    is_main     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否主料：1主料 0辅料',
    note        VARCHAR(255)          DEFAULT NULL COMMENT '备注，如：去皮切块',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_ingredient_recipe (recipe_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='食材清单';

-- ----------------------------
-- 调料清单（Recipe 1:N）
-- ----------------------------
CREATE TABLE IF NOT EXISTS recipe_seasoning
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    name        VARCHAR(64)  NOT NULL COMMENT '调料名称',
    amount      VARCHAR(32)           DEFAULT NULL COMMENT '用量描述，如 5 / 少许',
    unit        VARCHAR(16)           DEFAULT NULL COMMENT '计量单位：g / ml / 勺',
    note        VARCHAR(255)          DEFAULT NULL COMMENT '备注，如：出锅前放',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_seasoning_recipe (recipe_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='调料清单';

-- ----------------------------
-- 烹饪技巧（Recipe 1:N）
-- ----------------------------
CREATE TABLE IF NOT EXISTS cooking_tip
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    title       VARCHAR(128) NOT NULL COMMENT '技巧标题',
    content     TEXT COMMENT '技巧说明',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_tip_recipe (recipe_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='烹饪技巧';

-- ----------------------------
-- 用户（登录体系）
-- ----------------------------
CREATE TABLE IF NOT EXISTS user
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(128) NOT NULL COMMENT 'PBKDF2 密码摘要',
    salt        VARCHAR(64)  NOT NULL COMMENT '密码盐',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar      VARCHAR(500) DEFAULT NULL COMMENT '头像',
    role        TINYINT  NOT NULL DEFAULT 0 COMMENT '角色：0顾客 1管理员',
    status      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_role (role)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户';

-- ----------------------------
-- 打卡 / 菜谱评论
-- ----------------------------
CREATE TABLE IF NOT EXISTS check_in
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '打卡用户ID',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '菜谱ID',
    content     VARCHAR(500)  DEFAULT NULL COMMENT '打卡文字',
    images      VARCHAR(1000) DEFAULT NULL COMMENT '图片地址，多个用逗号分隔',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_checkin_recipe (recipe_id, deleted, create_time),
    KEY idx_checkin_user (user_id, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='打卡/评论';

-- ----------------------------
-- 打卡回复
-- ----------------------------
CREATE TABLE IF NOT EXISTS check_in_reply
(
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    check_in_id      BIGINT UNSIGNED NOT NULL COMMENT '所属打卡ID',
    user_id          BIGINT UNSIGNED NOT NULL COMMENT '回复人ID',
    reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复人ID，为空表示回复打卡作者',
    content          VARCHAR(500) NOT NULL COMMENT '回复内容',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_reply_checkin (check_in_id, deleted, create_time),
    KEY idx_reply_user (user_id, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='打卡回复';

-- ----------------------------
-- 购物车
-- ----------------------------
CREATE TABLE IF NOT EXISTS cart_item
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '用户ID，演示阶段默认 1',
    recipe_id   BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    quantity    INT      NOT NULL DEFAULT 1 COMMENT '份数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    -- 购物车条目删除走物理删除，因此唯一键不含 deleted
    UNIQUE KEY uk_cart_user_recipe (user_id, recipe_id),
    KEY idx_cart_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='购物车';

-- ----------------------------
-- 订单
-- ----------------------------
CREATE TABLE IF NOT EXISTS orders
(
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no      VARCHAR(32)  NOT NULL COMMENT '订单号',
    user_id       BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '下单用户ID',
    total_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '订单总额',
    pay_amount    DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '应付金额（预留优惠）',
    item_count    INT      NOT NULL DEFAULT 0 COMMENT '菜品总份数',
    status        TINYINT  NOT NULL DEFAULT 0 COMMENT '0待支付 1已下单待制作 2制作中 3已完成 4已取消',
    contact_name  VARCHAR(32)  DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    address       VARCHAR(255) DEFAULT NULL COMMENT '配送地址（堂食可为空）',
    remark        VARCHAR(255) DEFAULT NULL COMMENT '备注：口味、忌口等',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id),
    KEY idx_order_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单';

-- ----------------------------
-- 订单项（下单时的菜品快照）
-- ----------------------------
CREATE TABLE IF NOT EXISTS order_item
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id     BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    recipe_id    BIGINT UNSIGNED NOT NULL COMMENT '食谱ID',
    recipe_name  VARCHAR(128) NOT NULL COMMENT '下单时的菜名快照',
    cover_url    VARCHAR(500) DEFAULT NULL COMMENT '下单时的封面快照',
    price        DECIMAL(10, 2) NOT NULL COMMENT '下单时的单价快照',
    quantity     INT      NOT NULL DEFAULT 1 COMMENT '份数',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单项';
