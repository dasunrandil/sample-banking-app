CREATE TABLE Account(
                        account_number VARCHAR(36) PRIMARY KEY ,
                        holder_name VARCHAR(250) NOT NULL ,
                        holder_address VARCHAR(500) NOT NULL ,
                        balance DECIMAL(5, 2) NOT NULL DEFAULT 0.0
);

CREATE TABLE Transaction(
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            date DATETIME NOT NULL ,
                            type ENUM('CREDIT', 'DEBIT') NOT NULL ,
                            amount DECIMAL(5, 2) NOT NULL ,
                            account VARCHAR(36) NOT NULL ,
                            CONSTRAINT fk_transaction FOREIGN KEY (account) REFERENCES Account(account_number)
);

ALTER TABLE Transaction ADD COLUMN description VARCHAR(200) NOT NULL ;
ALTER TABLE Account MODIFY COLUMN balance DECIMAL(10, 2) NOT NULL DEFAULT 0.0;
ALTER TABLE Transaction MODIFY COLUMN amount DECIMAL(10, 2) NOT NULL;