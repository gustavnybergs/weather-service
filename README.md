# Weather API – Grupp 3

Ett Java Spring Boot-projekt där vi bygger en vädertjänst. Projektet utvecklas iterativt baserat på våra user stories och kravspecifikationer.

## Teknologier

- Java 17+
- Spring Boot 3
- Maven
- REST API
- JSON
- (Planerat) PostgreSQL för lagring
- (Planerat) API-integration med Open-Meteo

## Säkerhet

Skrivande endpoints kräver headern:
```
X-API-KEY: topsecret123
```

## API

### 1) Places (in-memory CRUD)

**Modell**
```json
{
  "name": "Stockholm",
  "lat": 59.3293,
  "lon": 18.0686
}
```

**Endpoints**
- `GET /places` → lista alla platser
- `POST /places` → lägg till plats (validerar namn, undviker dubbletter)
- `PUT /places/{name}` → uppdatera plats
- `DELETE /places/{name}` → ta bort plats

**Exempel**
```bash
curl -i -H "Content-Type: application/json" \
  -H "X-API-KEY: topsecret123" \
  -d '{"name":"Stockholm","lat":59.3293,"lon":18.0686}' \
  http://localhost:8080/places
```

### 2) Weather

**Endpoint**
- `GET /weather/{placeName}` → hämtar väderdata från Open-Meteo API och returnerar JSON

**Exempel**
```bash
curl -i http://localhost:8080/weather/Stockholm
```

**Svar**
```json
{
  "source": "open-meteo",
  "data": {
    "time": "2025-09-09T17:00",
    "interval": 900,
    "temperature_2m": 20.3,
    "cloud_cover": 23,
    "wind_speed_10m": 10.4
  },
  "place": {
    "name": "Stockholm",
    "lat": 59.3293,
    "lon": 18.0686
  }
}
```

## Körning

```bash
# Starta applikationen
./mvnw spring-boot:run

# Applikationen körs på http://localhost:8080
```

## User stories

- Som admin vill jag kunna lägga till/uppdatera/ta bort platser (namn, lat, lon), så att registret är korrekt.
- Som användare vill jag kunna söka efter en plats via namn, så att jag snabbt hittar rätt ort.
- Som användare vill jag hämta aktuellt väder för en vald plats, så att jag vet hur det är nu.
- Som säkerhetsansvarig vill jag att skrivande endpoints kräver API-nyckel/token, så att obehöriga inte kan ändra data.