import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Plane, Hotel as HotelIcon, LogIn, UserPlus, Bell, Search, Clock3, Lock,
  RefreshCw, Star, Heart, ShieldCheck, ChevronRight, MapPin, Armchair,
  CalendarDays, IndianRupee, ThumbsUp, Flag, MessageCircle, Gift,
  WalletCards, X, CheckCircle2, SlidersHorizontal, Home, LogOut,
  ArrowRightLeft, Sparkles, Building2, UserCircle, Luggage, Phone,
  Compass, Ticket, Tag, Check, AlertCircle, TrendingDown, Info, Send
} from 'lucide-react';

const API = import.meta.env.VITE_API_URL || `${window.location.protocol}//${window.location.hostname}:8080/api`;

async function api(path, opts = {}) {
  const r = await fetch(API + path, {
    headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
    ...opts
  });
  if (!r.ok) {
    const t = await r.text();
    try {
      const errObj = JSON.parse(t);
      throw new Error(errObj.message || errObj.error || t || 'Request failed');
    } catch (e) {
      if (e instanceof Error && e.message !== t) throw e;
      throw new Error(t || 'Request failed');
    }
  }
  return r.status === 204 ? null : r.json();
}

const money = (n) => `₹${Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;


function LocationAutocomplete({ label, value, placeholder, onSelect }) {
  const [query, setQuery] = useState(value || '');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const timer = useRef(null);

  useEffect(() => { setQuery(value || ''); }, [value]);

  const search = (q) => {
    setQuery(q);
    clearTimeout(timer.current);
    if (!q || q.trim().length < 2) { setResults([]); return; }
    timer.current = setTimeout(async () => {
      setLoading(true);
      try {
        const data = await api(`/locations/global?q=${encodeURIComponent(q.trim())}`);
        setResults(Array.isArray(data) ? data : []);
      } catch { setResults([]); }
      finally { setLoading(false); }
    }, 250);
  };

  const choose = (item) => {
    setQuery(item.name || item.city || '');
    setResults([]);
    onSelect(item);
  };

  return (
    <div className="relative border border-slate-200 rounded-xl p-3 hover:border-blue-500 transition-colors">
      <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">{label}</label>
      <div className="flex items-center gap-2">
        <MapPin size={16} className="text-blue-600 shrink-0" />
        <input
          value={query}
          onChange={(e) => search(e.target.value)}
          onFocus={() => query.length >= 2 && search(query)}
          placeholder={placeholder}
          className="w-full font-bold text-slate-800 bg-transparent focus:outline-none"
          autoComplete="off"
        />
        {loading && <RefreshCw size={14} className="animate-spin text-slate-400 shrink-0" />}
      </div>
      {results.length > 0 && (
        <div className="absolute z-50 left-0 right-0 top-full mt-2 bg-white border border-slate-200 rounded-2xl shadow-2xl max-h-80 overflow-auto">
          {results.slice(0, 12).map((x, i) => (
            <button key={`${x.type || 'LOC'}-${x.code || ''}-${x.name || x.city || i}`} onClick={() => choose(x)} className="w-full text-left p-3 hover:bg-blue-50 border-b border-slate-100 last:border-0">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <div className="font-bold text-sm text-slate-900 truncate">{x.name || x.city || 'Location'}</div>
                  <div className="text-[11px] text-slate-500 mt-1">
                    {x.type || 'LOCATION'}{x.code ? ` • ${x.code}` : ''}{x.city && x.name !== x.city ? ` • ${x.city}` : ''}
                  </div>
                </div>
                <div className="text-[10px] text-slate-400 text-right shrink-0">{x.state || x.country || ''}</div>
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function PriceGraph({ items = [] }) {
  if (!items.length) {
    return <div className="p-4 text-xs text-gray-500 italic bg-gray-50 rounded-lg">Real-time price trend tracker is calibrating...</div>;
  }
  const vals = items.map(x => x.price);
  const min = Math.min(...vals);
  const max = Math.max(...vals);
  const w = 480, h = 130;
  const range = max === min ? 1 : max - min;
  const pts = vals.map((v, i) => `${(i / Math.max(1, vals.length - 1)) * (w - 30) + 15},${h - 15 - ((v - min) / range) * (h - 35)}`).join(' ');

  return (
    <div className="bg-white p-3 rounded-xl border border-gray-100 shadow-inner">
      <div className="flex justify-between items-center text-xs font-semibold text-gray-500 mb-2">
        <span>Low: {money(min)}</span>
        <span className="text-blue-600 font-bold">Live Price Chart</span>
        <span>High: {money(max)}</span>
      </div>
      <svg viewBox={`0 0 ${w} ${h}`} className="w-full h-24 overflow-visible">
        <defs>
          <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stopColor="#2563eb" />
            <stop offset="100%" stopColor="#06b6d4" />
          </linearGradient>
        </defs>
        <polyline fill="none" stroke="url(#lineGrad)" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" points={pts} />
        {items.slice(-6).map((x, i) => {
          const ptX = (i / Math.max(1, Math.min(6, items.length) - 1)) * (w - 30) + 15;
          const ptY = h - 15 - ((x.price - min) / range) * (h - 35);
          return <circle key={i} cx={ptX} cy={ptY} r="4" fill="#f97316" className="drop-shadow-sm" />;
        })}
      </svg>
    </div>
  );
}

export default function App() {
  const [activeTab, setActiveTab] = useState('flights'); // flights, hotels, bookings, profile
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('mmtUser');
      return saved ? JSON.parse(saved) : null;
    } catch { return null; }
  });

  // Auth modal
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authMode, setAuthMode] = useState('login'); // login, signup, forgot
  const [authStep, setAuthStep] = useState('form');
  const [authOtp, setAuthOtp] = useState('');
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '', newPassword: '', preferences: 'beach,luxury,mountains', favoriteDestination: 'Goa' });
  const [authErr, setAuthErr] = useState('');
  const [devOtp, setDevOtp] = useState('');
  const [paymentModal, setPaymentModal] = useState(null);
  const [paymentProviders, setPaymentProviders] = useState({razorpay:false,upiQr:false});
  const [upiQr, setUpiQr] = useState(null);

  const [locationQuery, setLocationQuery] = useState('');
  const [locationResults, setLocationResults] = useState([]);
  const searchLocations = async (q) => { setLocationQuery(q); if (!q || q.length < 2) { setLocationResults([]); return; } try { setLocationResults(await api(`/locations/search?q=${encodeURIComponent(q)}`)); } catch { setLocationResults([]); } };

  // Flight search states
  const [flightSource, setFlightSource] = useState('Delhi');
  const [flightSourceCode, setFlightSourceCode] = useState('DEL');
  const [flightDest, setFlightDest] = useState('Mumbai');
  const [flightDestCode, setFlightDestCode] = useState('BOM');
  const [flightDate, setFlightDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [flightAirlines, setFlightAirlines] = useState([]);
  const [flightList, setFlightList] = useState([]);
  const [flightLoading, setFlightLoading] = useState(false);
  const [selectedAirlineFilter, setSelectedAirlineFilter] = useState('ALL');
  const [maxFlightPrice, setMaxFlightPrice] = useState(60000);

  // Hotel search states
  const [hotelCity, setHotelCity] = useState('Goa');
  const [hotelCityCode, setHotelCityCode] = useState('GOI');
  const [hotelList, setHotelList] = useState([]);
  const [hotelLoading, setHotelLoading] = useState(false);
  const [selectedHotel, setSelectedHotel] = useState(null);
  const [hotelRooms, setHotelRooms] = useState([]);
  const [hotelHistory, setHotelHistory] = useState([]);
  const [minRatingFilter, setMinRatingFilter] = useState(0);

  // Flight Details & Live Tracking Modal
  const [activeFlightModal, setActiveFlightModal] = useState(null);
  const [flightHistory, setFlightHistory] = useState([]);
  const [flightUpdates, setFlightUpdates] = useState([]);
  const [flightReviews, setFlightReviews] = useState([]);

  // Bookings & Freezes
  const [userBookings, setUserBookings] = useState([]);
  const [userFreezes, setUserFreezes] = useState([]);
  const [bookingLoading, setBookingLoading] = useState(false);

  // AI recommendations
  const [recommendations, setRecommendations] = useState(null);
  const [liveWeather, setLiveWeather] = useState(null);
  const [liveFx, setLiveFx] = useState(null);
  const [providerStatus, setProviderStatus] = useState(null);
  const [liveConnected, setLiveConnected] = useState(false);
  const [liveFlightOffers, setLiveFlightOffers] = useState([]);
  const [liveHotelOffers, setLiveHotelOffers] = useState([]);
  const [liveAirportBoard, setLiveAirportBoard] = useState([]);
  const [liveProviderLoading, setLiveProviderLoading] = useState(false);

  // Reviews modal
  const [reviewHotel, setReviewHotel] = useState(null);
  const [hotelReviews, setHotelReviews] = useState([]);
  const [newReviewText, setNewReviewText] = useState('');
  const [newReviewRating, setNewReviewRating] = useState(5);
  const [replyInput, setReplyInput] = useState({});
  const [reviewSort, setReviewSort] = useState('newest');
  const [reviewPhoto, setReviewPhoto] = useState('');
  const [cancelModal, setCancelModal] = useState(null);
  const [cancelReason, setCancelReason] = useState('Change of plans');
  const [cancelReasons, setCancelReasons] = useState([]);
  const [seatFlight, setSeatFlight] = useState(null);
  const [seatMap, setSeatMap] = useState(null);
  const [trackedFlights, setTrackedFlights] = useState([]);
  const [selectedSeat, setSelectedSeat] = useState('');
  const [browserNotifications, setBrowserNotifications] = useState(false);

  // Booking action feedback toast
  const [toast, setToast] = useState('');
  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(''), 4500);
  };

  const loadLiveData = async () => {
    try {
      const providers = await api('/live/providers');
      setProviderStatus(providers);
      const [weather, fx] = await Promise.all([
        api('/live/weather?latitude=15.49&longitude=73.83').catch(() => null),
        api('/live/fx?base=INR&symbols=USD,EUR,GBP,AED').catch(() => null)
      ]);
      setLiveWeather(weather?.current || null);
      setLiveFx(fx?.rates || null);
    } catch (e) { console.warn('Live data unavailable', e); }
  };

  useEffect(() => {
    loadLiveData();
    const id = setInterval(loadLiveData, 60000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    let ws;
    try {
      const wsBase = import.meta.env.VITE_WS_URL || ((location.protocol === 'https:' ? 'wss://' : 'ws://') + location.hostname + ':8080');
      ws = new WebSocket(wsBase + '/ws/updates');
      ws.onopen = () => setLiveConnected(true);
      ws.onclose = () => setLiveConnected(false);
      ws.onmessage = (e) => {
        try {
          const events = JSON.parse(e.data);
          const list = Array.isArray(events) ? events : [events];
          const updated = list.filter(x => x.type === 'FLIGHT_UPDATE').map(x => x.flight).filter(Boolean);
          if (updated.length) {
            setFlightList(prev => prev.map(f => updated.find(u => u.id === f.id) || f));
            updated.forEach(f => {
              if (['DELAYED','BOARDING','DEPARTED'].includes(f.status)) {
                showToast(`✈️ ${f.flightNumber}: ${f.status}${f.delayReason ? ' — '+f.delayReason : ''}`);
                if ('Notification' in window && Notification.permission === 'granted') new Notification(`Flight ${f.flightNumber} update`, {body: `${f.status}${f.delayReason ? ' — '+f.delayReason : ''}. ETA ${f.estimatedArrivalTime ? new Date(f.estimatedArrivalTime).toLocaleTimeString() : 'updating'}`});
              }
            });
          }
        } catch {}
      };
    } catch {}
    return () => { try { ws?.close(); } catch {} };
  }, []);

  // Initial loads
  useEffect(() => {
    loadFlights();
    loadHotels();
    if (user?.id) {
      loadUserBookings();
      loadRecommendations();
    }
  }, [user]);

  // Real-time polling / WebSocket fallback simulation for dynamic prices & updates
  useEffect(() => {
    const timer = setInterval(() => {
      if (activeTab === 'flights') {
        loadFlights(true);
      }
    }, 15000);
    return () => clearInterval(timer);
  }, [flightSource, flightDest, activeTab]);

  const finishAuth = (res) => { setUser(res); localStorage.setItem('mmtUser', JSON.stringify(res)); setShowAuthModal(false); setAuthStep('form'); setAuthOtp(''); setDevOtp(''); showToast(`Welcome, ${res.name}! Authentication successful.`); };
  const handleAuth = async (e) => {
    e.preventDefault(); setAuthErr('');
    try {
      if (authStep === 'otp') {
        const endpoint = authMode === 'signup' ? '/auth/signup/verify' : authMode === 'login' ? '/auth/login/verify' : '/auth/forgot/reset';
        const payload = authMode === 'forgot' ? { email: authForm.email, otp: authOtp, newPassword: authForm.newPassword } : { ...authForm, otp: authOtp };
        const res = await api(endpoint, { method: 'POST', body: JSON.stringify(payload) });
        if (authMode === 'forgot') { setAuthMode('login'); setAuthStep('form'); setAuthForm({ ...authForm, password: '', newPassword: '' }); showToast('Password reset successfully. Please log in.'); } else finishAuth(res);
        return;
      }
      const endpoint = authMode === 'signup' ? '/auth/signup/request-otp' : authMode === 'login' ? '/auth/login/request-otp' : '/auth/forgot/request-otp';
      const payload = authMode === 'signup' ? authForm : { email: authForm.email, password: authForm.password };
      const res = await api(endpoint, { method: 'POST', body: JSON.stringify(payload) });
      setDevOtp(res.devOtp || ''); setAuthStep('otp');
      if (res.devOtp) showToast(`Development OTP: ${res.devOtp}`); else showToast('OTP sent to your email.');
    } catch (err) { setAuthErr(err.message || 'Authentication failed'); }
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('mmtUser');
    showToast('Logged out safely.');
  };

  useEffect(() => {
    api('/payments/providers').then(setPaymentProviders).catch(() => {});
  }, []);

  const loadRazorpayScript = () => new Promise((resolve, reject) => {
    if (window.Razorpay) return resolve(true);
    const existing = document.querySelector('script[data-razorpay]');
    if (existing) { existing.addEventListener('load', () => resolve(true), {once:true}); existing.addEventListener('error', reject, {once:true}); return; }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true; script.dataset.razorpay = 'true';
    script.onload = () => resolve(true); script.onerror = () => reject(new Error('Razorpay Checkout could not be loaded'));
    document.body.appendChild(script);
  });

  const startPayment = async (booking) => {
    try {
      const order = await api('/payments/razorpay/order', {method:'POST', body:JSON.stringify({bookingId:booking.id})});
      setPaymentModal({booking, order});
      setUpiQr(null);
      await loadRazorpayScript();
      const rzp = new window.Razorpay({
        key: order.keyId, amount: order.amount, currency: order.currency, name: order.merchantName || 'MakeMyTrip',
        description: `Booking #${booking.id}`, order_id: order.orderId, prefill: {name:user?.name || '', email:user?.email || ''},
        theme: {color:'#2563eb'},
        handler: async (response) => {
          try {
            await api('/payments/razorpay/verify', {method:'POST', body:JSON.stringify(response)});
            setPaymentModal(null); setUpiQr(null);
            showToast(`Payment successful. Booking #${booking.id} is confirmed.`);
            loadUserBookings();
          } catch(e) { alert('Payment verification failed: '+e.message); }
        },
        modal: {ondismiss: () => showToast('Payment window closed. Your booking remains pending until payment is completed.')}
      });
      rzp.open();
    } catch(e) {
      alert('Payment could not be started: '+e.message);
    }
  };

  const loadUpiQr = async (booking) => {
    try {
      const q = await api(`/payments/upi-qr?amount=${encodeURIComponent(booking.totalAmount)}&reference=${encodeURIComponent(booking.id)}`);
      setUpiQr(q);
    } catch(e) { alert('UPI QR unavailable: '+e.message); }
  };

  const loadFlights = async (silent = false) => {
    if (!silent) setFlightLoading(true);
    try {
      // Prefer live global route data when IATA codes are available.
      if (flightSourceCode && flightDestCode) {
        try {
          const live = await api(`/live/aviation/route?from=${encodeURIComponent(flightSourceCode)}&to=${encodeURIComponent(flightDestCode)}&date=${encodeURIComponent(flightDate)}&limit=100`);
          const rows = (live?.data || []).map((x, i) => ({
            id: `AVI-${x.flight?.iata || x.flight?.number || i}-${x.flight_date || flightDate}`, external:true, bookable:false,
            airline: x.airline?.name || x.airline?.iata || 'Airline', flightNumber: x.flight?.iata || x.flight?.number || 'N/A',
            sourceCity: x.departure?.airport || flightSource, destinationCity: x.arrival?.airport || flightDest,
            departureTime: x.departure?.scheduled || x.departure?.estimated || null, arrivalTime: x.arrival?.scheduled || x.arrival?.estimated || null,
            price: null, basePrice:null, availableSeats:null, status:(x.flight_status || 'scheduled').toUpperCase(), delayMinutes:x.departure?.delay || 0,
            delayReason:'Live Aviationstack status', estimatedDepartureTime:x.departure?.estimated || x.departure?.scheduled || null, estimatedArrivalTime:x.arrival?.estimated || x.arrival?.scheduled || null, lastUpdated:new Date().toISOString()
          }));
          if (rows.length) { setFlightList(rows); setFlightAirlines([...new Set(rows.map(f=>f.airline))]); return; }
        } catch (e) { console.warn('Live route search unavailable:', e.message); }
      }
      const data = await api(`/flights/search?from=${encodeURIComponent(flightSource)}&to=${encodeURIComponent(flightDest)}`);
      setFlightList(data || []);
      setFlightAirlines([...new Set((data || []).map(f => f.airline))]);
    } catch {
      try { const all = await api('/flights'); setFlightList(all || []); } catch (e) { console.error(e); }
    } finally { if (!silent) setFlightLoading(false); }
  };

  const loadHotels = async () => {
    setHotelLoading(true);
    try {
      // Global discovery: works for districts, towns, cities and countries even without Amadeus.
      const global = await api(`/hotels/global?location=${encodeURIComponent(hotelCity)}`);
      if (Array.isArray(global) && global.length) { setHotelList(global); return; }
      const data = await api(`/hotels/search?city=${encodeURIComponent(hotelCity)}`);
      setHotelList(data || []);
    } catch {
      try { const all = await api('/hotels'); setHotelList(all || []); } catch (e) { console.error(e); }
    } finally { setHotelLoading(false); }
  };

  const loadRealProviderData = async () => {
    if (!providerStatus?.amadeus) { showToast('Add AMADEUS_CLIENT_ID and AMADEUS_CLIENT_SECRET in backend/.env for live flight/hotel inventory.'); return; }
    setLiveProviderLoading(true);
    try {
      const airports = flightSourceCode ? {data:[{iataCode:flightSourceCode,subType:'CITY'}]} : await api(`/live/amadeus/locations?keyword=${encodeURIComponent(flightSource)}`);
      const fromCode = airports?.data?.find(x => x.subType === 'CITY' || x.subType === 'AIRPORT')?.iataCode;
      const destAirports = flightDestCode ? {data:[{iataCode:flightDestCode,subType:'CITY'}]} : await api(`/live/amadeus/locations?keyword=${encodeURIComponent(flightDest)}`);
      const toCode = destAirports?.data?.find(x => x.subType === 'CITY' || x.subType === 'AIRPORT')?.iataCode;
      if (fromCode && toCode) {
        const offers = await api(`/live/amadeus/flights?from=${fromCode}&to=${toCode}&date=${flightDate}&adults=1`);
        setLiveFlightOffers(offers?.data || []);
      }
      // Goa is the default hotel city in the seeded UI; Amadeus uses IATA city codes.
      const cityCode = hotelCityCode || (await api(`/live/amadeus/locations?keyword=${encodeURIComponent(hotelCity)}`).then(r => r?.data?.find(x => x.subType === 'CITY' || x.subType === 'AIRPORT')?.iataCode).catch(() => null));
      if (!cityCode) throw new Error('Select a city/airport from the worldwide location suggestions for live hotel inventory.');
      const hotels = await api(`/live/amadeus/hotel-list?cityCode=${cityCode}`);
      const ids = (hotels?.data || []).slice(0, 8).map(h => h.hotelId).filter(Boolean);
      if (ids.length) {
        const inDate = new Date(); inDate.setDate(inDate.getDate()+1);
        const outDate = new Date(inDate); outDate.setDate(outDate.getDate()+1);
        const fmt = d => d.toISOString().slice(0,10);
        const offers = await api(`/live/amadeus/hotels?hotelIds=${ids.join(',')}&checkIn=${fmt(inDate)}&checkOut=${fmt(outDate)}&adults=2`);
        setLiveHotelOffers(offers?.data || []);
      }
      if (providerStatus?.aviationstack) {
        const board = await api(`/live/aviation/airport-board?airportIata=${(fromCode || 'DEL')}&type=departure&limit=20`);
        setLiveAirportBoard(board?.data || []);
      }
      showToast('Live provider data refreshed from external APIs.');
    } catch (e) {
      showToast(e.message || 'Live provider request failed');
    } finally { setLiveProviderLoading(false); }
  };

  const loadUserBookings = async () => {
    if (!user?.id) return;
    setBookingLoading(true);
    try {
      const [b, f, t] = await Promise.all([
        api(`/bookings?userId=${user.id}`).catch(() => []),
        api(`/flights/freeze/user/${user.id}`).catch(() => []),
        api(`/flights/tracked?userId=${user.id}`).catch(() => [])
      ]);
      setUserBookings(b || []); setUserFreezes(f || []); setTrackedFlights(t || []);
    } catch (e) {
      console.error(e);
    } finally {
      setBookingLoading(false);
    }
  };

  const loadRecommendations = async () => {
    if (!user?.id) return;
    try {
      const recs = await api(`/recommendations/user/${user.id}`);
      setRecommendations(Array.isArray(recs) ? recs : (recs?.recommendations || []));
    } catch (e) {
      console.error(e);
    }
  };

  const openFlightDetails = async (flight) => {
    setActiveFlightModal(flight);
    try {
      const [hist, updates, revs] = await Promise.all([
        api(`/flights/${flight.id}/history`).catch(() => []),
        api(`/flights/${flight.id}/updates`).catch(() => []),
        api(`/reviews?entityType=FLIGHT&entityId=${flight.id}&sort=helpful`).catch(() => [])
      ]);
      setFlightHistory(hist || []); setFlightUpdates(updates || []); setFlightReviews(revs || []);
    } catch (e) {
      console.error(e);
    }
  };

  const openSeatMap = async (flight) => {
    if (!user) { setAuthMode('login'); setShowAuthModal(true); return; }
    try {
      const map = await api(`/seats/${flight.id}`);
      setSeatFlight(flight); setSeatMap(map); setSelectedSeat(user.preferredSeat || '');
    } catch (e) { alert('Seat map unavailable: '+e.message); }
  };
  const bookSelectedSeat = async () => {
    if (!seatFlight || !selectedSeat) return showToast('Please select a seat.');
    try {
      const res = await api('/bookings', {method:'POST', body:JSON.stringify({userId:user.id,bookingType:'FLIGHT',itemId:seatFlight.id,seatNumber:selectedSeat,passengerName:user.name,passengerEmail:user.email,totalAmount:seatFlight.price})});
      await api(`/users/${user.id}/preferences`, {method:'PUT',body:JSON.stringify({preferredSeat:selectedSeat})}).catch(()=>{});
      setSeatFlight(null); setSeatMap(null); setSelectedSeat('');
      if (res.status === 'PENDING_PAYMENT') {
        showToast(`Booking #${res.id} created. Complete payment to confirm it.`);
        await startPayment(res);
      } else {
        showToast(`💺 Seat ${selectedSeat} booked successfully. Ref #${res.id}`);
      }
      loadUserBookings();
    } catch(e){ alert('Seat booking failed: '+e.message); }
  };
  const bookFlightTicket = async (flight) => {
    if (!user) { setAuthMode('login'); setShowAuthModal(true); return; }
    await openSeatMap(flight);
  };

  const toggleTrackFlight = async (flight) => {
    if (!user) { setAuthMode('login'); setShowAuthModal(true); return; }
    try { await api(`/flights/${flight.id}/track?userId=${user.id}`, {method:'POST'}); showToast(`📍 ${flight.flightNumber} is now tracked live.`); }
    catch(e){ alert(e.message); }
  };
  const requestBrowserNotifications = async () => {
    if (!('Notification' in window)) return showToast('Browser notifications are not supported.');
    const p = await Notification.requestPermission(); setBrowserNotifications(p === 'granted');
    showToast(p === 'granted' ? '🔔 Live flight notifications enabled.' : 'Notifications were not allowed.');
  };
  const saveRoomPreference = async (room) => {
    if (!user) return;
    await api(`/users/${user.id}/preferences`, {method:'PUT',body:JSON.stringify({preferredRoomType:room.roomType})}).catch(()=>{});
    showToast(`⭐ ${room.roomType} saved as your room preference.`);
  };
  const freezeFlightPrice = async (flight) => {
    if (!user) {
      setAuthMode('login');
      setShowAuthModal(true);
      return;
    }
    try {
      await api('/flights/freeze', {
        method: 'POST',
        body: JSON.stringify({ userId: user.id, flightId: flight.id, frozenPrice: flight.price })
      });
      showToast(`🔒 Price frozen at ${flight.price != null ? money(flight.price) : 'Live fare unavailable'} for 48 hours!`);
      loadUserBookings();
    } catch (err) {
      alert('Freeze Error: ' + err.message);
    }
  };

  const bookHotelRoom = async (hotel, room) => {
    if (!user) {
      setAuthMode('login');
      setShowAuthModal(true);
      return;
    }
    try {
      const res = await api('/bookings', {
        method: 'POST',
        body: JSON.stringify({
          userId: user.id,
          bookingType: 'HOTEL',
          itemId: hotel.id,
          roomId: room.id,
          passengerName: user.name,
          passengerEmail: user.email,
          roomType: room.roomType,
          totalAmount: room.price || hotel.currentPrice || hotel.basePrice
        })
      });
      setSelectedHotel(null);
      if (res.status === 'PENDING_PAYMENT') {
        showToast(`Booking #${res.id} created. Complete payment to confirm it.`);
        await startPayment(res);
      } else {
        showToast(`🏨 Hotel room booked! Confirmation ID: #${res.id || 'HOTEL-' + Date.now()}`);
      }
      loadUserBookings();
    } catch (err) {
      alert('Hotel Booking Error: ' + err.message);
    }
  };

  const openCancelModal = async (booking) => {
    setCancelModal(booking); setCancelReason('Change of plans');
    try { setCancelReasons(await api('/bookings/cancellation-reasons')); } catch { setCancelReasons(['Change of plans','Found a better price','Flight schedule changed','Personal emergency','Duplicate booking','Hotel/room issue','Other']); }
  };
  const cancelBooking = async () => {
    if (!cancelModal) return;
    try {
      const refund = await api(`/bookings/${cancelModal.id}/cancel`, {method:'POST',body:JSON.stringify({reason:cancelReason})});
      setCancelModal(null); showToast(`Cancellation accepted. Refund ${money(refund.refundAmount)} is PENDING.`);
      loadUserBookings();
    } catch(e){ alert('Cancellation failed: '+e.message); }
  };

  const openHotelModal = async (hotel) => {
    setSelectedHotel(hotel);
    try {
      const [rooms, hist] = await Promise.all([api(`/hotels/${hotel.id}/rooms`), api(`/hotels/${hotel.id}/history`).catch(()=>[])]);
      setHotelRooms(rooms || []); setHotelHistory(hist || []);
    } catch {
      setHotelRooms([]); setHotelHistory([]);
    }
  };

  const openReviewsModal = async (hotel) => {
    setReviewHotel(hotel);
    try {
      const revs = await api(`/reviews?entityType=HOTEL&entityId=${hotel.id}&sort=${reviewSort}`);
      setHotelReviews(revs || []);
    } catch {
      setHotelReviews([]);
    }
  };

  const submitReview = async (e) => {
    e.preventDefault();
    if (!user) {
      setShowAuthModal(true);
      return;
    }
    try {
      const newRev = await api('/reviews', {
        method: 'POST',
        body: JSON.stringify({
          entityType: 'HOTEL',
          entityId: reviewHotel.id,
          userId: user.id,
          rating: Number(newReviewRating),
          text: newReviewText,
          photoUrl: reviewPhoto
        })
      });
      setHotelReviews([newRev, ...hotelReviews]);
      setNewReviewText(''); setReviewPhoto('');
      showToast('Thank you! Review posted successfully.');
    } catch (err) {
      alert(err.message);
    }
  };

  const submitReply = async (reviewId) => {
    const comment = replyInput[reviewId];
    if (!comment || !user) return;
    try {
      await api(`/reviews/${reviewId}/reply`, {
        method: 'POST',
        body: JSON.stringify({
          userId: user.id,
          text: comment
        })
      });
      setReplyInput({ ...replyInput, [reviewId]: '' });
      openReviewsModal(reviewHotel);
      showToast('Reply submitted!');
    } catch (err) {
      alert(err.message);
    }
  };

  // Filtered flights
  const filteredFlights = useMemo(() => {
    return flightList.filter(f => {
      const matchAirline = selectedAirlineFilter === 'ALL' || f.airline === selectedAirlineFilter;
      const matchPrice = f.price <= maxFlightPrice;
      return matchAirline && matchPrice;
    });
  }, [flightList, selectedAirlineFilter, maxFlightPrice]);

  // Filtered hotels
  const filteredHotels = useMemo(() => {
    return hotelList.filter(h => h.rating >= minRatingFilter);
  }, [hotelList, minRatingFilter]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans antialiased pb-20">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 bg-emerald-600 text-white px-5 py-3 rounded-2xl shadow-2xl flex items-center gap-3 animate-bounce">
          <CheckCircle2 size={20} />
          <span className="text-sm font-semibold">{toast}</span>
        </div>
      )}

      {/* Top Navbar */}
      <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200/80 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center gap-8">
            <div className="flex items-center gap-2 cursor-pointer" onClick={() => setActiveTab('flights')}>
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white font-extrabold text-xl shadow-md shadow-blue-500/20">
                M
              </div>
              <div>
                <span className="text-2xl font-black tracking-tight text-slate-800">
                  make<span className="text-red-500">my</span>trip
                </span>
                <span className="block text-[10px] uppercase font-bold tracking-widest text-slate-400 -mt-1">
                  Enterprise Travel
                </span>
              </div>
            </div>

            {/* Main Navigation Tabs */}
            <nav className="hidden md:flex items-center gap-2 bg-slate-100 p-1.5 rounded-2xl">
              <button
                onClick={() => setActiveTab('flights')}
                className={`flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-bold transition-all ${
                  activeTab === 'flights' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Plane size={18} />
                Flights
              </button>
              <button
                onClick={() => setActiveTab('hotels')}
                className={`flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-bold transition-all ${
                  activeTab === 'hotels' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <HotelIcon size={18} />
                Hotels & Homestays
              </button>
              {user && (
                <button
                  onClick={() => { setActiveTab('bookings'); loadUserBookings(); }}
                  className={`flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-bold transition-all ${
                    activeTab === 'bookings' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <Ticket size={18} />
                  My Trips
                  {userBookings.length > 0 && (
                    <span className="bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                      {userBookings.length}
                    </span>
                  )}
                </button>
              )}
            </nav>
          </div>

          {/* User Account Controls */}
          <div className="flex items-center gap-3">
            {user ? (
              <div className="flex items-center gap-3 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-2xl">
                <div className="w-8 h-8 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center font-bold text-sm">
                  {user.name?.charAt(0).toUpperCase()}
                </div>
                <div className="text-left hidden sm:block">
                  <div className="text-xs font-bold text-slate-800">{user.name}</div>
                  <div className="text-[10px] text-slate-500">{user.email}</div>
                </div>
                <button
                  onClick={handleLogout}
                  title="Logout"
                  className="p-1.5 text-slate-400 hover:text-red-500 transition-colors ml-1"
                >
                  <LogOut size={16} />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <button
                  onClick={() => { setAuthMode('login'); setAuthStep('form'); setAuthOtp(''); setAuthErr(''); setShowAuthModal(true); }}
                  className="px-4 py-2 text-sm font-bold text-slate-700 hover:text-blue-600 transition-colors"
                >
                  Log In
                </button>
                <button
                  onClick={() => { setAuthMode('signup'); setAuthStep('form'); setAuthOtp(''); setAuthErr(''); setShowAuthModal(true); }}
                  className="px-4 py-2 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-xl hover:shadow-lg hover:shadow-blue-500/25 transition-all"
                >
                  Create Account
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <div className="bg-slate-950 text-white border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2 flex flex-wrap items-center justify-between gap-3 text-[11px] font-semibold">
          <div className="flex items-center gap-3">
            <span className={`inline-flex items-center gap-1.5 ${liveConnected ? 'text-emerald-400' : 'text-amber-300'}`}><span className="w-2 h-2 rounded-full bg-current animate-pulse"/> {liveConnected ? 'LIVE WebSocket connected' : 'Live polling active'}</span>
            <span className="text-slate-400">Aviation: {providerStatus?.aviationstack ? 'Aviationstack' : 'local live engine'}</span>
            <span className="text-slate-400">Email OTP: {providerStatus?.smtp ? 'SMTP ready' : 'dev mode'}</span>
            {user && <button onClick={requestBrowserNotifications} className="text-slate-200 hover:text-white flex items-center gap-1"><Bell size={13}/> {browserNotifications ? 'Notifications ON' : 'Enable alerts'}</button>}
          </div>
          <div className="flex items-center gap-4 text-slate-300">
            {liveWeather && <span>🌤 {Number(liveWeather.temperature_2m).toFixed(1)}°C · Wind {Number(liveWeather.wind_speed_10m).toFixed(0)} km/h</span>}
            {liveFx && <span>USD ₹{Number((1 / (liveFx.USD || 1))).toFixed(2)}</span>}
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        {/* HERO SEARCH BANNER */}
        <section className="bg-gradient-to-br from-blue-900 via-indigo-900 to-slate-900 rounded-3xl p-6 sm:p-10 text-white shadow-2xl relative overflow-hidden mb-8">
          <div className="absolute top-0 right-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none" />
          <div className="relative z-10">
            <div className="flex items-center gap-2 text-blue-400 text-xs uppercase tracking-widest font-extrabold mb-2">
              <Sparkles size={16} /> Best Price Guaranteed • Real-time Fare Updates
            </div>
            <h1 className="text-2xl sm:text-4xl font-black tracking-tight text-white mb-6">
              {activeTab === 'flights' && 'Book Domestic & International Flights'}
              {activeTab === 'hotels' && 'Book Luxury Hotels, Resorts & Homestays'}
              {activeTab === 'bookings' && 'Your Booked Trips & Real-Time Tracking'}
            </h1>

            {/* Flight Search Controls Bar */}
            {activeTab === 'flights' && (
              <div className="bg-white rounded-2xl p-4 sm:p-5 shadow-2xl text-slate-800">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-center">
                  <LocationAutocomplete
                    label="From City / Airport"
                    value={flightSource}
                    placeholder="Search any city or airport worldwide…"
                    onSelect={async (x) => { const label=x.city || x.name || ''; setFlightSource(label); setFlightSourceCode(x.code || ''); if(!x.code){ try { const a=await api(`/locations/resolve-airport?q=${encodeURIComponent(label)}`); setFlightSourceCode(a?.iataCode || ''); } catch {} } }}
                  />

                  <LocationAutocomplete
                    label="To Destination"
                    value={flightDest}
                    placeholder="Search any destination worldwide…"
                    onSelect={async (x) => { const label=x.city || x.name || ''; setFlightDest(label); setFlightDestCode(x.code || ''); if(!x.code){ try { const a=await api(`/locations/resolve-airport?q=${encodeURIComponent(label)}`); setFlightDestCode(a?.iataCode || ''); } catch {} } }}
                  />

                  <div className="border border-slate-200 rounded-xl p-3 hover:border-blue-500 transition-colors">
                    <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Departure Date</label>
                    <input
                      type="date"
                      value={flightDate}
                      onChange={(e) => setFlightDate(e.target.value)}
                      className="w-full font-bold text-slate-800 bg-transparent focus:outline-none"
                    />
                  </div>

                  <button
                    onClick={() => providerStatus?.amadeus ? loadRealProviderData() : loadFlights()}
                    className="w-full h-full min-h-[58px] bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-extrabold rounded-xl shadow-lg shadow-blue-500/30 flex items-center justify-center gap-2 transition-all transform active:scale-95"
                  >
                    <Search size={20} />
                    Search Flights
                  </button>
                </div>
              </div>
            )}

            {/* Hotel Search Controls Bar */}
            {activeTab === 'hotels' && (
              <div className="bg-white rounded-2xl p-4 sm:p-5 shadow-2xl text-slate-800">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                  <LocationAutocomplete
                    label="City, Location or Property"
                    value={hotelCity}
                    placeholder="Search hotels anywhere in India or worldwide…"
                    onSelect={(x) => { setHotelCity(x.city || x.name || ''); setHotelCityCode(x.code || ''); }}
                  />

                  <div className="border border-slate-200 rounded-xl p-3 hover:border-blue-500 transition-colors">
                    <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Rooms & Guests</label>
                    <div className="font-bold text-slate-800">1 Room, 2 Adults • Standard</div>
                  </div>

                  <button
                    onClick={() => providerStatus?.amadeus ? loadRealProviderData() : loadHotels()}
                    className="w-full h-full min-h-[58px] bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-extrabold rounded-xl shadow-lg shadow-blue-500/30 flex items-center justify-center gap-2 transition-all transform active:scale-95"
                  >
                    <Search size={20} />
                    Search Hotels
                  </button>
                </div>
              </div>
            )}
          </div>
        </section>

        {/* INDIA SMART LOCATION SEARCH */}
        <section className="bg-white border border-slate-200 rounded-3xl p-5 mb-8 shadow-sm">
          <div className="flex items-center gap-2 mb-3"><Compass size={20} className="text-blue-600"/><h3 className="font-black text-slate-900">Worldwide Location Search</h3><span className="text-[10px] font-bold bg-emerald-50 text-emerald-700 px-2 py-1 rounded-full">India + Worldwide • Cities • Airports • IATA • Districts</span></div>
          <div className="relative"><input value={locationQuery} onChange={e=>searchLocations(e.target.value)} placeholder="Search any city, country, airport, IATA code or landmark..." className="w-full p-4 rounded-2xl border border-slate-200 font-semibold focus:outline-none focus:border-blue-500"/>
          {locationResults.length>0 && <div className="absolute z-30 left-0 right-0 mt-2 bg-white border border-slate-200 rounded-2xl shadow-2xl max-h-72 overflow-auto">{locationResults.slice(0,12).map((x,i)=><button key={x.id||i} onClick={()=>{setLocationQuery(x.name || x.city); setLocationResults([]); if(x.type==='AIRPORT'&&x.city)setHotelCity(x.city); else if(x.city)setHotelCity(x.city);}} className="w-full text-left p-3 hover:bg-blue-50 flex items-center justify-between"><span><b>{x.name || x.city}</b><span className="text-xs text-slate-500 ml-2">{x.type} {x.code?`• ${x.code}`:''}</span></span><span className="text-[10px] text-slate-400">{x.state||x.country||''}</span></button>)}</div>}</div>
        </section>

        {/* EXTERNAL LIVE PROVIDERS */}
        <section className="bg-slate-950 text-white rounded-3xl p-5 mb-8 shadow-xl">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div className="flex items-center gap-2 font-black"><RefreshCw size={18} className={liveProviderLoading ? 'animate-spin' : ''}/> External Live Data Center</div>
              <p className="text-xs text-slate-300 mt-1">Real provider mode: Amadeus flight/hotel inventory + Aviationstack flight boards + live weather/FX.</p>
            </div>
            <button onClick={loadRealProviderData} disabled={liveProviderLoading} className="px-4 py-2 rounded-xl bg-white text-slate-900 text-xs font-black disabled:opacity-50">
              {liveProviderLoading ? 'Refreshing…' : 'Refresh real data'}
            </button>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-2 mt-4 text-[10px] font-bold">
            <span className={`px-3 py-2 rounded-lg ${providerStatus?.amadeus ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'}`}>Amadeus: {providerStatus?.amadeus ? 'CONNECTED' : 'KEY REQUIRED'}</span>
            <span className={`px-3 py-2 rounded-lg ${providerStatus?.aviationstack ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'}`}>Aviationstack: {providerStatus?.aviationstack ? 'CONNECTED' : 'KEY REQUIRED'}</span>
            <span className="px-3 py-2 rounded-lg bg-emerald-500/20 text-emerald-300">Weather: LIVE</span>
            <span className="px-3 py-2 rounded-lg bg-emerald-500/20 text-emerald-300">FX: LIVE</span>
            <span className={`px-3 py-2 rounded-lg ${liveConnected ? 'bg-emerald-500/20 text-emerald-300' : 'bg-amber-500/20 text-amber-300'}`}>WebSocket: {liveConnected ? 'CONNECTED' : 'CONNECTING'}</span>
          </div>
          {(liveFlightOffers.length || liveHotelOffers.length || liveAirportBoard.length) > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mt-4">
              <div className="bg-white/5 rounded-xl p-3"><div className="font-black text-sm">Live flight offers</div><div className="text-2xl font-black mt-1">{liveFlightOffers.length}</div><div className="text-[10px] text-slate-300">Amadeus shopping results</div></div>
              <div className="bg-white/5 rounded-xl p-3"><div className="font-black text-sm">Live hotel offers</div><div className="text-2xl font-black mt-1">{liveHotelOffers.length}</div><div className="text-[10px] text-slate-300">Current availability/pricing</div></div>
              <div className="bg-white/5 rounded-xl p-3"><div className="font-black text-sm">Airport board</div><div className="text-2xl font-black mt-1">{liveAirportBoard.length}</div><div className="text-[10px] text-slate-300">Live departures from selected origin</div></div>
            </div>
          )}
        </section>

        {/* PERSONALIZED REAL-TIME RECOMMENDATIONS */}
        {user && Array.isArray(recommendations) && recommendations.length > 0 && (
          <section className="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-3xl p-5 mb-8">
            <div className="flex flex-wrap justify-between gap-3 items-center mb-4">
              <div>
                <div className="flex items-center gap-2 text-amber-800 font-black"><Sparkles size={20}/> Personalized recommendations</div>
                <p className="text-xs text-amber-700 mt-1">Collaborative signals + your preferences + live prices.</p>
              </div>
              <span className="text-[10px] font-bold bg-white px-3 py-2 rounded-full border border-amber-200">Why this? shown on every card</span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {recommendations.slice(0,6).map((r,i) => (
                <div key={`${r.itemType}-${r.itemId}-${i}`} className="bg-white p-4 rounded-2xl border border-amber-100 shadow-sm">
                  <div className="flex justify-between items-start gap-2">
                    <div><div className="font-black text-sm text-slate-900">{r.title}</div><div className="text-xs text-slate-500 mt-1">{r.city}</div></div>
                    <span className="text-[10px] font-black text-emerald-700 bg-emerald-50 px-2 py-1 rounded-full">{Math.round((r.score||0)*100)}% match</span>
                  </div>
                  <div className="text-sm font-black text-blue-600 mt-3">{money(r.price)}</div>
                  <details className="mt-2"><summary className="text-[11px] font-bold text-slate-600 cursor-pointer">Why this recommendation?</summary><p className="text-[11px] text-slate-500 mt-2">{r.why}</p></details>
                  <div className="flex gap-2 mt-3">
                    <button onClick={()=>api('/recommendations/feedback',{method:'POST',body:JSON.stringify({userId:user.id,itemType:r.itemType,itemId:r.itemId,helpful:true})}).then(()=>showToast('Thanks — recommendations improved.'))} className="flex-1 text-[11px] font-bold py-2 rounded-lg bg-emerald-50 text-emerald-700"><ThumbsUp size={12} className="inline mr-1"/>Helpful</button>
                    <button onClick={()=>api('/recommendations/feedback',{method:'POST',body:JSON.stringify({userId:user.id,itemType:r.itemType,itemId:r.itemId,helpful:false})}).then(()=>showToast('Got it — we will refine suggestions.'))} className="flex-1 text-[11px] font-bold py-2 rounded-lg bg-slate-100 text-slate-600">Not for me</button>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* FLIGHTS TAB VIEW */}
        {activeTab === 'flights' && (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Filters Sidebar */}
            <div className="lg:col-span-1 space-y-6">
              <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-extrabold text-slate-800 flex items-center gap-2">
                    <SlidersHorizontal size={18} /> Filters
                  </h3>
                  <button
                    onClick={() => { setSelectedAirlineFilter('ALL'); setMaxFlightPrice(60000); }}
                    className="text-xs font-semibold text-blue-600 hover:underline"
                  >
                    Reset
                  </button>
                </div>

                {/* Filter by Airline */}
                <div className="mb-6">
                  <label className="text-xs font-bold uppercase text-slate-400 block mb-2">Airlines</label>
                  <div className="space-y-2">
                    <label className="flex items-center gap-2 text-sm font-medium text-slate-700 cursor-pointer">
                      <input
                        type="radio"
                        name="airline"
                        checked={selectedAirlineFilter === 'ALL'}
                        onChange={() => setSelectedAirlineFilter('ALL')}
                        className="text-blue-600 focus:ring-blue-500"
                      />
                      All Airlines ({flightList.length})
                    </label>
                    {flightAirlines.map(airline => (
                      <label key={airline} className="flex items-center gap-2 text-sm font-medium text-slate-700 cursor-pointer">
                        <input
                          type="radio"
                          name="airline"
                          checked={selectedAirlineFilter === airline}
                          onChange={() => setSelectedAirlineFilter(airline)}
                          className="text-blue-600 focus:ring-blue-500"
                        />
                        {airline}
                      </label>
                    ))}
                  </div>
                </div>

                {/* Filter by Price */}
                <div>
                  <div className="flex justify-between items-center text-xs font-bold mb-2">
                    <span className="text-slate-400 uppercase">Max Budget</span>
                    <span className="text-blue-600 font-extrabold">{money(maxFlightPrice)}</span>
                  </div>
                  <input
                    type="range"
                    min="3000"
                    max="60000"
                    step="1000"
                    value={maxFlightPrice}
                    onChange={(e) => setMaxFlightPrice(Number(e.target.value))}
                    className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-blue-600"
                  />
                </div>
              </div>

              {/* Price Lock Promo Card */}
              <div className="bg-gradient-to-br from-indigo-50 to-blue-50 border border-blue-200/80 rounded-2xl p-5">
                <div className="w-10 h-10 bg-blue-600 text-white rounded-xl flex items-center justify-center mb-3">
                  <Lock size={20} />
                </div>
                <h4 className="font-bold text-slate-900 text-sm mb-1">MakeMyTrip Price Freeze</h4>
                <p className="text-xs text-slate-600 leading-relaxed mb-3">
                  Worried fares will jump? Lock today's lowest fare for 48 hours for just zero risk!
                </p>
                <div className="text-[11px] font-bold text-blue-700 flex items-center gap-1">
                  <ShieldCheck size={14} /> 100% Price Protection Guaranteed
                </div>
              </div>
            </div>

            {/* Flights List View */}
            <div className="lg:col-span-3 space-y-4">
              <div className="flex justify-between items-center mb-2">
                <h2 className="text-lg font-black text-slate-800">
                  Available Flights ({filteredFlights.length})
                </h2>
                <div className="flex items-center gap-2 text-xs font-bold text-slate-500">
                  <RefreshCw size={14} className={flightLoading ? 'animate-spin' : ''} />
                  <span>Real-time Rates Active</span>
                </div>
              </div>

              {filteredFlights.length === 0 ? (
                <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
                  <Plane size={48} className="mx-auto text-slate-300 mb-3" />
                  <h3 className="font-bold text-slate-700 text-lg">No direct flights found for this filter</h3>
                  <p className="text-sm text-slate-500 mt-1">Try selecting different cities or resetting your budget filter.</p>
                </div>
              ) : (
                filteredFlights.map(flight => (
                  <div
                    key={flight.id}
                    className="bg-white rounded-2xl p-5 border border-slate-200 hover:border-blue-400 hover:shadow-xl transition-all duration-300"
                  >
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                      {/* Airline info */}
                      <div className="flex items-center gap-4 min-w-[200px]">
                        <div className="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-blue-600 font-black">
                          <Plane size={24} />
                        </div>
                        <div>
                          <div className="font-extrabold text-slate-900 text-base">{flight.airline}</div>
                          <div className="text-xs text-slate-400 font-mono font-bold">{flight.flightNumber}</div>
                          <span className={`inline-block mt-1 text-[10px] font-extrabold px-2 py-0.5 rounded-full ${
                            flight.status === 'DELAYED' ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'
                          }`}>
                            {flight.status || 'ON TIME'}
                          </span>
                        </div>
                      </div>

                      {/* Route & Times */}
                      <div className="flex items-center justify-between flex-1 max-w-md px-4">
                        <div className="text-left">
                          <div className="text-xl font-black text-slate-900">
                            {flight.departureTime ? new Date(flight.departureTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '10:30'}
                          </div>
                          <div className="text-xs font-bold text-slate-500">{flight.sourceCity}</div>
                        </div>

                        <div className="flex flex-col items-center px-4 flex-1">
                          <span className="text-[10px] font-bold text-slate-400 mb-1">Non-stop • 2h 15m</span>
                          <div className="w-full flex items-center gap-1">
                            <div className="h-0.5 flex-1 bg-slate-200"></div>
                            <Plane size={14} className="text-blue-500 transform rotate-90" />
                            <div className="h-0.5 flex-1 bg-slate-200"></div>
                          </div>
                          <span className="text-[10px] text-emerald-600 font-semibold mt-1">
                            {flight.availableSeats} seats left
                          </span>
                        </div>

                        <div className="text-right">
                          <div className="text-xl font-black text-slate-900">
                            {flight.arrivalTime ? new Date(flight.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '12:45'}
                          </div>
                          <div className="text-xs font-bold text-slate-500">{flight.destinationCity}</div>
                          <div className="text-[10px] text-emerald-600 font-bold mt-1">ETA: {flight.estimatedArrivalTime ? new Date(flight.estimatedArrivalTime).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'}) : 'live'}</div>
                        </div>
                      </div>

                      {/* Price & Action Buttons */}
                      <div className="flex items-center justify-between md:flex-col md:items-end gap-3 border-t md:border-t-0 pt-4 md:pt-0">
                        <div>
                          <div className="text-2xl font-black text-slate-900 tracking-tight">
                            {money(flight.price)}
                          </div>
                          <div className="text-[10px] text-slate-400 font-medium">per adult (incl. taxes)</div>
                        </div>

                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => flight.external ? showToast('Live provider flight result. Booking requires a live flight-shopping provider.') : openFlightDetails(flight)}
                            title="Live Price Trend & Updates"
                            className="p-2.5 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
                          >
                            <TrendingDown size={16} />
                          </button>
                          <button
                            onClick={() => toggleTrackFlight(flight)}
                            title="Track flight live"
                            className="p-2.5 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 transition-colors"
                          >
                            <Bell size={14} />
                          </button>
                          <button
                            onClick={() => freezeFlightPrice(flight)}
                            title="Freeze Price for 48h"
                            className="p-2.5 rounded-xl border border-amber-300 bg-amber-50 text-amber-800 hover:bg-amber-100 transition-colors font-bold text-xs flex items-center gap-1"
                          >
                            <Lock size={14} /> Freeze
                          </button>
                          <button
                            onClick={() => openSeatMap(flight)}
                            className="px-4 py-2.5 border border-blue-200 bg-blue-50 text-blue-700 font-bold text-xs rounded-xl flex items-center gap-1"
                          >
                            <Armchair size={14}/> Seats
                          </button>
                          <button
                            onClick={() => flight.external ? showToast('Live route result. Add a live flight-shopping/booking provider to enable checkout.') : bookFlightTicket(flight)}
                            className="px-5 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold text-sm rounded-xl shadow-md shadow-blue-500/20 transition-all transform active:scale-95"
                          >
                            {flight.external ? 'Live Result' : 'Book Now'}
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* HOTELS TAB VIEW */}
        {activeTab === 'hotels' && (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Sidebar Filter for Hotels */}
            <div className="lg:col-span-1 space-y-6">
              <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-extrabold text-slate-800 flex items-center gap-2">
                    <SlidersHorizontal size={18} /> Star Rating
                  </h3>
                  <button onClick={() => setMinRatingFilter(0)} className="text-xs font-semibold text-blue-600 hover:underline">
                    Reset
                  </button>
                </div>

                <div className="space-y-2">
                  {[
                    { stars: 0, label: 'All Ratings' },
                    { stars: 4.5, label: '4.5+ Exceptional' },
                    { stars: 4.8, label: '4.8+ Luxury Palaces' }
                  ].map(r => (
                    <label key={r.stars} className="flex items-center gap-2 text-sm font-medium text-slate-700 cursor-pointer">
                      <input
                        type="radio"
                        name="rating"
                        checked={minRatingFilter === r.stars}
                        onChange={() => setMinRatingFilter(r.stars)}
                        className="text-blue-600 focus:ring-blue-500"
                      />
                      <span className="flex items-center gap-1">
                        {r.label}
                        {r.stars > 0 && <Star size={12} className="text-amber-500 fill-amber-500" />}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Verified Stays Guarantee */}
              <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-5">
                <div className="w-10 h-10 bg-emerald-600 text-white rounded-xl flex items-center justify-center mb-3">
                  <ShieldCheck size={20} />
                </div>
                <h4 className="font-bold text-slate-900 text-sm mb-1">MakeMyTrip Assured Stays</h4>
                <p className="text-xs text-slate-600 leading-relaxed">
                  Every property is 100% verified for sanitized rooms, 24/7 power backup, and authentic dining.
                </p>
              </div>
            </div>

            {/* Hotel Cards Grid */}
            <div className="lg:col-span-3 grid grid-cols-1 md:grid-cols-2 gap-6">
              {filteredHotels.map(hotel => (
                <div
                  key={hotel.id}
                  className="bg-white rounded-3xl overflow-hidden border border-slate-200 hover:shadow-2xl transition-all duration-300 flex flex-col justify-between"
                >
                  <div className="relative h-52 overflow-hidden">
                    <img
                      src={hotel.imageUrl}
                      alt={hotel.name}
                      className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-500"
                    />
                    <div className="absolute top-4 left-4 bg-white/95 backdrop-blur-md px-3 py-1 rounded-xl text-xs font-black text-slate-800 flex items-center gap-1 shadow-md">
                      <Star size={14} className="text-amber-500 fill-amber-500" />
                      {hotel.rating} ({hotel.reviewCount}+ reviews)
                    </div>
                    <div className="absolute top-4 right-4 bg-slate-900/80 backdrop-blur-md px-3 py-1 rounded-xl text-xs font-bold text-white shadow-md">
                      {hotel.city}
                    </div>
                  </div>

                  <div className="p-5 flex-1 flex flex-col justify-between">
                    <div>
                      <h3 className="font-extrabold text-slate-900 text-lg leading-tight mb-1">{hotel.name}{hotel.external && <span className="ml-2 text-[9px] px-2 py-1 rounded-full bg-slate-100 text-slate-500">GLOBAL DISCOVERY</span>}</h3>
                      <p className="text-xs text-slate-500 flex items-center gap-1 mb-3">
                        <MapPin size={13} className="text-slate-400" />
                        {hotel.address}
                      </p>
                      <p className="text-xs text-slate-600 line-clamp-2 mb-4 leading-relaxed">
                        {hotel.description}
                      </p>

                      {/* Amenities chips */}
                      <div className="flex flex-wrap gap-1.5 mb-4">
                        {hotel.amenities?.split(',').slice(0, 4).map((a, i) => (
                          <span key={i} className="text-[10px] font-semibold bg-slate-100 text-slate-700 px-2 py-0.5 rounded-lg">
                            {a.trim()}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div className="border-t border-slate-100 pt-4 flex items-center justify-between">
                      <div>
                        <div className="text-[10px] uppercase font-bold text-slate-400">Starts from</div>
                        <div className="text-xl font-black text-slate-900">{hotel.basePrice != null ? money(hotel.basePrice) : 'Live rate unavailable'}</div>
                        <div className="text-[10px] text-slate-400">{hotel.external ? 'Map discovery • not bookable yet' : '+ taxes per night'}</div>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => hotel.external ? window.open(`https://www.openstreetmap.org/?mlat=${hotel.latitude}&mlon=${hotel.longitude}#map=18/${hotel.latitude}/${hotel.longitude}`, '_blank') : openReviewsModal(hotel)}
                          className="p-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 transition-colors"
                          title="Read Customer Reviews"
                        >
                          <MessageCircle size={16} />
                        </button>
                        <button
                          onClick={() => hotel.external ? window.open(`https://www.openstreetmap.org/?mlat=${hotel.latitude}&mlon=${hotel.longitude}#map=18/${hotel.latitude}/${hotel.longitude}`, '_blank') : openHotelModal(hotel)}
                          className="px-4 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-bold text-xs rounded-xl shadow-md transition-all"
                        >
                          {hotel.external ? 'View on Map' : 'View Rooms & Book'}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* BOOKINGS TAB VIEW */}
        {activeTab === 'bookings' && (
          <div className="space-y-8">
            {/* Active Price Freezes */}
            {userFreezes.length > 0 && (
              <div className="bg-amber-50/70 border border-amber-200 rounded-3xl p-6">
                <h3 className="text-lg font-black text-amber-900 mb-4 flex items-center gap-2">
                  <Lock size={20} className="text-amber-600" /> Active Price Locks (48h Protection)
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {userFreezes.map(f => (
                    <div key={f.id} className="bg-white p-4 rounded-2xl border border-amber-200 shadow-sm flex items-center justify-between">
                      <div>
                        <div className="font-bold text-slate-900 text-sm">Flight #{f.flightId} Lock</div>
                        <div className="text-xs text-slate-500">Locked Price: <span className="font-extrabold text-blue-600">{money(f.frozenPrice)}</span></div>
                        <div className="text-[10px] text-amber-700 font-semibold mt-1">Expires: {new Date(f.expiresAt).toLocaleDateString()}</div>
                      </div>
                      <span className="text-xs font-bold px-3 py-1 bg-amber-100 text-amber-800 rounded-full">ACTIVE</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {trackedFlights.length > 0 && (
              <div className="bg-slate-950 text-white rounded-3xl p-6">
                <div className="flex items-center justify-between mb-4"><h3 className="font-black flex items-center gap-2"><Bell size={18}/> Live Flight Watchlist</h3><span className="text-[10px] text-emerald-300">WebSocket + 15s engine</span></div>
                <div className="grid md:grid-cols-2 gap-3">{trackedFlights.map(f=><div key={f.id} className="bg-white/10 rounded-2xl p-4 flex justify-between items-center"><div><b>{f.airline} {f.flightNumber}</b><div className="text-xs text-slate-300">{f.sourceCity} → {f.destinationCity}</div><div className="text-xs mt-1 text-emerald-300">{f.status} • ETA {f.estimatedArrivalTime ? new Date(f.estimatedArrivalTime).toLocaleTimeString() : 'updating'}</div></div><button onClick={()=>api(`/flights/${f.id}/track?userId=${user.id}`,{method:'DELETE'}).then(()=>loadUserBookings())} className="text-xs font-bold px-3 py-2 rounded-lg bg-white/10">Untrack</button></div>)}</div>
              </div>
            )}

            {/* Confirmed Bookings */}
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm">
              <h3 className="text-xl font-black text-slate-900 mb-6 flex items-center gap-2">
                <Ticket size={22} className="text-blue-600" /> Confirmed Bookings ({userBookings.length})
              </h3>

              {userBookings.length === 0 ? (
                <div className="text-center py-12">
                  <Luggage size={48} className="mx-auto text-slate-300 mb-3" />
                  <p className="text-slate-500 font-medium">No bookings yet. Explore flights and luxury hotels above!</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {userBookings.map(b => (
                    <div key={b.id} className="border border-slate-200 rounded-2xl p-5 hover:border-slate-300 transition-all flex flex-col md:flex-row justify-between md:items-center gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className={`text-[10px] font-black px-2.5 py-0.5 rounded-full ${
                            b.bookingType === 'FLIGHT' ? 'bg-blue-100 text-blue-800' : 'bg-purple-100 text-purple-800'
                          }`}>
                            {b.bookingType}
                          </span>
                          <span className={`text-[10px] font-black px-2 py-0.5 rounded-full ${
                            b.status === 'CONFIRMED' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                          }`}>
                            {b.status}
                          </span>
                        </div>
                        <h4 className="font-extrabold text-slate-900 text-base mt-2">{b.title}</h4>
                        <div className="text-xs text-slate-500 mt-1">
                          Booked on: {new Date(b.bookingTime).toLocaleString()}
                        </div>
                      </div>

                      <div className="flex items-center justify-between md:justify-end gap-4 border-t md:border-t-0 pt-3 md:pt-0">
                        <div className="text-right">
                          <div className="text-lg font-black text-slate-900">{money(b.totalAmount)}</div>
                          <div className="text-[10px] text-slate-400">Total Paid</div>
                        </div>

                        {b.refundStatus && b.refundStatus !== 'NOT_REQUESTED' && (
                          <div className="text-right mr-2">
                            <div className="text-[10px] uppercase font-bold text-slate-400">Refund tracker</div>
                            <div className={`text-xs font-black ${b.refundStatus === 'COMPLETED' ? 'text-emerald-600' : 'text-amber-600'}`}>{b.refundStatus}</div>
                            {b.refundAmount != null && <div className="text-[10px] text-slate-500">{money(b.refundAmount)} • ETA {b.expectedRefundDate ? new Date(b.expectedRefundDate).toLocaleDateString() : '3 days'}</div>}
                          </div>
                        )}
                        {b.status === 'PENDING_PAYMENT' && (
                          <button onClick={() => startPayment(b)} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-black rounded-xl">Pay Now</button>
                        )}
                        {b.status === 'CONFIRMED' && (
                          <button
                            onClick={() => openCancelModal(b)}
                            className="px-4 py-2 border border-rose-200 bg-rose-50 hover:bg-rose-100 text-rose-700 text-xs font-bold rounded-xl transition-all"
                          >
                            Cancel & Refund
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* MODAL 1: FLIGHT DETAILS, REAL-TIME GRAPH & UPDATES */}
      {activeFlightModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl border border-slate-100 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h3 className="text-xl font-black text-slate-900">
                  {activeFlightModal.airline} • {activeFlightModal.flightNumber}
                </h3>
                <p className="text-xs text-slate-500 font-bold mt-0.5">
                  {activeFlightModal.sourceCity} ➔ {activeFlightModal.destinationCity}
                </p>
              </div>
              <button
                onClick={() => setActiveFlightModal(null)}
                className="p-2 rounded-full hover:bg-slate-100 text-slate-500"
              >
                <X size={20} />
              </button>
            </div>

            {/* Price Graph */}
            <div className="mb-6">
              <h4 className="text-xs font-extrabold uppercase text-slate-400 tracking-wider mb-2">
                Live Price Dynamic Variations
              </h4>
              <PriceGraph items={flightHistory} />
            </div>

            {/* Flight Status Log */}
            <div className="mb-6">
              <h4 className="text-xs font-extrabold uppercase text-slate-400 tracking-wider mb-2">
                Live Gate & Status Radar
              </h4>
              <div className="space-y-2">
                {flightUpdates.length === 0 ? (
                  <div className="text-xs text-emerald-600 bg-emerald-50 p-3 rounded-xl font-bold flex items-center gap-2">
                    <CheckCircle2 size={16} /> Flight is operating strictly on schedule. No delays reported.
                  </div>
                ) : (
                  flightUpdates.map((u, i) => (
                    <div key={i} className="text-xs bg-slate-50 p-3 rounded-xl border border-slate-200/80 flex justify-between items-center">
                      <div>
                        <span className="font-bold text-slate-800">{u.status}</span>
                        {u.delayMinutes > 0 && <span className="text-rose-600 ml-2 font-bold">(Delayed by {u.delayMinutes} mins)</span>}
                      </div>
                      <span className="text-slate-400 text-[10px]">{new Date(u.timestamp).toLocaleTimeString()}</span>
                    </div>
                  ))
                )}
              </div>
            </div>

            <div className="mb-5">
              <h4 className="text-xs font-extrabold uppercase text-slate-400 tracking-wider mb-2">Traveler flight reviews</h4>
              <div className="space-y-2">{flightReviews.slice(0,3).map(r=><div key={r.id} className="p-3 bg-slate-50 rounded-xl text-xs"><b>★ {r.rating}/5</b><span className="ml-2 text-slate-600">{r.text}</span></div>)}{flightReviews.length===0&&<span className="text-xs text-slate-400">No flight reviews yet.</span>}</div>
            </div>
            <div className="flex gap-3">
              <button
                onClick={() => {
                  bookFlightTicket(activeFlightModal);
                  setActiveFlightModal(null);
                }}
                className="flex-1 py-3 bg-blue-600 text-white font-bold rounded-xl shadow-lg hover:bg-blue-700 transition-all"
              >
                Book This Flight ({money(activeFlightModal.price)})
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: HOTEL ROOMS SELECTION */}
      {selectedHotel && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-3xl w-full p-6 sm:p-8 shadow-2xl border border-slate-100 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h3 className="text-xl font-black text-slate-900">{selectedHotel.name}</h3>
                <p className="text-xs text-slate-500 font-bold mt-0.5">{selectedHotel.address}</p>
              </div>
              <button onClick={() => setSelectedHotel(null)} className="p-2 rounded-full hover:bg-slate-100 text-slate-500">
                <X size={20} />
              </button>
            </div>

            <div className="mb-5"><h4 className="text-xs font-extrabold uppercase text-slate-400 tracking-wider mb-2">Live hotel pricing</h4><PriceGraph items={hotelHistory}/></div>
            <h4 className="text-xs font-extrabold uppercase text-slate-400 tracking-wider mb-4">
              Select Your Room Category
            </h4>

            <div className="space-y-4 mb-6">
              {hotelRooms.length === 0 ? (
                <div className="p-4 bg-slate-50 text-slate-500 text-sm rounded-xl">Loading room types...</div>
              ) : (
                hotelRooms.map(room => (
                  <div key={room.id} className="p-4 rounded-2xl border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div className="flex gap-3">
                      {room.imageUrl && <img src={room.imageUrl} alt={room.roomType} className="w-24 h-20 object-cover rounded-xl border border-slate-200"/>}
                      <div>
                      <div className="font-extrabold text-slate-900 text-base">{room.roomType}</div>
                      <div className="text-xs text-slate-500 mt-1">{room.amenities}</div>
                      <div className="text-[11px] text-emerald-600 font-bold mt-1">✓ Free Cancellation available</div>
                      </div>
                    </div>

                    <div className="flex sm:flex-col items-center sm:items-end justify-between gap-2">
                      <div className="text-xl font-black text-slate-900">{money(room.price)}</div>
                      <button
                        onClick={() => saveRoomPreference(room)}
                        className="px-3 py-2 border border-amber-200 bg-amber-50 text-amber-700 text-xs font-bold rounded-xl"
                      >
                        <Heart size={13} className="inline mr-1"/>Save
                      </button>
                      <button
                        onClick={() => bookHotelRoom(selectedHotel, room)}
                        className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-extrabold rounded-xl shadow-md transition-all"
                      >
                        Reserve Room
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* MODAL 3: HOTEL REVIEWS & REPLIES */}
      {reviewHotel && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl border border-slate-100 max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h3 className="text-xl font-black text-slate-900">Verified Reviews & Ratings</h3>
                <p className="text-xs text-slate-500 font-bold mt-0.5">{reviewHotel.name}</p>
              </div>
              <button onClick={() => setReviewHotel(null)} className="p-2 rounded-full hover:bg-slate-100 text-slate-500">
                <X size={20} />
              </button>
            </div>

            {/* Post Review Form */}
            <div className="flex items-center justify-between mb-4">
              <h4 className="text-xs font-extrabold uppercase text-slate-400">Traveler feedback</h4>
              <select value={reviewSort} onChange={async e=>{const v=e.target.value;setReviewSort(v);setHotelReviews(await api(`/reviews?entityType=HOTEL&entityId=${reviewHotel.id}&sort=${v}`));}} className="text-xs font-bold border rounded-lg p-2">
                <option value="newest">Newest</option><option value="highest">Highest rated</option><option value="helpful">Most helpful</option>
              </select>
            </div>
            <form onSubmit={submitReview} className="bg-slate-50 p-4 rounded-2xl border border-slate-200 mb-6">
              <h4 className="text-xs font-bold text-slate-700 mb-2">Write a Guest Review</h4>
              <div className="flex items-center gap-2 mb-3">
                <span className="text-xs text-slate-500 font-medium">Rating:</span>
                <select
                  value={newReviewRating}
                  onChange={(e) => setNewReviewRating(Number(e.target.value))}
                  className="text-xs font-bold border border-slate-300 rounded-lg p-1 bg-white"
                >
                  <option value={5}>5 - Excellent ★★★★★</option>
                  <option value={4}>4 - Very Good ★★★★</option>
                  <option value={3}>3 - Average ★★★</option>
                  <option value={2}>2 - Poor ★★</option>
                  <option value={1}>1 - Terrible ★</option>
                </select>
              </div>
              <input type="url" value={reviewPhoto} onChange={e=>setReviewPhoto(e.target.value)} placeholder="Photo URL (optional)" className="w-full text-xs p-3 mt-2 rounded-xl border border-slate-200 bg-white"/>
              <label className="mt-2 flex items-center gap-2 text-[11px] font-bold text-slate-500 cursor-pointer"><Gift size={13}/> Upload review photo <input type="file" accept="image/*" className="hidden" onChange={e=>{const file=e.target.files?.[0]; if(file){if(file.size>900000)return showToast('Choose an image under 900 KB.'); const rd=new FileReader(); rd.onload=()=>setReviewPhoto(rd.result); rd.readAsDataURL(file);}}}/></label>
              <textarea
                required
                rows={2}
                placeholder="Share your stay experience with other travelers..."
                value={newReviewText}
                onChange={(e) => setNewReviewText(e.target.value)}
                className="w-full text-xs p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 bg-white"
              />
              <button
                type="submit"
                className="mt-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl transition-all"
              >
                Post Review
              </button>
            </form>

            {/* Reviews List */}
            <div className="space-y-4">
              {hotelReviews.length === 0 ? (
                <div className="text-xs text-slate-400 italic">No reviews yet for this hotel. Be the first to review!</div>
              ) : (
                hotelReviews.map(r => (
                  <div key={r.id} className="p-4 rounded-2xl border border-slate-100 bg-white shadow-sm">
                    <div className="flex justify-between items-center mb-1">
                      <div className="font-extrabold text-xs text-slate-800">{r.userName || 'Guest Traveler'}</div>
                      <div className="flex items-center text-amber-500 text-xs font-black">
                        <Star size={12} className="fill-amber-500 mr-1" /> {r.rating}/5
                      </div>
                    </div>
                    <p className="text-xs text-slate-600 mb-2">{r.text}</p>
                    {r.photoUrl && <img src={r.photoUrl} alt="Traveler review" className="w-28 h-20 object-cover rounded-xl border border-slate-200 mb-2" />}
                    <div className="flex gap-2 mb-2">
                      <button onClick={()=>api(`/reviews/${r.id}/helpful`,{method:'POST'}).then(()=>openReviewsModal(reviewHotel))} className="text-[10px] font-bold px-2 py-1 rounded-lg bg-emerald-50 text-emerald-700"><ThumbsUp size={11} className="inline mr-1"/>Helpful {r.helpfulCount||0}</button>
                      <button onClick={()=>api(`/reviews/${r.id}/flag`,{method:'POST'}).then(()=>showToast('Review flagged for moderator review.'))} className="text-[10px] font-bold px-2 py-1 rounded-lg bg-rose-50 text-rose-700"><Flag size={11} className="inline mr-1"/>Flag</button>
                    </div>

                    {/* Replies */}
                    {r.replies && r.replies.length > 0 && (
                      <div className="ml-4 pl-3 border-l-2 border-slate-200 space-y-2 mt-2">
                        {r.replies.map(rep => (
                          <div key={rep.id} className="text-[11px] bg-slate-50 p-2 rounded-lg">
                            <span className="font-bold text-slate-700">{rep.userName || "Traveler"}: </span>
                            <span className="text-slate-600">{rep.text}</span>
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Reply input */}
                    <div className="mt-2 flex gap-2">
                      <input
                        type="text"
                        placeholder="Reply to this review..."
                        value={replyInput[r.id] || ''}
                        onChange={(e) => setReplyInput({ ...replyInput, [r.id]: e.target.value })}
                        className="text-[11px] px-3 py-1 rounded-lg border border-slate-200 flex-1 focus:outline-none"
                      />
                      <button
                        onClick={() => submitReply(r.id)}
                        className="px-3 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 text-[11px] font-bold rounded-lg"
                      >
                        Reply
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* MODAL: DYNAMIC FLIGHT SEAT MAP */}
      {seatFlight && seatMap && (
        <div className="fixed inset-0 z-50 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-xl w-full p-6 shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-5"><div><h3 className="text-xl font-black">Choose your seat</h3><p className="text-xs text-slate-500">{seatFlight.airline} {seatFlight.flightNumber} • {seatFlight.sourceCity} → {seatFlight.destinationCity}</p></div><button onClick={()=>setSeatFlight(null)}><X/></button></div>
            <div className="flex items-center justify-center gap-5 text-[10px] font-bold mb-4"><span>🟩 Available</span><span>🟦 Selected</span><span>🟥 Occupied</span></div>
            <div className="bg-slate-50 rounded-3xl p-5 border">
              <div className="text-center text-xs font-black text-slate-400 mb-4">COCKPIT</div>
              <div className="grid grid-cols-6 gap-2 max-w-sm mx-auto">
                {seatMap.seats.map(seat=>{
                  const occupied=seatMap.occupied?.includes(seat);
                  const selected=selectedSeat===seat;
                  const premium=seat.startsWith('1')||seat.startsWith('2'); return <button key={seat} disabled={occupied} onClick={()=>setSelectedSeat(seat)} title={premium?'Premium seat +₹799':'Standard seat'} className={`h-10 rounded-lg text-xs font-black border ${occupied?'bg-rose-100 text-rose-400 border-rose-200 cursor-not-allowed':selected?'bg-blue-600 text-white border-blue-600':premium?'bg-amber-50 text-amber-700 border-amber-200 hover:bg-amber-100':'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'}`}>{seat}{premium?' ★':''}</button>
                })}
              </div>
            </div>
            <div className="mt-5 flex items-center justify-between"><div><div className="text-[10px] uppercase font-bold text-slate-400">Selected</div><div className="text-xl font-black">{selectedSeat || 'None'}</div></div><button disabled={!selectedSeat} onClick={bookSelectedSeat} className="px-6 py-3 rounded-xl bg-blue-600 text-white font-black disabled:opacity-40">Book {selectedSeat || 'Seat'} • {money(seatFlight.price + ((selectedSeat.startsWith('1')||selectedSeat.startsWith('2'))?799:0))}</button></div>
          </div>
        </div>
      )}

      {/* MODAL: REAL PAYMENT + UPI QR */}
      {paymentModal && (
        <div className="fixed inset-0 z-[60] bg-slate-950/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl">
            <div className="flex justify-between items-start mb-5"><div><h3 className="text-xl font-black">Complete secure payment</h3><p className="text-xs text-slate-500 mt-1">Booking #{paymentModal.booking.id} • {money(paymentModal.booking.totalAmount)}</p></div><button onClick={()=>{setPaymentModal(null);setUpiQr(null)}}><X/></button></div>
            <div className="grid grid-cols-2 gap-3 mb-5">
              <button onClick={()=>startPayment(paymentModal.booking)} className="p-4 rounded-2xl bg-blue-600 text-white font-black text-sm">Razorpay Checkout<br/><span className="text-[10px] font-semibold opacity-80">Cards • UPI • NetBanking • Wallets</span></button>
              {paymentProviders.upiQr ? <button onClick={()=>loadUpiQr(paymentModal.booking)} className="p-4 rounded-2xl border border-slate-200 font-black text-sm">UPI QR<br/><span className="text-[10px] text-slate-500">Scan with any UPI app</span></button> : <div className="p-4 rounded-2xl bg-slate-50 text-xs text-slate-500">UPI QR is available after PAYMENT_UPI_VPA is configured.</div>}
            </div>
            {upiQr && <div className="text-center border-t pt-5"><img src={upiQr.qrUrl} alt="UPI payment QR" className="w-64 h-64 mx-auto rounded-xl border"/><p className="font-black mt-3">Scan & pay {money(upiQr.amount)}</p><a href={upiQr.upiUri} className="inline-block mt-2 px-4 py-2 rounded-xl bg-emerald-600 text-white text-xs font-black">Open UPI App</a><p className="text-[10px] text-slate-400 mt-2">QR payment is a real UPI intent. For automatic booking confirmation and refund reconciliation, use Razorpay Checkout.</p></div>}
          </div>
        </div>
      )}

      {/* MODAL: CANCELLATION + REFUND POLICY */}
      {cancelModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl">
            <div className="flex justify-between items-center mb-5"><div><h3 className="text-xl font-black">Cancel booking</h3><p className="text-xs text-slate-500">Refund is calculated automatically from the policy.</p></div><button onClick={()=>setCancelModal(null)}><X/></button></div>
            <label className="text-[10px] uppercase font-bold text-slate-400">Cancellation reason</label>
            <select value={cancelReason} onChange={e=>setCancelReason(e.target.value)} className="w-full mt-2 p-3 border rounded-xl font-bold">{cancelReasons.map(x=><option key={x}>{x}</option>)}</select>
            <div className="mt-4 p-4 rounded-2xl bg-blue-50 border border-blue-200 text-xs text-blue-800"><b>Policy:</b> cancellation within 24 hours of reservation → 50% refund. Later cancellations → 20% refund. Refund starts as <b>PENDING</b> and expected within 3 days.</div>
            <button onClick={cancelBooking} className="w-full mt-5 py-3 rounded-xl bg-rose-600 text-white font-black">Confirm Cancellation & Refund</button>
          </div>
        </div>
      )}

      {/* MODAL 4: AUTH (LOGIN / REGISTER) */}
      {showAuthModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-8 shadow-2xl border border-slate-100 relative">
            <button
              onClick={() => setShowAuthModal(false)}
              className="absolute top-6 right-6 p-2 rounded-full hover:bg-slate-100 text-slate-500"
            >
              <X size={20} />
            </button>

            <div className="text-center mb-6">
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 text-white font-black text-2xl flex items-center justify-center mx-auto mb-3">
                M
              </div>
              <h3 className="text-2xl font-black text-slate-900">
                {authMode === 'signup' ? 'Create Account' : authMode === 'forgot' ? 'Reset Password' : 'Welcome Back'}
              </h3>
              <p className="text-xs text-slate-500 font-semibold mt-1">
                {authMode === 'signup' ? 'Verify your email with OTP to create your account' : authMode === 'forgot' ? 'We will send a secure OTP to your registered email' : 'Log in with password + email OTP'}
              </p>
            </div>

            {authErr && (
              <div className="mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-bold flex items-center gap-2">
                <AlertCircle size={16} />
                {authErr}
              </div>
            )}

            <form onSubmit={handleAuth} className="space-y-4">
              {authStep === 'form' && authMode === 'signup' && (
                <div>
                  <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Full Name</label>
                  <input
                    type="text"
                    required
                    value={authForm.name}
                    onChange={(e) => setAuthForm({ ...authForm, name: e.target.value })}
                    placeholder="Shivam Pandey"
                    className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"
                  />
                </div>
              )}

              <div>
                <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Email Address</label>
                <input
                  type="email"
                  required
                  value={authForm.email}
                  onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })}
                  placeholder="traveler@example.com"
                  className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"
                />
              </div>

              {authStep === 'form' && authMode !== 'forgot' && <div>
                <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Password</label>
                <input type="password" required value={authForm.password} onChange={(e)=>setAuthForm({...authForm,password:e.target.value})} placeholder="••••••••" className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"/>
              </div>}
              {authStep === 'form' && authMode === 'forgot' && <div>
                <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Registered Email</label>
                <input type="email" required value={authForm.email} onChange={(e)=>setAuthForm({...authForm,email:e.target.value})} className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"/>
              </div>}
              {authStep === 'otp' && <div className="space-y-3">
                <div className="p-4 rounded-2xl bg-blue-50 border border-blue-200 text-blue-800 text-xs font-bold">OTP sent to {authForm.email}. It expires in 10 minutes.</div>
                {devOtp && <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs font-bold">SMTP not configured — development OTP: {devOtp}</div>}
                <input inputMode="numeric" maxLength={6} required value={authOtp} onChange={(e)=>setAuthOtp(e.target.value.replace(/\D/g,'').slice(0,6))} placeholder="Enter 6-digit OTP" className="w-full text-center tracking-[0.4em] text-lg font-black p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"/>
                {authMode === 'forgot' && <input type="password" required value={authForm.newPassword} onChange={(e)=>setAuthForm({...authForm,newPassword:e.target.value})} placeholder="New password" className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"/>}
              </div>}

              {authStep === 'form' && authMode === 'signup' && (
                <div>
                  <label className="text-[10px] font-bold uppercase text-slate-400 block mb-1">Favorite Destination</label>
                  <input
                    type="text"
                    value={authForm.favoriteDestination}
                    onChange={(e) => setAuthForm({ ...authForm, favoriteDestination: e.target.value })}
                    placeholder="Goa, Dubai, Kashmir..."
                    className="w-full text-sm font-semibold p-3 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-600"
                  />
                </div>
              )}

              <button
                type="submit"
                className="w-full py-3.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-extrabold text-sm rounded-xl shadow-lg shadow-blue-500/25 transition-all mt-2"
              >
                {authStep === 'otp' ? (authMode === 'forgot' ? 'Reset Password' : 'Verify OTP & Continue') : (authMode === 'signup' ? 'Send Signup OTP' : authMode === 'forgot' ? 'Send Reset OTP' : 'Send Login OTP')}
              </button>
            </form>

            <div className="text-center mt-6 space-y-2">
              {authMode === 'login' && authStep === 'form' && <button type="button" onClick={()=>{setAuthMode('forgot');setAuthStep('form');setAuthErr('')}} className="text-xs font-bold text-rose-600 hover:underline">Forgot Password?</button>}
              <div><button type="button" onClick={()=>{setAuthMode(authMode==='login'?'signup':'login');setAuthStep('form');setAuthErr('');setAuthOtp('')}} className="text-xs font-bold text-blue-600 hover:underline">{authMode==='login'?"Don't have an account? Sign Up":'Already have an account? Log In'}</button></div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
