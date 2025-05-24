```sql
SELECT
  t1.customer_id,
  t1.first_name,
  t1.last_name,
  SUBSTR(t1.email, 1, 10) AS email_prefix,
  TRIM(SUBSTR(t1.comments, 5, 20)) AS comment_snippet,
  COALESCE(t3.discount, 0) AS discount_amount,
  SUM(t2.order_amount) OVER (
    PARTITION BY t1.customer_id
    ORDER BY t2.order_date
    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
  ) AS running_total,
  CASE
    WHEN t2.order_amount > 1000 THEN 'High Value'
    WHEN t2.order_amount > 500 THEN SUBSTR(t1.region, 1, 3)
    ELSE 'Standard'
  END AS order_category,
  EXTRACT(YEAR FROM t2.order_date) AS order_year,
  t4.source_system
FROM customers AS t1
INNER JOIN orders AS t2
  ON t1.customer_id = t2.customer_id
LEFT JOIN (
  SELECT *
  FROM payments
  WHERE payment_date >= DATE '2024-01-01'
) AS t3
  ON t2.order_id = t3.order_id
RIGHT JOIN regions AS t4
  ON t1.region_id = t4.region_id
FULL OUTER JOIN (
  SELECT *
  FROM sales_archive
  WHERE sale_year = 2024
) AS sa
  ON t1.customer_id = sa.customer_id
WHERE t1.active_flag = 'Y'
QUALIFY ROW_NUMBER() OVER (
  PARTITION BY t1.customer_id
  ORDER BY t2.order_date DESC
) = 1
ORDER BY t1.customer_id;
```
