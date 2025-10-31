create database IF NOT EXISTS school; 
use school;
SET SESSION cte_max_recursion_depth = 1000000;
CREATE TABLE IF NOT EXISTS book (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price INT NOT NULL,
    is_adult BOOLEAN NOT NULL,
    published_at DATE NOT NULL
);

-- 100만 개의 책 데이터 생성 및 삽입
INSERT INTO book (name, category, price, is_adult, published_at)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 1000000
)
SELECT
    CONCAT('Book ', n), -- 책 이름
    CASE MOD(n, 5)
        WHEN 0 THEN 'Fiction'
        WHEN 1 THEN 'Non-fiction'
        WHEN 2 THEN 'Science'
        WHEN 3 THEN 'History'
        ELSE 'Art'
    END, -- 카테고리
    FLOOR(9000 + (RAND() * 100000)), -- 가격
    CASE MOD(n, 2)
        WHEN 0 THEN TRUE
        ELSE FALSE
    END, -- 성인 여부
    DATE_ADD('2000-01-01', INTERVAL FLOOR(RAND() * 9000) DAY) -- 출판일
FROM numbers;