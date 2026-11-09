CREATE DATABASE IF NOT EXISTS estoque_veiculos
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE estoque_veiculos;

CREATE TABLE IF NOT EXISTS marca (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(60)  NOT NULL,
    pais_origem    VARCHAR(60)  NULL,
    criado_em      DATETIME(6)  NOT NULL,
    atualizado_em  DATETIME(6)  NULL,
    CONSTRAINT pk_marca PRIMARY KEY (id),
    CONSTRAINT uk_marca_nome UNIQUE (nome)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS modelo (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(80)  NOT NULL,
    tipo           VARCHAR(20)  NOT NULL,
    marca_id       BIGINT       NOT NULL,
    criado_em      DATETIME(6)  NOT NULL,
    atualizado_em  DATETIME(6)  NULL,
    CONSTRAINT pk_modelo PRIMARY KEY (id),
    CONSTRAINT uk_modelo_marca UNIQUE (nome, marca_id),
    CONSTRAINT fk_modelo_marca FOREIGN KEY (marca_id) REFERENCES marca (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS veiculo (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    modelo_id       BIGINT         NOT NULL,
    ano_fabricacao  INT            NOT NULL,
    cor             VARCHAR(30)    NOT NULL,
    preco           DECIMAL(12,2)  NOT NULL,
    quilometragem   INT            NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    placa           VARCHAR(10)    NULL,
    criado_em       DATETIME(6)    NOT NULL,
    atualizado_em   DATETIME(6)    NULL,
    CONSTRAINT pk_veiculo PRIMARY KEY (id),
    CONSTRAINT uk_veiculo_placa UNIQUE (placa),
    CONSTRAINT fk_veiculo_modelo FOREIGN KEY (modelo_id) REFERENCES modelo (id),
    CONSTRAINT ck_veiculo_preco CHECK (preco > 0),
    CONSTRAINT ck_veiculo_km CHECK (quilometragem >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_veiculo_status ON veiculo (status);
CREATE INDEX idx_veiculo_preco  ON veiculo (preco);
CREATE INDEX idx_veiculo_ano    ON veiculo (ano_fabricacao);

INSERT INTO marca (nome, pais_origem, criado_em, atualizado_em) VALUES
 ('Toyota', 'Japao', NOW(), NOW()),
 ('Volkswagen', 'Alemanha', NOW(), NOW()),
 ('Honda', 'Japao', NOW(), NOW());

INSERT INTO modelo (nome, tipo, marca_id, criado_em, atualizado_em) VALUES
 ('Corolla', 'SEDAN', 1, NOW(), NOW()),
 ('Polo', 'HATCH', 2, NOW(), NOW()),
 ('Civic', 'SEDAN', 3, NOW(), NOW());

INSERT INTO veiculo (modelo_id, ano_fabricacao, cor, preco, quilometragem, status, placa, criado_em, atualizado_em) VALUES
 (1, 2023, 'Prata', 149900.00, 18500, 'DISPONIVEL', 'RTA1B23', NOW(), NOW()),
 (2, 2024, 'Vermelho', 92900.00, 3200, 'DISPONIVEL', 'POL3D44', NOW(), NOW()),
 (3, 2019, 'Azul', 104900.00, 78300, 'VENDIDO', 'CIV6G89', NOW(), NOW());
