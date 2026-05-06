CREATE TABLE produto (
    codigo BINARY(16) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(255) NOT NULL,
    PRIMARY KEY (codigo)
);
