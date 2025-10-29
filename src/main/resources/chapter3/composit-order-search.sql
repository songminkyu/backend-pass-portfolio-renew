ALTER TABLE ch3_orders ADD INDEX idx_order_date_status_amount (order_date desc ,total_amount,status);

explain select
            o1_0.id,
            o1_0.created_at,
            o1_0.member_id,
            o1_0.order_date,
            o1_0.order_number,
            o1_0.status,
            o1_0.total_amount,
            o1_0.updated_at
        from
            ch3_orders o1_0
        where
            o1_0.order_date>='2023-01-01T00:00'
          and o1_0.status='COMPLETED'
          and o1_0.total_amount>=500
        order by
            o1_0.order_date desc
            limit
        100;