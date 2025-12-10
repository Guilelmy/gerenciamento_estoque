-- 1. Tabela EMPRESA (Usuário do Sistema)
CREATE TABLE empresa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cnpj VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

-- 2. Tabela FORNECEDOR
CREATE TABLE fornecedor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cnpj VARCHAR(20) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    CONSTRAINT fk_fornecedor_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    -- Garante que o CNPJ seja único APENAS dentro da mesma empresa
    CONSTRAINT uk_fornecedor_cnpj_empresa UNIQUE (cnpj, empresa_id)
);

-- 3. Tabela PRODUTO
CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    fornecedor_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    CONSTRAINT fk_produto_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES fornecedor(id),
    CONSTRAINT fk_produto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

-- 4. Tabela ITEM_ESTOQUE
CREATE TABLE item_estoque (
    id BIGSERIAL PRIMARY KEY,
    quantidade_atual INTEGER NOT NULL,
    quantidade_minima INTEGER DEFAULT 0,
    data_atualizacao DATE,
    produto_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    CONSTRAINT fk_estoque_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT fk_estoque_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    -- Garante que o mesmo produto não apareça duplicado no estoque da mesma empresa
    CONSTRAINT uk_estoque_empresa_produto UNIQUE (empresa_id, produto_id) 
);

-- 5. Inserir Empresa Padrão (Senha: 123456)
-- Útil para você conseguir logar logo após rodar o sistema
INSERT INTO empresa (nome, cnpj, email, senha) 
VALUES ('Empresa Admin', '00.000.000/0001-99', 'admin@teste.com', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAtepPIq0.W9G8r.i.v.x7y.z0.u');