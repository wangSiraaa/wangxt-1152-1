-- =============================================
-- 林业有害生物诱捕监测系统 - 数据库表结构
-- =============================================

-- =============================================
-- 1. 用户表（护林员、检疫员、处置队）
-- =============================================
CREATE TABLE forest_user (
    pk_forest_user      CHAR(20)        NOT NULL,
    user_code           VARCHAR(50)     NOT NULL,
    user_name           VARCHAR(100)    NOT NULL,
    user_role           INTEGER         NOT NULL,
    phone               VARCHAR(20),
    id_card             VARCHAR(30),
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    modifier            CHAR(20),
    modifiedtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_forest_user PRIMARY KEY (pk_forest_user)
);

COMMENT ON TABLE forest_user IS '林业用户表';
COMMENT ON COLUMN forest_user.user_role IS '角色：1-护林员 2-检疫员 3-处置队';
COMMENT ON COLUMN forest_user.dr IS '删除标记：0-正常 1-已删除';

-- =============================================
-- 2. 诱捕器点位表
-- =============================================
CREATE TABLE forest_trap (
    pk_forest_trap      CHAR(20)        NOT NULL,
    trap_code           VARCHAR(50)     NOT NULL,
    trap_name           VARCHAR(200),
    longitude           DECIMAL(15,10)  NOT NULL,
    latitude            DECIMAL(15,10)  NOT NULL,
    location_desc       VARCHAR(500),
    forest_type         VARCHAR(50),
    trap_type           VARCHAR(50),
    install_date        CHAR(10),
    is_key_patrol       INTEGER         DEFAULT 0,
    key_patrol_reason   VARCHAR(500),
    pk_ranger           CHAR(20),
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    modifier            CHAR(20),
    modifiedtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_forest_trap PRIMARY KEY (pk_forest_trap)
);

COMMENT ON TABLE forest_trap IS '诱捕器点位表';
COMMENT ON COLUMN forest_trap.is_key_patrol IS '重点巡查：0-否 1-是';
COMMENT ON COLUMN forest_trap.pk_ranger IS '负责护林员';

-- =============================================
-- 3. 诱捕记录表（护林员登记虫情数量）
-- =============================================
CREATE TABLE forest_trap_record (
    pk_trap_record      CHAR(20)        NOT NULL,
    pk_forest_trap      CHAR(20)        NOT NULL,
    record_date         CHAR(10)        NOT NULL,
    insect_type         VARCHAR(100)    NOT NULL,
    insect_count        INTEGER         NOT NULL,
    is_suspect_quarantine INTEGER      DEFAULT 0,
    suspect_remark      VARCHAR(500),
    record_status       INTEGER         DEFAULT 0,
    risk_level          INTEGER,
    pk_ranger           CHAR(20)        NOT NULL,
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    modifier            CHAR(20),
    modifiedtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_trap_record PRIMARY KEY (pk_trap_record)
);

COMMENT ON TABLE forest_trap_record IS '诱捕记录表';
COMMENT ON COLUMN forest_trap_record.is_suspect_quarantine IS '疑似检疫对象：0-否 1-是';
COMMENT ON COLUMN forest_trap_record.record_status IS '状态：0-待复核 1-已复核 2-已处置';
COMMENT ON COLUMN forest_trap_record.risk_level IS '风险等级：1-低 2-中 3-高';

-- =============================================
-- 4. 复核记录表（检疫员确认风险等级）
-- =============================================
CREATE TABLE forest_review (
    pk_forest_review    CHAR(20)        NOT NULL,
    pk_trap_record      CHAR(20)        NOT NULL,
    review_date         CHAR(10)        NOT NULL,
    risk_level          INTEGER         NOT NULL,
    is_quarantine       INTEGER         DEFAULT 0,
    review_remark       VARCHAR(1000),
    review_result       VARCHAR(500),
    is_allow_disposal   INTEGER         DEFAULT 0,
    pk_quarantine       CHAR(20)        NOT NULL,
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    modifier            CHAR(20),
    modifiedtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_forest_review PRIMARY KEY (pk_forest_review)
);

