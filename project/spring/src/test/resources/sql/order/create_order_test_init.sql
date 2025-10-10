-- Insert test order for conflict testing
INSERT INTO orders (order_id, customer_id, amount, product_code, status, created_at, processed_at)
VALUES ('ORD-999', 'CUST-999', 150.00, 'PROD-999', 'PENDING', NOW(), NULL);