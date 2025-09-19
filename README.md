# Weather API – Grupp 3

Ett skolprojekt för att bygga ett weather API med Spring Boot, databas och cache-funktionalitet.

## Teknisk Stack

- **Java 17+** med Spring Boot 3.5.5
- **PostgreSQL** för datalagring
- **Redis** för cache (5 minuters TTL)
- **Open-Meteo API** för väderdata (gratis tier)
- **Maven** för build management

## Säkerhet

Write-operationer kräver API-key i header:
```
X-API-KEY: topsecret123
```

Basic rate limiting:
- Weather endpoints: 30 requests/minut
- Admin endpoints: 10 requests/minut

## API Endpoints

### User Endpoints

#### Favoriter
```bash
GET /favorites                 # Mina favoritplatser
PUT /favorites/{placeName}     # Markera som favorit
DELETE /favorites/{placeName}  # Ta bort favorit
```

#### Väderdata
```bash
GET /weather/{placeName}                    # Väder för favoritplats (cachad)
GET /weather/weatherAtLocation/{placeName}  # Sök väder för valfri plats
GET /weather/locationByName/{placeName}     # Hämta platsinfo
GET /forecast/{placeName}                   # 7-dagars prognos
```

#### Alerts
```bash
GET /alerts                    # Se alert-definitioner
```

### Admin Endpoints (kräver API-key)

#### Alert Management
```bash
POST /admin/alerts            # Skapa alert
GET /admin/alerts             # Lista alerts
PUT /admin/alerts/{id}        # Uppdatera alert
DELETE /admin/alerts/{id}     # Ta bort alert
```

#### System
```bash
POST /admin/weather/update    # Manuell uppdatering
GET /admin/stats              # Systemstatistik
```

## Exempel

### Lägg till favorit och hämta väder
```bash
# Markera Stockholm som favorit
curl -X PUT http://localhost:8080/favorites/Stockholm

# Hämta väder (första gången från API, sedan cache)
curl http://localhost:8080/weather/Stockholm
```

### Sök efter ny plats
```bash
# Sök väder för vilken plats som helst
curl http://localhost:8080/weather/weatherAtLocation/Malmö
```

### Skapa alert (admin)
```bash
curl -X POST http://localhost:8080/admin/alerts \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: <your-api-key>" \
  -d '{
    "name": "Kyla",
    "alertType": "temperature",
    "operator": "<",
    "thresholdValue": 0.0,
    "severity": "medium",
    "message": "Kallt väder"
  }'
```

## Funktioner

**Automatisk uppdatering:** Hämtar väder för favoritplatser var 30:e minut

**Cache:** Redis cache för att minska API-anrop till Open-Meteo

**Alert system:** Admin kan definiera väderalerts som triggas automatiskt

**Sökning:** Använder Open-Meteo geocoding för att hitta platser

## Installation

### Förutsättningar
- Java 17+
- PostgreSQL
- Redis

### Database setup
```sql
CREATE DATABASE weather_db;
CREATE USER weather_user WITH PASSWORD 'weather_pass';
GRANT ALL PRIVILEGES ON DATABASE weather_db TO weather_user;
```

### Köra applikationen
```bash
# Starta PostgreSQL och Redis först
./mvnw spring-boot:run

# Går på http://localhost:8080
```

## Frontend

Projektet inkluderar också en React TypeScript frontend i `weather-frontend/` mappen:

```bash
cd weather-frontend
npm install
npm start
# Går på http://localhost:3000
```

### Frontend funktioner
- Sök väder för valfri plats
- Lägg till/ta bort favoriter
- Visa 7-dagars prognoser för favoritplatser
- Responsiv design med Tailwind CSS

## Data Models

### Weather Response
```json
{
  "place": {
    "name": "Stockholm",
    "lat": 59.3293,
    "lon": 18.0686
  },
  "source": "open-meteo",
  "cached": false,
  "data": {
    "time": "2025-09-12T19:15",
    "temperature_2m": 15.4,
    "cloud_cover": 45,
    "wind_speed_10m": 19.1
  }
}
```

