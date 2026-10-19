-- =========================================================
-- 初始化演示数据（可重复执行时会先清理）
-- =========================================================
USE system_cookies;

DELETE FROM cooking_step;
DELETE FROM recipe_ingredient;
DELETE FROM recipe_seasoning;
DELETE FROM cooking_tip;
DELETE FROM recipe;
DELETE FROM recipe_category;

-- ----------------------------
-- 分类
-- ----------------------------
INSERT INTO recipe_category (id, name, parent_id, sort, status) VALUES
(1, '家常菜', 0, 1, 1),
(2, '川湘菜', 0, 2, 1),
(3, '汤羹煲', 0, 3, 1),
(4, '主食小吃', 0, 4, 1),
(5, '凉菜', 0, 5, 1);

-- ----------------------------
-- 食谱一：番茄炒蛋
-- ----------------------------
INSERT INTO recipe (id, category_id, name, subtitle, cover_url, description, cuisine, taste, cook_method,
                    difficulty, cook_time, servings, calories, price, original_price, stock, sales, is_recommend, status)
VALUES (1, 1, '番茄炒蛋', '国民下饭菜，10分钟搞定', 'https://img.example.com/recipe/tomato-egg.jpg',
        '酸甜开胃的经典家常菜，蛋嫩汁浓，拌饭一绝。', '家常菜', '酸甜', '炒',
        1, 10, 2, 180, 18.00, 22.00, 50, 128, 1, 1);

INSERT INTO recipe_ingredient (recipe_id, name, amount, unit, is_main, note, sort) VALUES
(1, '番茄', '2', '个', 1, '选成熟度高的，去皮更细腻', 1),
(1, '鸡蛋', '3', '个', 1, '常温蛋更易打发', 2),
(1, '小葱', '1', '根', 0, '切葱花', 3);

INSERT INTO recipe_seasoning (recipe_id, name, amount, unit, note, sort) VALUES
(1, '食盐', '3', 'g', '分两次放', 1),
(1, '白糖', '5', 'g', '中和番茄酸味', 2),
(1, '食用油', '20', 'ml', '炒蛋多放些', 3),
(1, '番茄酱', '10', 'g', '可选，增色增味', 4);

INSERT INTO cooking_step (recipe_id, step_no, title, content, duration, tip) VALUES
(1, 1, '备料', '番茄顶部划十字，开水烫30秒后去皮切块；鸡蛋打散，加少许盐搅匀；小葱切葱花。', 2, '番茄去皮后口感更细腻'),
(1, 2, '炒蛋', '热锅倒入15ml油，油温六成热倒入蛋液，待边缘凝固用铲子从外向内推，八成熟盛出备用。', 2, '鸡蛋不要炒老，八成熟即可出锅'),
(1, 3, '炒番茄', '锅内留底油，下番茄块中火翻炒3分钟，加白糖炒至软烂出汁。', 3, '中火慢炒才能炒出红油'),
(1, 4, '合炒调味', '倒入炒好的鸡蛋，加盐和番茄酱翻炒均匀，让蛋块吸饱汤汁，撒葱花出锅。', 2, '最后收汁不要过干，留些汤汁拌饭');

INSERT INTO cooking_tip (recipe_id, title, content, sort) VALUES
(1, '鸡蛋更嫩的秘诀', '打蛋时加一小勺清水或料酒，炒出来更蓬松鲜嫩。', 1),
(1, '番茄如何快速出汁', '切块后先撒少许盐腌2分钟，下锅很快就能炒出浓郁汤汁。', 2),
(1, '先炒蛋还是先炒番茄', '先炒蛋盛出，再炒番茄，最后回锅合炒，避免鸡蛋变老。', 3);

-- ----------------------------
-- 食谱二：红烧肉
-- ----------------------------
INSERT INTO recipe (id, category_id, name, subtitle, cover_url, description, cuisine, taste, cook_method,
                    difficulty, cook_time, servings, calories, price, original_price, stock, sales, is_recommend, status)
