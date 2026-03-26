-- 1. members 테이블
CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nickname VARCHAR(20) NOT NULL,
                         bio VARCHAR(255),
                         profile_image VARCHAR(255),
                         oauth_id VARCHAR(255),
                         oauth_provider VARCHAR(255),
                         email VARCHAR(255) NOT NULL,
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                         nickname_updated_at DATETIME,
                         deleted_at DATETIME,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT uc_members_email UNIQUE (email)
);
CREATE INDEX ix_members_email ON members(email);

-- 2. teams 테이블
CREATE TABLE teams (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(40) NOT NULL,
                       description VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(20) NOT NULL,
                       address VARCHAR(255) NOT NULL,
                       searchable TINYINT(1) NOT NULL,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT uc_team_name UNIQUE (name)
);

-- 3. participants 테이블
CREATE TABLE participants (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              team_id BIGINT NOT NULL,
                              member_id BIGINT NOT NULL,
                              team_member_role VARCHAR(50) NOT NULL,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT uc_participant UNIQUE (team_id, member_id),
                              FOREIGN KEY (team_id) REFERENCES teams(id),
                              FOREIGN KEY (member_id) REFERENCES members(id)
);