### Alert Definition
```json
{
  "id": 1,
  "name": "Kyla",
  "alertType": "temperature",
  "operator": "<",
  "thresholdValue": 0.0,
  "severity": "medium",
  "message": "Kallt väder",
  "active": true
}
```

## Reflektion

### Vad har vi lärt oss om webbtjänster, versionshantering och samarbete?

**Steven:**
"I've achieved a deeper understanding on how web services function and how crucial they are in enabling applications to communicate and exchange data across the internet. While I knew beforehand that there were different types of databases, this program has taught me that each have a different purpose and function, and can serve better or worse depending situation and context.

I have now further understanding of HTTPS, having previously only known of it during our Frontend course. Understanding how DDoS protection functions has also been a new eye-opener, having always known what a DDoS attack was but didn't know how to prevent it.

In regards to version control, nothing new has surprised me or come to mind, however I continue to hone my skills by making sure to properly document commits in a short-and-sweet format, as well as constantly using branches and keeping out of main, only merging into it.

Our co-operation as a team has been great, as everyone was given a task to do via our Gant schedule and Kanban board. Each of us set out to get it done, without fuss. We aided each other when requested, to the best of our ability and we can proudly deliver this program."

**Parmida:**
"Jag har mer förstått samarbetet och kommunikationen vid webbtjänster och vad som behövs först en webbtjänst ska fungera. Jag har nu fattat hela versionshantering och inser varför de är så bra att ha det. Samarbetet gick bra, hade bra kommunikation!"

**Gustav:**
"Detta projekt har givit mig en djupare förståelse för hur komplexa webbtjänster byggs och underhålls i praktiken. Jag har lärt mig att REST API-design handlar om mycket mer än bara endpoints - det kräver genomtänkt arkitektur för säkerhet, prestanda och skalbarhet. Implementeringen av caching-strategier med Redis har visat mig hur viktigt det är att optimera externa API-anrop för att skapa responsiva applikationer.

Versionshantering har blivit en naturlig del av utvecklingsprocessen genom att använda Git-branches för parallell utveckling. Jag har insett värdet av tydliga commit-meddelanden och hur de fungerar som dokumentation för teamet. Pull request-processen har lärt mig vikten av kodgranskning för att hålla hög kodkvalitet.

Samarbetet i teamet har fungerat utmärkt tack vare tydlig uppdelning av ansvarsområden - när var och en fokuserar på sina områden (services, controllers, frontend) medan vi ändå koordinerar regelbundet. Den tekniska kommunikationen kring API-design och databasstruktur har förbättrat min förmåga att diskutera tekniska lösningar med andra utvecklare."

### Tekniska lärdomar

Projektet har gett oss djupare förståelse för:

- **REST API-design**: Hur man strukturerar endpoints logiskt och använder rätt HTTP-metoder
- **Säkerhet**: Implementering av API-keys, rate limiting och DDoS-skydd
- **Caching-strategier**: Redis för prestanda och minska externa API-anrop
- **Databasdesign**: Relationer mellan entiteter och optimering av queries
- **Schemalagda tjänster**: Automatisk uppdatering av data i bakgrunden
- **Frontend-integration**: Hur backend och frontend kommunicerar via API:er
- **Felhantering**: Graceful degradation och användarvänliga felmeddelanden

### Samarbete och utvecklingsprocess

- **GitHub Projects**: Använding av issues, milestones och Kanban-board för planering
- **Branching-strategi**: Konsekvent användning av feature branches och pull requests
- **Commit-meddelanden**: Tydliga och beskrivande commit-meddelanden
- **Kodgranskning**: Gemensam granskning av kod innan merge till main
- **Dokumentation**: Vikten av bra dokumentation för både utvecklare och användare