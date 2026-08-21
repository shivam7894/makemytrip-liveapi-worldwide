# MakeMyTrip Live API - Fixed Build

Fixed compilation mismatch between DataSeeder and the Hotel / HotelRoom JPA models.

Changes:
- Hotel now contains `address`, `reviewCount`, and `amenities` fields used by DataSeeder.
- HotelRoom now contains `refundable` field used by DataSeeder.

Run backend from `mmt/backend`:
```
mvn clean install
mvn spring-boot:run
```

Then run frontend from `mmt/frontend`:
```
npm install
npm run dev
```

## New production-style features in this build
- OTP authentication: signup OTP, login OTP, and forgot-password/reset OTP.
- Real email OTP support through SMTP. Configure `MAIL_SMTP_*` in `.env`/environment variables. Without SMTP, local development exposes a one-time `devOtp` so the complete flow remains testable.
- H2 file database stores users, OTPs, bookings, hotels, rooms, flights, prices and location records.
- India location database sync: states + districts are downloaded from a current public dataset sourced from the Government of India portal and cached into H2; India airports are synced from a maintained worldwide airport JSON dataset and filtered to India.
- Location search UI supports state, district, city and airport/IATA searches.
- Amadeus live provider support remains available for live flight offers and hotel inventory when credentials are configured. `/api/live/amadeus/hotel-list?cityCode=DEL` can retrieve live hotel inventory.

### Real email OTP setup (Gmail)
1. Enable 2-Step Verification on the Gmail account.
2. Create a Google App Password.
3. Put the App Password in `MAIL_SMTP_PASSWORD` and the Gmail address in `MAIL_SMTP_USERNAME`.
4. Restart the backend.

Do not commit SMTP passwords, Amadeus secrets or other API keys to GitHub.


## Internship Tasks 1–6 implemented

### Task 1 — Live Flight Status & Tracking
- 15-second mock real-time flight engine updates status: ON TIME, BOARDING, DELAYED and DEPARTED.
- Delay reason, revised departure and dynamic ETA are persisted.
- WebSocket `/ws/updates` broadcasts live flight events to connected browsers.
- Multiple flights can be tracked per user.
- Browser notification permission can be enabled from the live status bar.

### Task 2 — Dynamic Pricing & Price Freeze
- Demand-based pricing engine runs every 15 seconds for flights and hotels.
- Demand, weekend/seasonal periods and holiday-style peak dates affect the multiplier.
- Every live price change is stored in `price_history` and displayed as a price graph.
- Flight price freeze stores a locked price with an expiry and is honored during booking.

### Task 3 — Cancellation & Refunds
- Dashboard cancellation flow requires a predefined reason.
- Refund policy is calculated automatically: 50% within 24 hours, 20% afterward.
- Refund tracker progresses automatically from PENDING → PROCESSED → COMPLETED.
- Expected refund date is shown to the user.

### Task 4 — Seat & Room Selection
- Dynamic 6-column flight seat map with occupied-seat detection.
- Premium front rows are clearly marked and add a transparent ₹799 surcharge.
- Selected seats are persisted as the user's preferred seat.
- Hotel room-type grid supports availability, room images, room preference saving and live room inventory changes.

### Task 5 — Reviews & Ratings
- 1–5 star reviews for hotels and flights.
- Review text, photo URL and direct image upload (stored as a data URL for the demo database).
- Helpful votes, replies and inappropriate-content flagging.
- Moderator endpoint can remove flagged reviews.
- Sorting: newest, highest rated and most helpful.

### Task 6 — Personalized Recommendations
- Recommendations use saved destination/preferences and feedback signals.
- Each card exposes a “Why this recommendation?” explanation.
- Helpful / Not for me feedback is stored and used by the recommendation scorer.

## Important note about “real time”
The internship requirements are implemented with a realistic local real-time engine and WebSocket stream, so the project works without paid aviation credentials. Optional Amadeus/Aviationstack integrations remain available for external live inventory/status data. Browser notifications require the user to grant notification permission.

## Run
Backend:
```bat
cd backend
mvn clean spring-boot:run
```

Frontend:
```bat
cd frontend
npm install
npm run dev
```

Default URLs:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- H2 console: http://localhost:8080/h2-console
- WebSocket: ws://localhost:8080/ws/updates

## Real external live mode (Tasks 1-6)

The project now includes a dedicated **External Live Data Center** in the frontend and real provider endpoints in the backend.

### Required for real flight/hotel inventory
Create `backend/.env` and set:

```env
AVIATION_API_KEY=your_aviationstack_key
AMADEUS_CLIENT_ID=your_amadeus_client_id
AMADEUS_CLIENT_SECRET=your_amadeus_client_secret
AMADEUS_BASE_URL=https://api.amadeus.com
```

- **Aviationstack** powers real flight status plus airport arrival/departure boards.
- **Amadeus** powers real flight shopping, airport/city lookup, hotel inventory lookup and current hotel offers.
- **Open-Meteo** supplies live weather without a key.
- **Frankfurter** supplies live FX rates without a key.
- WebSocket pushes the application's live flight/hotel/price events to connected clients.
- When an Aviationstack key is configured, the local telemetry engine no longer overwrites the provider's flight status.

### Important
External providers require valid accounts/API credentials and their quotas/availability. The project can still start without those credentials, but that is **development mode**, not the real external-provider mode.
