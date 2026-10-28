-- 素材制作网站数据库结构（幂等，可重复执行）

CREATE TABLE IF NOT EXISTS app_user (
    id         VARCHAR(64) PRIMARY KEY,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS template (
    id                   VARCHAR(64) PRIMARY KEY,
    title                VARCHAR(255) NOT NULL,
    category             VARCHAR(100) NOT NULL,
    cover_url            VARCHAR(500) NOT NULL,
    description          TEXT,
    required_image_count INT          NOT NULL,
    accepted_mime_types  VARCHAR(500) NOT NULL,
    max_file_size_bytes  BIGINT       NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS asset (
    id            VARCHAR(64) PRIMARY KEY,
    user_id       VARCHAR(64)  NOT NULL,
    original_name VARCHAR(255),
    stored_path   VARCHAR(500) NOT NULL,
    mime_type     VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_asset_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS task (
    id            VARCHAR(64) PRIMARY KEY,
    user_id       VARCHAR(64)  NOT NULL,
    template_id   VARCHAR(64)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    current_stage VARCHAR(20),
    fail_stage    VARCHAR(20),
    fail_reason   VARCHAR(500),
    result_url    VARCHAR(500),
    asset_ids     TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_task_user (user_id),
    KEY idx_task_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;