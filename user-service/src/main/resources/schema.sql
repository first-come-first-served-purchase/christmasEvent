CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    nickname VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    is_deleted BOOLEAN DEFAULT false NOT NULL,
    is_blocked BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMP,
    modified_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member_roles (
    member_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    token_value VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (member_id) REFERENCES member(id)
); 