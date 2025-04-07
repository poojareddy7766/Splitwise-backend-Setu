CREATE TABLE expense_shares (
    id SERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_expense FOREIGN KEY (expense_id) REFERENCES expenses (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);