# Sistema Automotivo: Gestão de Estoque de Veículos

API REST CRUD orientada a objetos para cadastrar, consultar, filtrar, atualizar e remover veículos de uma concessionária.

**Tecnologias:** Java 17+, Spring Boot 3.3, Spring Data JPA (Hibernate), Bean Validation, MySQL 8, frontend em HTML/CSS/JavaScript puro.

## Estrutura

```
estoque-veiculos/
├── database/schema.sql              Modelo físico MySQL (tabelas, chaves, índices, dados de exemplo)
├── requisicoes.http                 Requisições prontas para testar a API (VS Code / IntelliJ)
├── src/main/java/br/com/unifecaf/estoque/
│   ├── model/        Entidades (EntidadeBase, Marca, Modelo, Veiculo) e enums
│   ├── dto/          Objetos de entrada e saída da API (records)
│   ├── repository/   Interfaces Spring Data + filtros dinâmicos (Specification)
│   ├── service/      Regras de negócio
│   ├── controller/   Endpoints REST
│   ├── exception/    Exceções e tratamento global de erros
│   └── config/       Carga inicial de dados de exemplo
├── src/main/resources/
│   ├── application.properties       Configuração do MySQL
│   ├── application-dev.properties   Perfil "dev" com H2 em memória (sem MySQL)
│   └── static/                      Frontend (index.html, style.css, app.js)
└── src/test/java/...                Testes de integração da API
```

Arquitetura em camadas (MVC/REST): **Controller → Service → Repository → Banco**.

## Como executar

### Opção 1: com MySQL (padrão)

1. Tenha o MySQL 8 rodando em `localhost:3306`.
2. Ajuste usuário e senha em `src/main/resources/application.properties` (padrão `root` / `root`) ou defina as variáveis `DB_USER` e `DB_PASSWORD`.
3. Rode:
   ```bash
   mvn spring-boot:run
   ```
   O banco `estoque_veiculos` e as tabelas são criados automaticamente. Opcionalmente, rode `database/schema.sql` antes.

### Opção 2: sem instalar MySQL (H2 em memória)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Depois abra **http://localhost:8080** para usar o frontend.

> Sem Maven instalado? Abra a pasta no IntelliJ IDEA ou no VS Code (Extension Pack for Java) e rode a classe `EstoqueVeiculosApplication`.

### Testes

```bash
mvn test
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/marcas` | Lista marcas |
| POST | `/api/marcas` | Cadastra marca |
| PUT | `/api/marcas/{id}` | Edita marca |
| DELETE | `/api/marcas/{id}` | Remove marca (bloqueado se tiver modelos) |
| GET | `/api/modelos?marcaId=` | Lista modelos (opcionalmente por marca) |
| POST | `/api/modelos` | Cadastra modelo ligado a uma marca |
| PUT | `/api/modelos/{id}` | Edita modelo |
| DELETE | `/api/modelos/{id}` | Remove modelo (bloqueado se tiver veículos) |
| GET | `/api/veiculos` | Consulta com filtros |
| GET | `/api/veiculos/{id}` | Busca por id |
| GET | `/api/veiculos/resumo` | Indicadores do estoque |
| POST | `/api/veiculos` | Cadastra veículo |
| PUT | `/api/veiculos/{id}` | Substitui todos os dados |
| PATCH | `/api/veiculos/{id}` | Atualiza preço, quilometragem e/ou status |
| POST | `/api/veiculos/{id}/vender` | Marca como vendido |
| DELETE | `/api/veiculos/{id}` | Remove do estoque |

Filtros de `GET /api/veiculos`: `marcaId`, `modeloId`, `tipo`, `status`, `precoMin`, `precoMax`, `anoMin`, `anoMax`, `cor`, `busca`, `ordenarPor` (`preco`, `anoFabricacao`, `quilometragem`, `atualizadoEm`) e `direcao` (`asc`/`desc`).

Exemplo: `GET /api/veiculos?status=DISPONIVEL&precoMax=120000&anoMin=2020&ordenarPor=preco&direcao=asc`

## Regras de negócio

- Preço sempre maior que zero; quilometragem nunca negativa e nunca menor que a atual.
- Ano de fabricação entre 1950 e o ano seguinte ao atual.
- Placa única (aceita padrão antigo e Mercosul); nome de marca único; modelo único por marca.
- Marca com modelos e modelo com veículos não podem ser removidos.
- Um veículo já vendido não pode ser vendido de novo.
- Erros voltam em JSON padronizado (400, 404 ou 409).
