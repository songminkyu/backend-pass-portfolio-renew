-- Session A and B
-- dbeaver열어서 쿼리 스크립트창 2개 아래에 동일하게 복사 붙혀놓고 테스트 진행
START TRANSACTION;
SELECT * FROM book WHERE id = 1 LOCK IN SHARE MODE;  -- S-Lock 적용
UPDATE book SET price = price - 1000 WHERE id = 1;
COMMIT;
