CREATE TABLE app_user
(
    user_id            UUID                        NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    first_name         VARCHAR(255)                NOT NULL,
    last_name          VARCHAR(255)                NOT NULL,
    email              VARCHAR(255)                NOT NULL,
    password           VARCHAR(255),
    profile_picture_id UUID,
    CONSTRAINT pk_app_user PRIMARY KEY (user_id)
);

CREATE TABLE app_user_role
(
    user_id   UUID         NOT NULL,
    role_name VARCHAR(255) NOT NULL
);

CREATE TABLE client_orders_report
(
    client_orders_report_id       UUID                        NOT NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vending_machine_serial_number VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_client_orders_report PRIMARY KEY (client_orders_report_id)
);

CREATE TABLE item
(
    item_id    UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255)                NOT NULL,
    type       VARCHAR(255)                NOT NULL,
    price      DECIMAL(4, 2)               NOT NULL,
    image_id   UUID,
    CONSTRAINT pk_item PRIMARY KEY (item_id)
);

CREATE TABLE reported_client_order
(
    reported_client_order_id        UUID                        NOT NULL,
    created_at                      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vending_machine_serial_number   VARCHAR(255)                NOT NULL,
    ordered_item_name               VARCHAR(255)                NOT NULL,
    ordered_item_price              DECIMAL                     NOT NULL,
    ordered_at                      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reported_client_order_report_id UUID                        NOT NULL,
    CONSTRAINT pk_reported_client_order PRIMARY KEY (reported_client_order_id)
);

CREATE TABLE status_report
(
    status_report_id              UUID                        NOT NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vending_machine_serial_number VARCHAR(255)                NOT NULL,
    last_intervention             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    measured_temperature          INTEGER                     NOT NULL,
    power_status                  VARCHAR(255)                NOT NULL,
    working_status                VARCHAR(255)                NOT NULL,
    rfid_status                   VARCHAR(255)                NOT NULL,
    smart_card_status             VARCHAR(255)                NOT NULL,
    change_money_status           VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_status_report PRIMARY KEY (status_report_id)
);

CREATE TABLE stock_report
(
    stock_report_id               UUID                        NOT NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vending_machine_serial_number VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_stock_report PRIMARY KEY (stock_report_id)
);

CREATE TABLE stock_report_item_quantity
(
    item_name       VARCHAR(255) NOT NULL,
    quantity_value  INTEGER      NOT NULL,
    stock_report_id UUID         NOT NULL
);

CREATE TABLE uploaded_file
(
    uploaded_file_id UUID                        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name             VARCHAR(255)                NOT NULL,
    content_type     VARCHAR(255)                NOT NULL,
    content          OID                         NOT NULL,
    CONSTRAINT pk_uploaded_file PRIMARY KEY (uploaded_file_id)
);

CREATE TABLE vending_machine
(
    vending_machine_id  UUID                        NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    serial_number       VARCHAR(255)                NOT NULL,
    last_intervention   TIMESTAMP WITHOUT TIME ZONE,
    temperature         INTEGER,
    type                VARCHAR(255),
    power_status        VARCHAR(255),
    working_status      VARCHAR(255),
    rfid_status         VARCHAR(255),
    smart_card_status   VARCHAR(255),
    change_money_status VARCHAR(255),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    street_number       INTEGER                     NOT NULL,
    street_name         VARCHAR(255)                NOT NULL,
    postal_code         VARCHAR(255)                NOT NULL,
    city                VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_vending_machine PRIMARY KEY (vending_machine_id)
);

CREATE TABLE vending_machine_order
(
    vending_machine_order_id UUID                        NOT NULL,
    created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ordered_item_id          UUID                        NOT NULL,
    ordered_item_name        VARCHAR(255)                NOT NULL,
    ordered_item_price       DECIMAL(4, 2)               NOT NULL,
    vending_machine_id       UUID                        NOT NULL,
    CONSTRAINT pk_vending_machine_order PRIMARY KEY (vending_machine_order_id)
);

CREATE TABLE vending_machine_stock
(
    quantity           INTEGER NOT NULL,
    vending_machine_id UUID    NOT NULL,
    item_id            UUID    NOT NULL,
    CONSTRAINT pk_vending_machine_stock PRIMARY KEY (vending_machine_id, item_id)
);

ALTER TABLE stock_report_item_quantity
    ADD CONSTRAINT pk_stock_report_item_quantity PRIMARY KEY (stock_report_id, item_name);

ALTER TABLE item
    ADD CONSTRAINT UK_ITEM_IMAGE UNIQUE (image_id);

ALTER TABLE item
    ADD CONSTRAINT UK_ITEM_NAME UNIQUE (name);

ALTER TABLE uploaded_file
    ADD CONSTRAINT UK_UPLOADED_FILE_NAME UNIQUE (name);