VALUES (2, 1, '秘制红烧肉', '肥而不腻，入口即化', 'https://img.example.com/recipe/braised-pork.jpg',
        '经典本帮红烧肉，糖色红亮、软糯入味，慢炖60分钟收浓汁。', '家常菜', '咸甜', '炖',
        2, 75, 4, 520, 58.00, 68.00, 20, 86, 1, 1);

INSERT INTO recipe_ingredient (recipe_id, name, amount, unit, is_main, note, sort) VALUES
(2, '五花肉', '800', 'g', 1, '选三层肥瘦相间的下五花', 1),
(2, '姜', '4', '片', 0, '', 2),
(2, '大葱', '1', '段', 0, '切段', 3),
(2, '八角', '2', '个', 0, '', 4);

INSERT INTO recipe_seasoning (recipe_id, name, amount, unit, note, sort) VALUES
(2, '冰糖', '30', 'g', '炒糖色用', 1),
(2, '生抽', '30', 'ml', '', 2),
(2, '老抽', '10', 'ml', '上色', 3),
(2, '料酒', '30', 'ml', '去腥', 4),
(2, '食盐', '4', 'g', '最后调味', 5);

INSERT INTO cooking_step (recipe_id, step_no, title, content, duration, tip) VALUES
(2, 1, '焯水去腥', '五花肉切3cm见方的块，冷水下锅加姜片料酒，水开后撇净浮沫捞出，温水冲洗沥干。', 10, '一定冷水下锅，血水才能析出'),
(2, 2, '炒糖色', '锅中放少许油下冰糖，小火慢炒至冰糖融化呈琥珀色，迅速下肉块翻炒上色。', 5, '全程小火，糖色过火会发苦'),
(2, 3, '煸香炖煮', '加葱段姜片八角炒香，淋入生抽老抽料酒，倒入没过肉面2cm的热水，大火烧开转小火炖50分钟。', 50, '中途加热水，冷水会让肉收缩变硬'),
(2, 4, '大火收汁', '拣出葱姜八角，加盐调味，转大火收汁5-8分钟，不停翻动让每块肉都裹上浓汁即可。', 10, '收汁时要守着锅，防止糊底');

INSERT INTO cooking_tip (recipe_id, title, content, sort) VALUES
(2, '肉块不要切太小', '切3cm见方，炖煮后会回缩，太小容易炖散。', 1),
(2, '炒糖色的火候判断', '冰糖由大泡转小泡、颜色呈琥珀色时立刻下肉，晚一秒就苦。', 2),
(2, '解腻小技巧', '炖煮时加几片山楂或一小撮陈皮，肉更易软烂且不腻。', 3);

-- ----------------------------
-- 食谱三：麻婆豆腐
-- ----------------------------
INSERT INTO recipe (id, category_id, name, subtitle, cover_url, description, cuisine, taste, cook_method,
                    difficulty, cook_time, servings, calories, price, original_price, stock, sales, is_recommend, status)
VALUES (3, 2, '麻婆豆腐', '麻辣鲜香，下饭神器', 'https://img.example.com/recipe/mapo-tofu.jpg',
        '川菜经典，麻、辣、烫、香、酥、嫩、鲜、活八字诀，配米饭能多吃两碗。', '川菜', '麻辣', '烧',
        2, 25, 3, 320, 32.00, 38.00, 30, 64, 1, 1);

INSERT INTO recipe_ingredient (recipe_id, name, amount, unit, is_main, note, sort) VALUES
(3, '嫩豆腐', '400', 'g', 1, '切2cm方块', 1),
(3, '牛肉末', '80', 'g', 1, '肥瘦相间更香', 2),
(3, '蒜苗', '2', '根', 0, '切碎', 3),
(3, '姜蒜', '适量', '', 0, '切末', 4);

INSERT INTO recipe_seasoning (recipe_id, name, amount, unit, note, sort) VALUES
(3, '郫县豆瓣酱', '25', 'g', '剁细炒出红油', 1),
(3, '花椒粉', '3', 'g', '出锅前撒', 2),
(3, '辣椒粉', '5', 'g', '', 3),
(3, '生抽', '10', 'ml', '', 4),
(3, '水淀粉', '30', 'ml', '分三次勾芡', 5);

