# MakeMyTrip Live Platform — Real External API Edition

This ZIP keeps the existing flight, hotel, live telemetry, reviews, recommendations, seat map, price freeze, OTP, cancellation and refund UI, and adds a real-provider layer for:

- Worldwide location search using OpenStreetMap Nominatim, with Amadeus/Aviationstack IATA and airport enrichment when credentials are configured.
- Live airport/flight status and airport boards through Aviationstack.
- Live flight shopping, hotel reference data and hotel offers through Amadeus.
- Live weather through Open-Meteo.
- Live FX rates through Frankfurter.
- Real Razorpay payment orders, checkout verification and refunds.
- Optional UPI QR/deep-link payment using a configured merchant VPA.
- Payment-aware bookings: bookings are `PENDING_PAYMENT` until the Razorpay signature is verified, then become `CONFIRMED`.
- Razorpay refunds are initiated automatically when a confirmed paid booking is cancelled.
- WebSocket live update channel remains enabled.

## Important: what cannot be bundled as a fake “real” service

A ZIP cannot contain your private airline/GDS/payment credentials or a merchant account. The project therefore never invents payment success or airline inventory. For production-real operation, put your own provider credentials in `backend/.env`.

### Required for real booking payments

```env
RAZORPAY_KEY_ID=...
RAZORPAY_KEY_SECRET=...
PAYMENT_REQUIRED=true
```

Without these, the application deliberately refuses to create a payable booking instead of pretending that money was received.

### Recommended live travel credentials

```env
AVIATION_API_KEY=...
AMADEUS_CLIENT_ID=...
AMADEUS_CLIENT_SECRET=...
AMADEUS_BASE_URL=https://api.amadeus.com
```

### Optional UPI QR

```env
PAYMENT_UPI_VPA=yourmerchant@upi
PAYMENT_MERCHANT_NAME=Your Merchant Name
```

The QR is a real UPI intent. Razorpay remains the authoritative payment confirmation/refund path because a plain UPI QR cannot safely tell the booking server that money was received.

## Run on Windows

### Backend

Requirements: Java 17+ and Maven 3.9+.

```bat
cd backend
mvn spring-boot:run
```

or run `start-backend.bat` from `mmt_work`.

Backend: `http://localhost:8080`

Health: `http://localhost:8080/api/health`

Provider status: `http://localhost:8080/api/live/providers`

### Frontend

Requirements: Node.js 18+.

```bat
cd frontend
npm install
npm run dev
```

or run `start-frontend.bat`.

Frontend: normally `http://localhost:5173`

**Do not copy `node_modules` between Windows/Linux/macOS.** This final package intentionally excludes `node_modules`; run `npm install` on the target machine so Vite/Rollup/esbuild use the correct native binaries.

## Global locations / airports

The location box now says “Search anywhere in the world”. Search can combine:

1. Local database records already present in the project.
2. Amadeus airport/city IATA reference data when Amadeus is configured.
3. Aviationstack airport data when Aviationstack is configured.
4. OpenStreetMap Nominatim global geocoding as a live fallback.

This means the user does not need to manually enter a finite list of cities. Enter a city, country, airport name, IATA code or landmark and the backend performs live lookup.

Endpoints:

- `GET /api/locations/search?q=...`
- `GET /api/locations/global?q=...`
- `GET /api/locations/airports?q=...`

## Real payment flow

1. User selects a flight seat or hotel room.
2. Backend creates a `PENDING_PAYMENT` booking and reserves inventory.
3. Frontend opens Razorpay Checkout.
4. Razorpay returns `order_id`, `payment_id` and signature.
5. Backend verifies the HMAC signature using the private Razorpay secret.
6. Only after verification does the booking become `CONFIRMED` and `paymentStatus=PAID`.
7. Cancellation calculates the current refund amount and, for a captured Razorpay payment, calls the Razorpay refund API automatically.
8. The booking stores refund state and expected refund date.

API endpoints:

- `GET /api/payments/providers`
- `POST /api/payments/razorpay/order`
- `POST /api/payments/razorpay/verify`
- `POST /api/payments/razorpay/refund`
- `GET /api/payments/booking/{bookingId}`
- `GET /api/payments/upi-qr?amount=...&reference=...`

## Security cleanup

The supplied ZIP previously contained credential-looking values in `.env`. The delivered package replaces them with placeholders. **Never put real SMTP, Aviationstack, Amadeus or Razorpay secrets in Git.**

## Existing features retained

The existing project functionality is intentionally retained: authentication/OTP, flight search, hotel search, seat map, price freeze, dynamic pricing, flight tracking, WebSocket updates, reviews/replies/moderation, recommendations, weather, FX, cancellation, refund tracking, H2 default database and optional MySQL configuration.

## Reality check

“Real-time” depends on the provider account, endpoint availability, quotas, network and the freshness of the provider's own feed. The application does not label local simulation as live external inventory. Where an external provider is unavailable, the corresponding provider status is exposed instead of silently claiming that the data is airline-authoritative.

## Worldwide Location Search Upgrade

Flight origin/destination and hotel destination are no longer limited to the seeded dropdown lists. The UI now uses the backend `/api/locations/global` endpoint for worldwide autocomplete, combining configured Amadeus reference data, Aviationstack airports, and OpenStreetMap/Nominatim geographic search. Selecting a result carries its IATA code when available, so live Amadeus flight/hotel requests can use the selected location instead of a hard-coded city map.

For truly live flight offers and hotel inventory, Amadeus credentials must be configured. Without them, the worldwide location search can still discover geographic locations/airports, while the existing local seeded booking data remains available.

## Worldwide search upgrade

The search UI is no longer restricted to the seeded cities.

- Flight locations accept cities, airports, IATA codes, towns and districts worldwide.
- If a selected location has no IATA code, the backend resolves the nearest IATA airport from OpenStreetMap/Overpass where possible.
- When `AVIATION_API_KEY` is configured, route search can show live Aviationstack flight records for the selected airports/date.
- Hotel search accepts a district/town/city/country name and discovers mapped hotels/hostels/motels around the resolved location using OpenStreetMap/Overpass.

### Important distinction

OpenStreetMap/Overpass is **location discovery**, not a hotel booking inventory or fare engine. The discovered worldwide properties are therefore marked `GLOBAL DISCOVERY` and are not falsely presented as live bookable rooms/prices.

For true worldwide bookable hotel rooms, live rates, availability and checkout, the project still needs a commercial hotel inventory/booking provider account. Likewise, live flight shopping/fare + ticket issuance requires a flight-shopping/ticketing provider; Aviationstack provides operational flight data/status, not a universal ticketing inventory.
