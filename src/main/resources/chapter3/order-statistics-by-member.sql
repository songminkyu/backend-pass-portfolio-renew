ALTER TABLE ch3_order_stats add index idx_total_amount (total_amount);

explain SELECT
    m.email as memberEmail,
    COUNT(*) as totalOrders,
    SUM(o.total_amount) as totalAmount,
    AVG(o.total_amount) as averageAmount
FROM
    ch3_orders o
JOIN
    ch2_members m
    ON o.member_id = m.id
GROUP BY
    m.email
HAVING
    SUM(o.total_amount) >= 5000
    limit 100;

# 구문 분석
# FOR EACH row in members (외부 테이블) //외부 루프
#    FOR EACH matching row in orders  //내부 루프
#        IF join-condition matches THEN
#            RETURN combined row