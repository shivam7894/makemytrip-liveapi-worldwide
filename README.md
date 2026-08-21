# MakeMyTrip Live API — Complete Project

See **README-REAL-WORKING.md** for the full production-oriented setup.

The project is a Spring Boot + React travel platform retaining the original features and adding worldwide live location/airport lookup plus real Razorpay payment verification/refunds and optional UPI QR.

## Quick start

1. Put your real provider credentials in `backend/.env` (see `backend/.env.example`).
2. Start backend: `cd backend && mvn spring-boot:run`.
3. Start frontend: `cd frontend && npm install && npm run dev`.
4. Open the Vite URL shown by the terminal.

Real payment is intentionally enabled by default. Configure Razorpay before attempting a booking.
