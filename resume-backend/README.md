# Resume Backend

Spring Boot 3.5 backend for AI-assisted LaTeX resume/cover-letter generation.

## Run
- Dev (H2, swagger): `./gradlew bootRun` (defaults to `dev` profile).
- Tests: `./gradlew test` (uses `application-test.yml`).
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Key Endpoints
- `GET /api/ping` – health check.
- Profiles CRUD: `GET/POST/PUT/DELETE /api/profiles`
- Job descriptions: `GET/POST /api/job-descriptions`
- Generation: `POST /api/generation/resume` (LaTeX pipeline), `GET /api/generation/resume/{id}/download` (returns generated PDF).

## Config
- `application-dev.yml`: H2 + seed default LaTeX template; OpenAI key via `SPRING_AI_OPENAI_API_KEY`; security disabled for dev.
- `application-prod.yml`: Postgres placeholders; set datasource + OpenAI env vars; security enabled with `APP_SECURITY_USERNAME`/`APP_SECURITY_PASSWORD`.
- `app.latex.output-dir`: where generated PDFs are stored.
- `app.ai.keyword-extraction`: `basic` (default) or `ai` (stubbed hook).
