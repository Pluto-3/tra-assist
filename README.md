# TRA Assist

A RAG-powered tax guidance assistant for small businesses in Tanzania, accessible via Telegram. Built entirely with local, open-source tools — zero API costs, zero cloud dependency.

---

## What It Does

TRA Assist answers tax questions by retrieving relevant information from official TRA (Tanzania Revenue Authority) documents and generating structured, plain-language responses using a local LLM. Every answer includes:

- A direct answer
- A plain-language explanation
- Step-by-step action items
- A confidence indicator (HIGH / MEDIUM / LOW)
- A mandatory disclaimer directing users to verify with TRA

---

## Tech Stack

| Component | Technology |
|---|---|
| Backend | Spring Boot 3.2.5, Java 21 |
| RAG Framework | LangChain4j 0.31.0 |
| Embedding Model | nomic-embed-text (via Ollama) |
| LLM | Mistral 7B (via Ollama) |
| Vector DB | PostgreSQL 16 + pgvector |
| PDF Extraction | Apache PDFBox 3.0.2 |
| Messaging | Telegram Bot API |
| Containerization | Docker + docker-compose |

---

## Prerequisites

- Java 21
- Maven
- Docker + docker-compose
- Ollama with `nomic-embed-text` and `mistral` pulled
- A Telegram bot token from [@BotFather](https://t.me/BotFather)

---

## Setup

**1. Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/tra-assist.git
cd tra-assist
```

**2. Start PostgreSQL with pgvector**
```bash
docker-compose up -d
```

**3. Add the vector column (first run only)**
```bash
docker exec -it tra-postgres psql -U trauser -d traassist \
  -c "ALTER TABLE document_chunks ADD COLUMN IF NOT EXISTS embedding vector(768);"
```

**4. Add TRA documents**

Drop TRA PDF files into `src/main/resources/documents/`. The ingestion pipeline runs automatically on startup.

**5. Start Ollama**
```bash
ollama serve
```

**6. Set environment variables**
```bash
export BOT_TOKEN=your_telegram_bot_token
export BOT_USERNAME=your_bot_username
```

**7. Run the application**
```bash
mvn spring-boot:run
```

**8. Verify everything is running**
```bash
curl http://localhost:8080/health
# Expected: {"status":"ok","ollama":"reachable","db":"connected"}
```

---

## Usage

### Via Telegram
Find your bot on Telegram and send any tax question in English:

> *What taxes does a small business pay in Tanzania?*

> *When is the deadline to file VAT returns?*

> *What is the VAT rate?*

### Via REST API
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "What is the VAT rate in Tanzania?"}'
```

**Sample response:**
```json
{
  "answer": "ANSWER: The standard VAT rate in Tanzania is 18%...\n\nWHAT THIS MEANS: ...\n\nWHAT TO DO NEXT:\n1. ...\n\nCONFIDENCE: HIGH\n\n⚠️ Verify with TRA before acting: www.tra.go.tz",
  "sources": ["TAXES_AND_DUTIES_AT_A_GLANCE_2025_2026.pdf"],
  "topSimilarity": 0.638,
  "confidence": "MEDIUM"
}
```

---

## Configuration

`src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/traassist
    username: trauser
    password: trapass

ollama:
  base-url: http://localhost:11434
  embedding-model: nomic-embed-text
  chat-model: mistral

telegram:
  bot-token: ${BOT_TOKEN}
  bot-username: ${BOT_USERNAME}
```

---

## Project Structure

```
src/main/java/com/traassist/tra_assist/
├── config/          # Ollama and database configuration beans
├── document/        # PDF ingestion pipeline (extract, chunk, embed, store)
├── rag/             # Retrieval, generation, confidence, disclaimer
├── language/        # Swahili detection and translation
├── observability/   # Query logging to database
├── telegram/        # Telegram bot handler
├── api/             # REST API controller
└── health/          # Health check endpoint
```

---

## Adding Documents

Drop any TRA PDF into `src/main/resources/documents/` and restart the application. The ingestion service automatically processes new files and skips already-ingested ones. More documents = better retrieval coverage.

---

## Reviewing Query Logs

```bash
docker exec -it tra-postgres psql -U trauser -d traassist \
  -c "SELECT query, confidence, top_similarity, response_time_ms, created_at FROM query_logs ORDER BY created_at DESC LIMIT 20;"
```

---

## Known Limitations

- **Swahili quality is poor** — Mistral is not a strong Swahili model. English queries are recommended until a better model is available.
- **GPU acceleration** — Ollama may run Mistral on CPU depending on driver setup, resulting in slow responses (30–180 seconds). See [Ollama GPU docs](https://github.com/ollama/ollama/blob/main/docs/gpu.md).
- **Single document coverage** — Retrieval quality improves significantly with more TRA documents ingested.
- **No authentication** — The bot accepts queries from any Telegram user. Add rate limiting before production deployment.
- **This is guidance only** — All responses include a disclaimer. Always verify with TRA at [www.tra.go.tz](https://www.tra.go.tz).

---

## Disclaimer

TRA Assist is a guidance tool only. It is not a substitute for professional tax advice. TRA rules change regularly. Always verify any information provided by this system with an official TRA office or at [www.tra.go.tz](https://www.tra.go.tz) before making tax decisions.

---

## License

MIT