ALTER TABLE app_user
    ADD CONSTRAINT UK_USER_EMAIL UNIQUE (email);

ALTER TABLE vending_machine
    ADD CONSTRAINT UK_VENDING_MACHINE_SERIAL_NUMBER UNIQUE (serial_number);

ALTER TABLE app_user
    ADD CONSTRAINT uc_app_user_profile_picture UNIQUE (profile_picture_id);

CREATE INDEX IDS_ITEM_NAME ON item (name);

CREATE INDEX IDX_CLIENT_ORDERS_REPORT_CREATED_AT ON client_orders_report (created_at);

CREATE INDEX IDX_CLIENT_ORDERS_REPORT_VENDING_MACHINE_SERIAL_NUMBER ON client_orders_report (vending_machine_serial_number);

CREATE INDEX IDX_ITEM_TYPE ON item (type);

CREATE INDEX IDX_ORDER_ITEM ON vending_machine_order (ordered_item_id);

CREATE INDEX IDX_STATUS_REPORT_CREATED_AT ON status_report (created_at);

CREATE INDEX IDX_STATUS_REPORT_VENDING_MACHINE_SERIAL_NUMBER ON status_report (vending_machine_serial_number);

CREATE INDEX IDX_STOCK_REPORT_CREATED_AT ON stock_report (created_at);

CREATE INDEX IDX_STOCK_REPORT_VENDING_MACHINE_SERIAL_NUMBER ON stock_report (vending_machine_serial_number);

CREATE INDEX IDX_UPLOADED_FILE_CONTENT_TYPE_NAME ON uploaded_file (name, content_type);

CREATE INDEX IDX_USER_EMAIL ON app_user (email);

CREATE INDEX IDX_USER_FIRST_NAME ON app_user (first_name);

CREATE INDEX IDX_USER_FIRST_NAME_LAST_NAME ON app_user (first_name, last_name);

CREATE INDEX IDX_USER_LAST_NAME ON app_user (last_name);

CREATE INDEX IDX_USER_ROLE_NAME ON app_user_role (role_name);

CREATE INDEX IDX_VENDING_MACHINE_ADDRESS ON vending_machine (street_number, street_name, postal_code, city);

CREATE INDEX IDX_VENDING_MACHINE_CITY ON vending_machine (postal_code, city);

CREATE INDEX IDX_VENDING_MACHINE_POSITION ON vending_machine (latitude, longitude);

CREATE INDEX IDX_VENDING_MACHINE_POWER_STATUS ON vending_machine (power_status);

CREATE INDEX IDX_VENDING_MACHINE_TYPE ON vending_machine (type);

CREATE INDEX IDX_VENDING_MACHINE_WORKING_STATUS ON vending_machine (power_status, working_status);

ALTER TABLE item
    ADD CONSTRAINT FK_ITEM_IMAGE_ID FOREIGN KEY (image_id) REFERENCES uploaded_file (uploaded_file_id);

ALTER TABLE vending_machine_order
    ADD CONSTRAINT FK_ORDER_VENDING_MACHINE FOREIGN KEY (vending_machine_id) REFERENCES vending_machine (vending_machine_id);

CREATE INDEX IDX_ORDER_VENDING_MACHINE ON vending_machine_order (vending_machine_id);

ALTER TABLE reported_client_order
    ADD CONSTRAINT FK_REPORTED_CLIENT_ORDER_ENTRY_REPORT FOREIGN KEY (reported_client_order_report_id) REFERENCES client_orders_report (client_orders_report_id);

ALTER TABLE vending_machine_stock
    ADD CONSTRAINT FK_STOCK_ITEM FOREIGN KEY (item_id) REFERENCES item (item_id);

ALTER TABLE stock_report_item_quantity
    ADD CONSTRAINT FK_STOCK_REPORT_ITEM_QUANTITY_REPORT FOREIGN KEY (stock_report_id) REFERENCES stock_report (stock_report_id);

CREATE INDEX IDX_STOCK_REPORT_ENTRY_STOCK ON stock_report_item_quantity (stock_report_id);

ALTER TABLE vending_machine_stock
    ADD CONSTRAINT FK_STOCK_VENDING_MACHINE FOREIGN KEY (vending_machine_id) REFERENCES vending_machine (vending_machine_id);

CREATE INDEX IDX_STOCK_VENDING_MACHINE ON vending_machine_stock (vending_machine_id);

ALTER TABLE app_user
    ADD CONSTRAINT FK_USER_PICTURE_ID FOREIGN KEY (profile_picture_id) REFERENCES uploaded_file (uploaded_file_id);

ALTER TABLE app_user_role
    ADD CONSTRAINT FK_USER_ROLE_USER_ID FOREIGN KEY (user_id) REFERENCES app_user (user_id);
