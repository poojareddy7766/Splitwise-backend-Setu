CREATE TABLE user_balance (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    other_user_id BIGINT NOT NULL,
    balance_amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_other_user FOREIGN KEY (other_user_id) REFERENCES users (id)
);