COMMENT ON TABLE forest_review IS '复核记录表';
COMMENT ON COLUMN forest_review.risk_level IS '风险等级：1-低 2-中 3-高';
COMMENT ON COLUMN forest_review.is_quarantine IS '确认检疫对象：0-否 1-是';
COMMENT ON COLUMN forest_review.is_allow_disposal IS '允许清理：0-否 1-是';
COMMENT ON COLUMN forest_review.pk_quarantine IS '复核检疫员';

-- =============================================
-- 5. 处置记录表（处置队清理疫木）
-- =============================================
CREATE TABLE forest_disposal (
    pk_forest_disposal  CHAR(20)        NOT NULL,
    pk_trap_record      CHAR(20)        NOT NULL,
    disposal_date       CHAR(10)        NOT NULL,
    disposal_type       VARCHAR(50),
    disposal_method     VARCHAR(200),
    disposal_area       DECIMAL(10,2),
    tree_count          INTEGER,
    disposal_remark     VARCHAR(1000),
    has_photo           INTEGER         DEFAULT 0,
    disposal_status     INTEGER         DEFAULT 0,
    pk_disposal_team    CHAR(20)        NOT NULL,
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    modifier            CHAR(20),
    modifiedtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_forest_disposal PRIMARY KEY (pk_forest_disposal)
);

COMMENT ON TABLE forest_disposal IS '处置记录表';
COMMENT ON COLUMN forest_disposal.has_photo IS '是否拍照：0-否 1-是';
COMMENT ON COLUMN forest_disposal.disposal_status IS '处置状态：0-待处置 1-处置中 2-已完成';

-- =============================================
-- 6. 处置照片表
-- =============================================
CREATE TABLE forest_disposal_photo (
    pk_disposal_photo   CHAR(20)        NOT NULL,
    pk_forest_disposal  CHAR(20)        NOT NULL,
    photo_url           VARCHAR(500)    NOT NULL,
    photo_type          VARCHAR(50),
    photo_remark        VARCHAR(200),
    upload_date         CHAR(19),
    pk_org              CHAR(20)        NOT NULL,
    pk_group            CHAR(20)        NOT NULL,
    creator             CHAR(20),
    creationtime        CHAR(19),
    dr                  INTEGER         DEFAULT 0,
    ts                  CHAR(19),
    CONSTRAINT pk_disposal_photo PRIMARY KEY (pk_disposal_photo)
);

COMMENT ON TABLE forest_disposal_photo IS '处置照片表';

-- =============================================
-- 索引
-- =============================================
CREATE INDEX idx_trap_record_trap ON forest_trap_record(pk_forest_trap);
CREATE INDEX idx_trap_record_status ON forest_trap_record(record_status);
CREATE INDEX idx_trap_record_risk ON forest_trap_record(risk_level);
CREATE INDEX idx_review_record ON forest_review(pk_trap_record);
CREATE INDEX idx_disposal_record ON forest_disposal(pk_trap_record);
CREATE INDEX idx_disposal_photo ON forest_disposal_photo(pk_forest_disposal);
CREATE INDEX idx_trap_key_patrol ON forest_trap(is_key_patrol);

-- =============================================
-- 枚举值说明
-- =============================================
-- user_role: 1=护林员 2=检疫员 3=处置队
-- risk_level: 1=低风险 2=中风险 3=高风险
-- record_status: 0=待复核 1=已复核 2=已处置
-- disposal_status: 0=待处置 1=处置中 2=已完成
-- is_key_patrol: 0=否 1=是
-- is_suspect_quarantine: 0=否 1=是
-- is_quarantine: 0=否 1=是
-- is_allow_disposal: 0=否 1=是
-- has_photo: 0=否 1=是