INSERT INTO cooking_step (recipe_id, step_no, title, content, duration, tip) VALUES
(3, 1, '豆腐去豆腥', '豆腐切2cm方块，放入淡盐水中浸泡5分钟，捞出沥干。', 6, '淡盐水泡过的豆腐不易碎'),
(3, 2, '炒肉末', '热锅凉油下牛肉末，中火煸炒至酥香出油，下姜蒜末炒香。', 4, '肉末要炒到微微焦黄才酥香'),
(3, 3, '炒红油', '转小火下郫县豆瓣酱和辣椒粉，慢炒1分钟炒出红油，加生抽和300ml热水烧开。', 3, '豆瓣酱必须小火炒，大火发苦'),
(3, 4, '烧豆腐勾芡', '下豆腐块，轻推锅铲避免弄碎，中火烧3分钟，分三次淋入水淀粉勾芡至汤汁浓稠。', 5, '分三次勾芡，芡汁才亮而不糊'),
(3, 5, '撒花椒出锅', '撒蒜苗碎和花椒粉，轻轻推匀立刻出锅。', 1, '花椒粉一定出锅前放，久煮会发苦');

INSERT INTO cooking_tip (recipe_id, title, content, sort) VALUES
(3, '豆腐不碎的关键', '下锅后不要频繁翻动，用锅铲背部轻推，或晃动锅身让豆腐均匀入味。', 1),
(3, '三次勾芡法', '第一次锁味、第二次增稠、第三次提亮，每次间隔30秒。', 2),
(3, '麻味的层次', '花椒粉出锅前撒，若想更麻可再淋少许花椒油。', 3);

-- ----------------------------
-- 食谱四：冬瓜排骨汤
-- ----------------------------
INSERT INTO recipe (id, category_id, name, subtitle, cover_url, description, cuisine, taste, cook_method,
                    difficulty, cook_time, servings, calories, price, original_price, stock, sales, is_recommend, status)
VALUES (4, 3, '冬瓜排骨汤', '清甜解腻，老少皆宜', 'https://img.example.com/recipe/pork-rib-soup.jpg',
        '小火慢炖的清汤，冬瓜清甜、排骨酥烂，饭前一碗暖胃又解腻。', '家常菜', '清淡', '炖',
        1, 90, 4, 210, 42.00, 48.00, 15, 42, 0, 1);

INSERT INTO recipe_ingredient (recipe_id, name, amount, unit, is_main, note, sort) VALUES
(4, '排骨', '500', 'g', 1, '选肋排，斩小段', 1),
(4, '冬瓜', '400', 'g', 1, '去皮切厚片', 2),
(4, '姜', '3', '片', 0, '', 3),
(4, '香葱', '1', '根', 0, '打结', 4);

INSERT INTO recipe_seasoning (recipe_id, name, amount, unit, note, sort) VALUES
(4, '料酒', '20', 'ml', '焯水去腥', 1),
(4, '食盐', '5', 'g', '出锅前放', 2),
(4, '白胡椒粉', '1', 'g', '提鲜去腥', 3);

INSERT INTO cooking_step (recipe_id, step_no, title, content, duration, tip) VALUES
(4, 1, '排骨焯水', '排骨冷水下锅，加姜片料酒，水开后煮3分钟撇净浮沫，捞出温水冲净。', 10, '焯水后用温水冲洗，汤色才清'),
(4, 2, '小火慢炖', '排骨放入砂锅，加没过食材3cm的热水、姜片和葱结，大火烧开转小火炖60分钟。', 60, '全程保持微沸，汤才清亮不浑浊'),
(4, 3, '下冬瓜', '放入冬瓜片继续炖15分钟，至冬瓜呈半透明状。', 15, '冬瓜别放太早，否则炖化了'),
(4, 4, '调味出锅', '拣出葱结，加盐和白胡椒粉调味，再煮2分钟即可。', 3, '盐最后放，早放肉质会变柴');

