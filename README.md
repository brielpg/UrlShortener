# URL Shortener

## Visão Geral

Este projeto implementa um serviço de encurtamento de URLs com suporte a:

* Geração de URLs curtas a partir de URLs originais.
* Redirecionamento de URLs curtas para suas respectivas URLs originais.
* Alta disponibilidade e capacidade de armazenamento para operação contínua durante 10 anos.

---

# Requisitos Funcionais

## RF-01 — Encurtamento de URL

Dado uma URL original, o sistema deve gerar e retornar uma URL encurtada.

### Exemplo

**Entrada**

```http
POST /api/shorten
```

```json
{
  "url": "https://www.exemplo.com"
}
```

**Saída**

```json
{
  "shortCode": "xkXG8"
}
```

---

## RF-02 — Redirecionamento de URL

Dado um código encurtado, o sistema deve redirecionar o usuário para a URL original.

### Exemplo

```http
GET /api/{shorter_code}
```

**Resposta**

```http
308 Permanent Redirect
Location: https://www.exemplo.com
```

---

# Requisitos Não Funcionais

| ID     | Requisito                                                 |
|--------|-----------------------------------------------------------|
| RNF-01 | Suportar geração de 10.000 URLs por dia                   |
| RNF-02 | O código encurtado deve ser o menor possível              |
| RNF-03 | Utilizar apenas caracteres `[a-z, A-Z, 0-9]`              |
| RNF-04 | Relação de carga estimada: 10 leituras para cada gravação |
| RNF-05 | Comprimento médio das URLs armazenadas: 100 bytes         |
| RNF-06 | Persistência mínima dos dados: 10 anos                    |
| RNF-07 | Operação em alta disponibilidade (24/7)                   |

---

# Capacity Planning

## Quantidade de URLs

O sistema deve suportar:

```text
10.000 URLs/dia x 365 dias x 10 anos
```

Resultado:

```text
36.500.000 URLs
```

---

## Armazenamento

Considerando:

```text
100 bytes por URL
```

Temos:

```text
36.500.000 URLs x 100 bytes
= 3.650.000.000 bytes
≈ 3,40 GB
```

### Capacidade Total

| Métrica               | Valor        |
|-----------------------|--------------|
| URLs armazenadas      | 36,5 milhões |
| Tamanho médio por URL | 100 bytes    |
| Armazenamento total   | ~3,40 GB     |

---

## Throughput

### Escritas

```text
10.000 urls / 24h / 60m / 60s
≈ 0,115 operações/segundo
```

### Leituras

Como a proporção é de 10:1:

```text
0,115 x 10
≈ 1,15 operações/segundo
```

| Operação | Taxa         |
|----------|--------------|
| Escritas | ~0,115 req/s |
| Leituras | ~1,15 req/s  |

---

# Estratégia de Geração dos Códigos

Os códigos curtos utilizarão um alfabeto Base62:

```text
[a-z] + [A-Z] + [0-9]
```

Total:

```text
26 + 26 + 10 = 62 caracteres
```

---

## Capacidade por Comprimento

| Comprimento | Combinações |
|-------------|-------------|
| 1           | 62          |
| 2           | 3.844       |
| 3           | 238.328     |
| 4           | 14.776.336  |
| 5           | 916.132.832 |

Como o sistema precisa armazenar pelo menos: `36.500.000 URLs`

Temos:

```text
62¹ = 62          < 36.5mi
62² = 3.844       < 36.5mi
62³ = 238.328     < 36.5mi
62⁴ = 14.776.336  < 36.5mi
62⁵ = 916.132.832 > 36.5mi
```

Portanto, o tamanho mínimo do código encurtado deve ser: `5 caracteres`

---

# Estratégia de Geração de IDs

Para evitar colisões, simplificar a geração dos códigos e eliminar sequenciais:

1. Utilizar um contador sequencial global no Redis através do comando `INCR`.
2. O valor retornado será ofuscado com o **Hashids** usando um salt secreto, gerando um ID curto não previsível.
3. O ID ofuscado será então convertido para Base62 para compor o código encurtado.
4. O contador pode iniciar em `62⁴` para garantir códigos de 5 caracteres desde o primeiro registro.

### Exemplo

```text
Redis INCR => 14.776.336
Hashids    => "3kYp1"  (não sequencial, ofuscado pelo salt)                                                                   
Base62     => mantém-se como "3kYp1
```

Conversão:

```text
14.776.336 -> Base62 -> "aaaaa"
```

---

# Modelo de Dados

## Tabela URL

```sql
CREATE TABLE urls (
    shorter_code VARCHAR(5) PRIMARY KEY,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Índices

```sql
PRIMARY KEY (shorter_code)
```

---

# API

## Criar URL Encurtada

### Request

```http
POST /api/shorten
```

Body:

```json
{
  "url": "https://www.exemplo.com"
}
```

### Response

```http
201 Created
```

```json
{
  "shortCode": "xkXG8"
}
```

---

## Redirecionar URL

### Request

```http
GET /api/{shorter_code}
```

### Response

```http
308 Permanent Redirect
Location: https://www.exemplo.com
```

---

# Arquitetura

```text
                +----------------+
                | Load Balancer  |
                +--------+-------+
                         |
                         v
                +----------------+
                | Application    |
                | Servers        |
                +------+---------+
                       |
         +-------------+-------------+
         |                           |
         v                           v
+----------------+       +----------------+
| Redis          |       | PostgreSQL     |
| INCR Counter   |       | URL Storage    |
+----------------+       +----------------+
```

---

# Fluxos

## Criação de URL

```text
Cliente
   |
   v
POST /api/shorten
   |
   v
Redis INCR
   |
   v
Conversão Base62
   |
   v
Persistência PostgreSQL
   |
   v
Retorna URL curta
```

---

## Redirecionamento

```text
Cliente
   |
   v
GET /{shorter_code}
   |
   v
Busca no PostgreSQL
   |
   v
308 Permanent Redirect
   |
   v
URL Original
```
