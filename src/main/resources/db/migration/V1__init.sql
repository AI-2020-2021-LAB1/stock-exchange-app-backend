CREATE SEQUENCE USER_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE SEQUENCE STOCK_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE SEQUENCE ORDER_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE SEQUENCE USER_STOCK_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;
	
CREATE SEQUENCE TRANSACTION_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;
	
CREATE SEQUENCE ORDER_TRANSACTION_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;	

CREATE TABLE USERS
(
    ID         bigint DEFAULT NEXTVAL('USER_SEQUENCE') PRIMARY KEY,
    FIRST_NAME varchar(255)        NOT NULL,
    LAST_NAME  varchar(255)        NOT NULL,
    EMAIL      varchar(255) UNIQUE NOT NULL,
    PASSWORD   varchar(255)        NOT NULL,
    ROLE       varchar(255)        NOT NULL,
    MONEY      decimal             NOT NULL
);

CREATE TABLE STOCKS
(
    ID            bigint DEFAULT NEXTVAL('STOCK_SEQUENCE') PRIMARY KEY,
    NAME          varchar(255) UNIQUE NOT NULL,
    ABBREVIATION  varchar(3) UNIQUE NOT NULL,
    CURRENT_PRICE decimal,
    AMOUNT        int
);

CREATE TABLE ORDERS
(
    ID       bigint DEFAULT NEXTVAL('ORDER_SEQUENCE') PRIMARY KEY,
    USER_ID  bigint NOT NULL,
    STOCK_ID bigint NOT NULL,
	AMOUNT int NOT NULL,
	REMAINING_AMOUNT int NOT NULL,
	TYPE varchar(255) NOT NULL,
	PRICE_TYPE varchar(255) NOT NULL,
	PRICE decimal NOT NULL,
	DATE_CREATION timestamp NOT NULL,
	DATE_EXPIRATION timestamp NOT NULL,
	DATE_CLOSING timestamp NOT NULL,
    FOREIGN KEY (USER_ID)
        REFERENCES USERS (ID),
    FOREIGN KEY (STOCK_ID)
        REFERENCES STOCKS (ID)
);

CREATE TABLE USERS_STOCKS
(
    ID       bigint DEFAULT NEXTVAL('USER_STOCK_SEQUENCE') PRIMARY KEY,
    USER_ID  bigint NOT NULL,
    STOCK_ID bigint NOT NULL,
	AMOUNT int NOT NULL,
    FOREIGN KEY (USER_ID)
        REFERENCES USERS (ID),
    FOREIGN KEY (STOCK_ID)
        REFERENCES STOCKS (ID)
);

CREATE TABLE TRANSACTIONS
(
    ID       bigint DEFAULT NEXTVAL('TRANSACTION_SEQUENCE') PRIMARY KEY,
    DATE  timestamp NOT NULL,
    UNIT_PRICE decimal NOT NULL,
	AMOUNT int NOT NULL
);

CREATE TABLE ORDERS_TRANSACTIONS
(
    ID       bigint DEFAULT NEXTVAL('ORDER_TRANSACTION_SEQUENCE') PRIMARY KEY,
    ORDER_ID  bigint NOT NULL,
    TRANSACTION_ID bigint NOT NULL,
    FOREIGN KEY (ORDER_ID)
        REFERENCES ORDERS (ID),
    FOREIGN KEY (TRANSACTION_ID)
        REFERENCES TRANSACTIONS (ID)
);