INSERT INTO cooking_tip (recipe_id, title, content, sort) VALUES
(4, '汤色清亮的秘诀', '焯水后一定用温水冲洗血沫，炖时全程小火保持"虾眼水"状态。', 1),
(4, '盐什么时候放', '出锅前5分钟放盐，肉质更嫩，汤味更鲜。', 2);

-- ----------------------------
-- 食谱五：蒜蓉粉丝蒸虾
-- ----------------------------
INSERT INTO recipe (id, category_id, name, subtitle, cover_url, description, cuisine, taste, cook_method,
                    difficulty, cook_time, servings, calories, price, original_price, stock, sales, is_recommend, status)
VALUES (5, 1, '蒜蓉粉丝蒸虾', '宴客必杀技，鲜香扑鼻', 'https://img.example.com/recipe/garlic-shrimp.jpg',
        '开背蒸虾，蒜香浓郁、粉丝吸满虾汁，15分钟上桌的硬菜。', '粤菜', '蒜香', '蒸',
        2, 25, 3, 260, 68.00, 78.00, 12, 33, 1, 1);

INSERT INTO recipe_ingredient (recipe_id, name, amount, unit, is_main, note, sort) VALUES
(5, '大虾', '10', '只', 1, '开背去虾线', 1),
(5, '龙口粉丝', '60', 'g', 1, '温水泡软', 2),
(5, '大蒜', '2', '头', 0, '剁成蒜蓉', 3),
(5, '小米辣', '2', '个', 0, '切圈', 4);

INSERT INTO recipe_seasoning (recipe_id, name, amount, unit, note, sort) VALUES
(5, '蒸鱼豉油', '20', 'ml', '出锅后淋', 1),
(5, '食用油', '30', 'ml', '爆香蒜蓉', 2),
(5, '白糖', '3', 'g', '提鲜', 3),
(5, '食盐', '2', 'g', '', 4);

INSERT INTO cooking_step (recipe_id, step_no, title, content, duration, tip) VALUES
(5, 1, '处理虾', '虾剪去虾枪虾须，从背部剪开去虾线，用刀背轻拍虾身使其平整。', 6, '开背深一些，蒜蓉才入味'),
(5, 2, '泡粉丝调蒜蓉', '粉丝温水泡10分钟至软，铺在盘底；蒜蓉分两份，一半用热油炸至金黄，与另一半生蒜混合，加糖盐拌匀。', 10, '金银蒜：一半生一半熟，香味更有层次'),
(5, 3, '摆盘蒸制', '虾摆放在粉丝上，均匀铺上蒜蓉，水开后上锅大火蒸6分钟。', 6, '一定水开后再上锅，计时才准'),
(5, 4, '淋油激香', '出锅撒小米辣和葱花，淋蒸鱼豉油，再浇一勺滚烫热油激出香味。', 2, '最后这勺热油是灵魂');

INSERT INTO cooking_tip (recipe_id, title, content, sort) VALUES
(5, '金银蒜做法', '生蒜辛辣、熟蒜醇厚，两者混合蒜香层次最丰富。', 1),
(5, '蒸虾的时间', '大火6分钟足够，蒸久了虾肉会老。', 2),
(5, '粉丝的处理', '泡软后剪短一些，方便夹取，铺底能接住虾汁。', 3);

-- 登录账号：admin/admin123（管理员）、user1/123456（顾客），密码为 PBKDF2-SHA256 摘要
INSERT IGNORE INTO user (id, username, password, salt, nickname, phone, role, status) VALUES
(1, 'user1', '8e238267a13b34ca3a3263c670d06be72db7b48c044f4e0b654bd621a6d9a4be', '2b7e9c4d1a6f8035', '小吃货', '13800000001', 0, 1),
(2, 'admin', 'f40116e2ef72907b428185b1f48a2d0a6e3e3e196190d66c156469272d9e863b', '8f3c1a7b5e2d9046', '店长', '13800000000', 1, 1);