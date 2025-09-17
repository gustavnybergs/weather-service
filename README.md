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
GET /weather/weatherAtLocation/{query}      # Sök väder för valfri plats
GET /weather/locationByName/{query}         # Hämta platsinfo
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

**Automatic uppdatering:** Hämtar väder för favoritplatser var 30:e minut

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


Reflektion: Vad har vi lärt oss?
Webbtjänster
Vi har lärt oss att bygga en robust webbtjänst som:

Hanterar externa API:er med felhantering och resilience
Implementerar caching för bättre prestanda och användarupplevelse
Följer REST-principer med semantiska endpoints och HTTP-statuskoder
Hanterar async/schemalagda processer för bakgrundsuppdateringar
Implementerar säkerhetslager (autentisering, rate limiting, validering)

Versionshantering
Genom GitHub har vi övat på:

Branch-strategier för parallell utveckling
Pull Request-workflows för kodgranskning
Issue tracking för buggar och feature requests
Project management för sprint-planering
Commit discipline med beskrivande meddelanden

Samarbete
Vi har utvecklat färdigheter inom:

Kodgranskning genom pull requests
Dokumentation för kunskapsöverföring
API-design för konsistens mellan utvecklare
Konfliktlösning vid merge-konflikter
Kommunikation kring tekniska beslut

Tekniska lärdomar

Spring Boot ecosystem: Förstått dependency injection, auto-configuration
Database design: Normalisering, relationer, index-optimering
Caching strategies: Redis för performance-förbättringar
External integrations: Hantering av third-party APIs
Security mindset: Implementerat flera säkerhetslager
Monitoring: Logging och error tracking för produktion

Utmaningar vi löste

Rate limiting - Implementerade Bucket4j för att skydda mot överbelastning
Cache invalidation - Balanserade prestanda mot data-fräschhet
Error resilience - Graceful degradation när externa tjänster är nere
Data consistency - Transaktionshantering vid komplexa operationer
Security layering - Flera säkerhetsnivåer utan att påverka UX

Projektet gav oss verklig erfarenhet av hela utvecklingscykeln från planering till deployment.