package com.mmt.service;

import com.mmt.model.*;
import com.mmt.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    private final FlightRepository flightRepo;
    private final HotelRepository hotelRepo;
    private final HotelRoomRepository roomRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;
    private final UtilService util;

    public DataSeeder(FlightRepository flightRepo, HotelRepository hotelRepo,
                      HotelRoomRepository roomRepo, UserRepository userRepo,
                      ReviewRepository reviewRepo, UtilService util) {
        this.flightRepo = flightRepo;
        this.hotelRepo = hotelRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.util = util;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedFlights();
        seedHotelsAndRooms();
    }

    private void seedUsers() {
        if (userRepo.count() == 0) {
            User demo = User.builder()
                    .name("Demo Traveler")
                    .email("demo@mmt.com")
                    .passwordHash(util.hash("demo123"))
                    .preferences("beach,luxury,mountains,nightlife,heritage")
                    .favoriteDestination("Goa")
                    .createdAt(LocalDateTime.now().minusMonths(1))
                    .build();

            User admin = User.builder()
                    .name("Shivam Admin")
                    .email("shivam@mmt.com")
                    .passwordHash(util.hash("password123"))
                    .preferences("tech,flights,resorts,business")
                    .favoriteDestination("Dubai")
                    .createdAt(LocalDateTime.now().minusDays(15))
                    .build();

            userRepo.saveAll(Arrays.asList(demo, admin));
        }
    }

    private void seedFlights() {
        if (flightRepo.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            List<Flight> flightList = Arrays.asList(
                // Delhi <-> Mumbai
                Flight.builder().airline("IndiGo").flightNumber("6E-2041").sourceCity("Delhi").destinationCity("Mumbai")
                        .departureTime(now.plusHours(2)).arrivalTime(now.plusHours(4).plusMinutes(15))
                        .estimatedDepartureTime(now.plusHours(2)).estimatedArrivalTime(now.plusHours(4).plusMinutes(15))
                        .basePrice(4850.0).price(4850.0).totalSeats(180).availableSeats(42).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-805").sourceCity("Delhi").destinationCity("Mumbai")
                        .departureTime(now.plusHours(4)).arrivalTime(now.plusHours(6).plusMinutes(20))
                        .estimatedDepartureTime(now.plusHours(4)).estimatedArrivalTime(now.plusHours(6).plusMinutes(20))
                        .basePrice(5400.0).price(5400.0).totalSeats(174).availableSeats(19).status("BOARDING").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Vistara").flightNumber("UK-953").sourceCity("Delhi").destinationCity("Mumbai")
                        .departureTime(now.plusHours(6).plusMinutes(30)).arrivalTime(now.plusHours(8).plusMinutes(45))
                        .estimatedDepartureTime(now.plusHours(6).plusMinutes(30)).estimatedArrivalTime(now.plusHours(8).plusMinutes(45))
                        .basePrice(6200.0).price(6200.0).totalSeats(164).availableSeats(60).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Akasa Air").flightNumber("QP-1120").sourceCity("Mumbai").destinationCity("Delhi")
                        .departureTime(now.plusHours(3)).arrivalTime(now.plusHours(5).plusMinutes(10))
                        .estimatedDepartureTime(now.plusHours(3)).estimatedArrivalTime(now.plusHours(5).plusMinutes(10))
                        .basePrice(4499.0).price(4499.0).totalSeats(189).availableSeats(55).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("SpiceJet").flightNumber("SG-8169").sourceCity("Mumbai").destinationCity("Delhi")
                        .departureTime(now.plusHours(5)).arrivalTime(now.plusHours(7).plusMinutes(25))
                        .estimatedDepartureTime(now.plusHours(5).plusMinutes(35)).estimatedArrivalTime(now.plusHours(8).plusMinutes(0))
                        .basePrice(4100.0).price(4100.0).totalSeats(189).availableSeats(12).status("DELAYED").delayMinutes(35).lastUpdated(now).build(),

                // Delhi <-> Goa
                Flight.builder().airline("IndiGo").flightNumber("6E-6324").sourceCity("Delhi").destinationCity("Goa")
                        .departureTime(now.plusHours(3).plusMinutes(15)).arrivalTime(now.plusHours(5).plusMinutes(50))
                        .estimatedDepartureTime(now.plusHours(3).plusMinutes(15)).estimatedArrivalTime(now.plusHours(5).plusMinutes(50))
                        .basePrice(5999.0).price(5999.0).totalSeats(186).availableSeats(28).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Vistara").flightNumber("UK-849").sourceCity("Delhi").destinationCity("Goa")
                        .departureTime(now.plusHours(7)).arrivalTime(now.plusHours(9).plusMinutes(35))
                        .estimatedDepartureTime(now.plusHours(7)).estimatedArrivalTime(now.plusHours(9).plusMinutes(35))
                        .basePrice(6850.0).price(6850.0).totalSeats(158).availableSeats(44).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India Express").flightNumber("IX-154").sourceCity("Goa").destinationCity("Delhi")
                        .departureTime(now.plusHours(4).plusMinutes(45)).arrivalTime(now.plusHours(7).plusMinutes(15))
                        .estimatedDepartureTime(now.plusHours(4).plusMinutes(45)).estimatedArrivalTime(now.plusHours(7).plusMinutes(15))
                        .basePrice(5200.0).price(5200.0).totalSeats(180).availableSeats(35).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // Delhi <-> Bengaluru
                Flight.builder().airline("IndiGo").flightNumber("6E-5032").sourceCity("Delhi").destinationCity("Bengaluru")
                        .departureTime(now.plusHours(2).plusMinutes(30)).arrivalTime(now.plusHours(5).plusMinutes(15))
                        .estimatedDepartureTime(now.plusHours(2).plusMinutes(30)).estimatedArrivalTime(now.plusHours(5).plusMinutes(15))
                        .basePrice(5650.0).price(5650.0).totalSeats(186).availableSeats(65).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-506").sourceCity("Delhi").destinationCity("Bengaluru")
                        .departureTime(now.plusHours(5).plusMinutes(15)).arrivalTime(now.plusHours(8).plusMinutes(0))
                        .estimatedDepartureTime(now.plusHours(5).plusMinutes(15)).estimatedArrivalTime(now.plusHours(8).plusMinutes(0))
                        .basePrice(6100.0).price(6100.0).totalSeats(170).availableSeats(30).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Akasa Air").flightNumber("QP-1351").sourceCity("Bengaluru").destinationCity("Delhi")
                        .departureTime(now.plusHours(4)).arrivalTime(now.plusHours(6).plusMinutes(50))
                        .estimatedDepartureTime(now.plusHours(4)).estimatedArrivalTime(now.plusHours(6).plusMinutes(50))
                        .basePrice(5199.0).price(5199.0).totalSeats(189).availableSeats(72).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // Mumbai <-> Bengaluru
                Flight.builder().airline("IndiGo").flightNumber("6E-451").sourceCity("Mumbai").destinationCity("Bengaluru")
                        .departureTime(now.plusHours(1).plusMinutes(45)).arrivalTime(now.plusHours(3).plusMinutes(30))
                        .estimatedDepartureTime(now.plusHours(1).plusMinutes(45)).estimatedArrivalTime(now.plusHours(3).plusMinutes(30))
                        .basePrice(3950.0).price(3950.0).totalSeats(180).availableSeats(48).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Vistara").flightNumber("UK-860").sourceCity("Mumbai").destinationCity("Bengaluru")
                        .departureTime(now.plusHours(6)).arrivalTime(now.plusHours(7).plusMinutes(45))
                        .estimatedDepartureTime(now.plusHours(6)).estimatedArrivalTime(now.plusHours(7).plusMinutes(45))
                        .basePrice(4700.0).price(4700.0).totalSeats(164).availableSeats(22).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("SpiceJet").flightNumber("SG-3012").sourceCity("Bengaluru").destinationCity("Mumbai")
                        .departureTime(now.plusHours(3).plusMinutes(20)).arrivalTime(now.plusHours(5).plusMinutes(10))
                        .estimatedDepartureTime(now.plusHours(3).plusMinutes(20)).estimatedArrivalTime(now.plusHours(5).plusMinutes(10))
                        .basePrice(3800.0).price(3800.0).totalSeats(189).availableSeats(50).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // Delhi <-> Srinagar (Kashmir)
                Flight.builder().airline("IndiGo").flightNumber("6E-212").sourceCity("Delhi").destinationCity("Srinagar")
                        .departureTime(now.plusHours(3)).arrivalTime(now.plusHours(4).plusMinutes(35))
                        .estimatedDepartureTime(now.plusHours(3)).estimatedArrivalTime(now.plusHours(4).plusMinutes(35))
                        .basePrice(6150.0).price(6150.0).totalSeats(180).availableSeats(31).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("SpiceJet").flightNumber("SG-8472").sourceCity("Delhi").destinationCity("Srinagar")
                        .departureTime(now.plusHours(5).plusMinutes(30)).arrivalTime(now.plusHours(7).plusMinutes(5))
                        .estimatedDepartureTime(now.plusHours(5).plusMinutes(50)).estimatedArrivalTime(now.plusHours(7).plusMinutes(25))
                        .basePrice(5750.0).price(5750.0).totalSeats(189).availableSeats(15).status("DELAYED").delayMinutes(20).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-825").sourceCity("Srinagar").destinationCity("Delhi")
                        .departureTime(now.plusHours(6)).arrivalTime(now.plusHours(7).plusMinutes(30))
                        .estimatedDepartureTime(now.plusHours(6)).estimatedArrivalTime(now.plusHours(7).plusMinutes(30))
                        .basePrice(6300.0).price(6300.0).totalSeats(170).availableSeats(40).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // Delhi / Mumbai <-> Jaipur
                Flight.builder().airline("IndiGo").flightNumber("6E-7201").sourceCity("Delhi").destinationCity("Jaipur")
                        .departureTime(now.plusHours(2)).arrivalTime(now.plusHours(3).plusMinutes(0))
                        .estimatedDepartureTime(now.plusHours(2)).estimatedArrivalTime(now.plusHours(3).plusMinutes(0))
                        .basePrice(2950.0).price(2950.0).totalSeats(78).availableSeats(20).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-491").sourceCity("Mumbai").destinationCity("Jaipur")
                        .departureTime(now.plusHours(4).plusMinutes(30)).arrivalTime(now.plusHours(6).plusMinutes(15))
                        .estimatedDepartureTime(now.plusHours(4).plusMinutes(30)).estimatedArrivalTime(now.plusHours(6).plusMinutes(15))
                        .basePrice(4600.0).price(4600.0).totalSeats(170).availableSeats(52).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // Hyderabad / Kolkata / Chennai
                Flight.builder().airline("IndiGo").flightNumber("6E-3402").sourceCity("Hyderabad").destinationCity("Goa")
                        .departureTime(now.plusHours(2).plusMinutes(15)).arrivalTime(now.plusHours(3).plusMinutes(40))
                        .estimatedDepartureTime(now.plusHours(2).plusMinutes(15)).estimatedArrivalTime(now.plusHours(3).plusMinutes(40))
                        .basePrice(4200.0).price(4200.0).totalSeats(180).availableSeats(36).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-772").sourceCity("Kolkata").destinationCity("Delhi")
                        .departureTime(now.plusHours(3).plusMinutes(45)).arrivalTime(now.plusHours(6).plusMinutes(15))
                        .estimatedDepartureTime(now.plusHours(3).plusMinutes(45)).estimatedArrivalTime(now.plusHours(6).plusMinutes(15))
                        .basePrice(5450.0).price(5450.0).totalSeats(174).availableSeats(29).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("IndiGo").flightNumber("6E-883").sourceCity("Chennai").destinationCity("Mumbai")
                        .departureTime(now.plusHours(5)).arrivalTime(now.plusHours(7).plusMinutes(0))
                        .estimatedDepartureTime(now.plusHours(5)).estimatedArrivalTime(now.plusHours(7).plusMinutes(0))
                        .basePrice(4300.0).price(4300.0).totalSeats(186).availableSeats(60).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),

                // International: Delhi / Mumbai <-> Dubai & Singapore & London
                Flight.builder().airline("Emirates").flightNumber("EK-513").sourceCity("Delhi").destinationCity("Dubai")
                        .departureTime(now.plusHours(6)).arrivalTime(now.plusHours(9).plusMinutes(45))
                        .estimatedDepartureTime(now.plusHours(6)).estimatedArrivalTime(now.plusHours(9).plusMinutes(45))
                        .basePrice(19800.0).price(19800.0).totalSeats(354).availableSeats(85).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Air India").flightNumber("AI-995").sourceCity("Mumbai").destinationCity("Dubai")
                        .departureTime(now.plusHours(7).plusMinutes(30)).arrivalTime(now.plusHours(10).plusMinutes(45))
                        .estimatedDepartureTime(now.plusHours(7).plusMinutes(30)).estimatedArrivalTime(now.plusHours(10).plusMinutes(45))
                        .basePrice(16500.0).price(16500.0).totalSeats(256).availableSeats(42).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("Singapore Airlines").flightNumber("SQ-403").sourceCity("Delhi").destinationCity("Singapore")
                        .departureTime(now.plusHours(8)).arrivalTime(now.plusHours(16).plusMinutes(20))
                        .estimatedDepartureTime(now.plusHours(8)).estimatedArrivalTime(now.plusHours(16).plusMinutes(20))
                        .basePrice(28900.0).price(28900.0).totalSeats(303).availableSeats(50).status("ON TIME").delayMinutes(0).lastUpdated(now).build(),
                Flight.builder().airline("British Airways").flightNumber("BA-142").sourceCity("Delhi").destinationCity("London")
                        .departureTime(now.plusHours(10)).arrivalTime(now.plusHours(19).plusMinutes(50))
                        .estimatedDepartureTime(now.plusHours(10)).estimatedArrivalTime(now.plusHours(19).plusMinutes(50))
                        .basePrice(54900.0).price(54900.0).totalSeats(272).availableSeats(38).status("ON TIME").delayMinutes(0).lastUpdated(now).build()
            );

            flightRepo.saveAll(flightList);
        }
    }

    private void seedHotelsAndRooms() {
        if (hotelRepo.count() == 0) {
            // Goa Hotels
            Hotel h1 = hotelRepo.save(Hotel.builder()
                    .name("Taj Exotica Resort & Spa, Goa")
                    .city("Goa")
                    .address("Calwaddo, Benaulim, Salcete, South Goa")
                    .rating(4.9)
                    .reviewCount(1420)
                    .basePrice(16500.0)
                    .amenities("Beachfront,Infinity Pool,Luxury Spa,Fine Dining,Free WiFi,Valet Parking,Kids Club")
                    .imageUrl("https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&auto=format&fit=crop&q=80")
                    .description("Mediterranean-inspired luxury resort set in 56 acres of lush gardens along a pristine private beach.")
                    .build());
            createRooms(h1, "Deluxe Sea Facing Room", 16500.0, 10, "Executive Villa with Plunge Pool", 32000.0, 4);

            Hotel h2 = hotelRepo.save(Hotel.builder()
                    .name("W Goa Luxury Beachfront Resort")
                    .city("Goa")
                    .address("Vagator Beach, Bardez, North Goa")
                    .rating(4.8)
                    .reviewCount(980)
                    .basePrice(14200.0)
                    .amenities("Rock Pool,Sunset Bar,Spa,Gym,DJ Events,Pet Friendly,Ocean View")
                    .imageUrl("https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop&q=80")
                    .description("Vibrant beachfront resort with panoramic views of Chapora Fort and Arabian Sea.")
                    .build());
            createRooms(h2, "Wonderful King Room", 14200.0, 12, "Fabulous Marvelous Suite", 26500.0, 5);

            Hotel h3 = hotelRepo.save(Hotel.builder()
                    .name("Alila Diwa Goa - Hyatt")
                    .city("Goa")
                    .address("48/10 Adao Waddo, Majorda, Goa")
                    .rating(4.7)
                    .reviewCount(1150)
                    .basePrice(9800.0)
                    .amenities("Paddy View,Infinity Pool,Spa,Boutique Cinema,Kids Play Area,Free Breakfast")
                    .imageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop&q=80")
                    .description("Surrounded by lush paddy fields with serene infinity pools blending into coastal Goan charm.")
                    .build());
            createRooms(h3, "Terrace King Room", 9800.0, 15, "Diwa Club Luxury Suite", 18500.0, 6);

            // Mumbai Hotels
            Hotel h4 = hotelRepo.save(Hotel.builder()
                    .name("The Taj Mahal Palace, Mumbai")
                    .city("Mumbai")
                    .address("Apollo Bunder, Colaba, Mumbai")
                    .rating(4.9)
                    .reviewCount(3890)
                    .basePrice(18500.0)
                    .amenities("Gateway of India View,Historic Suites,9 Award Winning Restaurants,Luxury Spa,Butler Service")
                    .imageUrl("https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&auto=format&fit=crop&q=80")
                    .description("An iconic landmark blending royal elegance with modern luxury since 1903.")
                    .build());
            createRooms(h4, "Luxury Heritage Room", 18500.0, 14, "Grand Luxury Sea Suite", 45000.0, 3);

            Hotel h5 = hotelRepo.save(Hotel.builder()
                    .name("The St. Regis Mumbai")
                    .city("Mumbai")
                    .address("462 Senapati Bapat Marg, Lower Parel, Mumbai")
                    .rating(4.8)
                    .reviewCount(2140)
                    .basePrice(12900.0)
                    .amenities("Rooftop Lounge,High-rise Skyline View,St. Regis Butler,Fine Dining,Spa & Pool")
                    .imageUrl("https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&auto=format&fit=crop&q=80")
                    .description("Highest 5-star hotel in Mumbai situated in the bustling heart of Lower Parel.")
                    .build());
            createRooms(h5, "Deluxe King City View", 12900.0, 18, "St. Regis Signature Suite", 28000.0, 6);

            // Delhi & NCR Hotels
            Hotel h6 = hotelRepo.save(Hotel.builder()
                    .name("The Leela Palace New Delhi")
                    .city("Delhi")
                    .address("Diplomatic Enclave, Chanakyapuri, New Delhi")
                    .rating(4.9)
                    .reviewCount(2640)
                    .basePrice(17500.0)
                    .amenities("Temperature Rooftop Pool,Royal Architecture,Megu Japanese Restaurant,Butler Service,Spa")
                    .imageUrl("https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800&auto=format&fit=crop&q=80")
                    .description("A breathtaking architectural marvel inspired by Lutyens design and royal Indian palaces.")
                    .build());
            createRooms(h6, "Grande Deluxe King", 17500.0, 16, "Royal Suite with Butler", 42000.0, 4);

            Hotel h7 = hotelRepo.save(Hotel.builder()
                    .name("The Oberoi, New Delhi")
                    .city("Delhi")
                    .address("Dr. Zakir Hussain Marg, Golf Links, New Delhi")
                    .rating(4.9)
                    .reviewCount(1890)
                    .basePrice(16900.0)
                    .amenities("Delhi Golf Course View,Air Purification Tech,Rooftop Bar Cirrus9,Oberoi Spa")
                    .imageUrl("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&auto=format&fit=crop&q=80")
                    .description("Centrally located overlooking the Delhi Golf Club and Humayun's Tomb with clean air guarantee.")
                    .build());
            createRooms(h7, "Premier Plus Room", 16900.0, 20, "Luxury Golf View Suite", 36000.0, 5);

            // Jaipur Hotels
            Hotel h8 = hotelRepo.save(Hotel.builder()
                    .name("Rambagh Palace - Taj Heritage")
                    .city("Jaipur")
                    .address("Bhawani Singh Road, Jaipur, Rajasthan")
                    .rating(5.0)
                    .reviewCount(3120)
                    .basePrice(26000.0)
                    .amenities("Former Royal Residence,Peacock Gardens,Polo Bar,Jiva Grande Spa,Vintage Car Escort")
                    .imageUrl("https://images.unsplash.com/photo-1585543805890-6051f7829f98?w=800&auto=format&fit=crop&q=80")
                    .description("The 'Jewel of Jaipur', pristine former residence of the Maharaja of Jaipur.")
                    .build());
            createRooms(h8, "Palace Room", 26000.0, 8, "Maharani Royal Suite", 75000.0, 2);

            Hotel h9 = hotelRepo.save(Hotel.builder()
                    .name("ITC Rajputana, Luxury Collection")
                    .city("Jaipur")
                    .address("Palace Road, Gopalbari, Jaipur")
                    .rating(4.7)
                    .reviewCount(1780)
                    .basePrice(8500.0)
                    .amenities("Peshawri Cuisine,Traditional Haveli Courtyards,Kaya Kalp Spa,Outdoor Pool")
                    .imageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&auto=format&fit=crop&q=80")
                    .description("Echoing the architectural brilliance of Rajasthani Havelis with royal hospitality.")
                    .build());
            createRooms(h9, "Executive Rajputana Room", 8500.0, 25, "Thikana Heritage Suite", 19000.0, 6);

            // Bengaluru Hotels
            Hotel h10 = hotelRepo.save(Hotel.builder()
                    .name("The Ritz-Carlton, Bangalore")
                    .city("Bengaluru")
                    .address("99 Residency Road, Shanthala Nagar, Bengaluru")
                    .rating(4.8)
                    .reviewCount(1670)
                    .basePrice(11500.0)
                    .amenities("Bang Rooftop Bar,Ritz Carlton Spa,Lantern Chinese Restaurant,Helipad,Heated Pool")
                    .imageUrl("https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800&auto=format&fit=crop&q=80")
                    .description("Contemporary luxury sanctuary with stunning city views in the silicon valley capital.")
                    .build());
            createRooms(h10, "Deluxe Skyline Room", 11500.0, 22, "Ritz-Carlton Executive Suite", 24000.0, 7);

            // Srinagar Hotels (Kashmir)
            Hotel h11 = hotelRepo.save(Hotel.builder()
                    .name("The Lalit Grand Palace Srinagar")
                    .city("Srinagar")
                    .address("Gupkar Road, Dal Lake, Srinagar, Kashmir")
                    .rating(4.8)
                    .reviewCount(1490)
                    .basePrice(19000.0)
                    .amenities("Dal Lake View,Himalayan Chinar Lawns,Re彩 Heated Pool,Traditional Kashmiri Dining")
                    .imageUrl("https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&auto=format&fit=crop&q=80")
                    .description("Built in 1910 by Maharaja Pratap Singh overlooking the serene waters of Dal Lake.")
                    .build());
            createRooms(h11, "Palace Heritage Lake View", 19000.0, 10, "Maharaja Valley Villa", 39000.0, 3);

            // Dubai International Hotel
            Hotel h12 = hotelRepo.save(Hotel.builder()
                    .name("Atlantis, The Palm Dubai")
                    .city("Dubai")
                    .address("Crescent Road, Palm Jumeirah, Dubai, UAE")
                    .rating(4.9)
                    .reviewCount(6400)
                    .basePrice(34000.0)
                    .amenities("Aquaventure Waterpark Access,Underwater Aquarium,Gordon Ramsay Bread Street,Private Beach")
                    .imageUrl("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=800&auto=format&fit=crop&q=80")
                    .description("World-renowned luxury icon on Dubai's iconic Palm Jumeirah island.")
                    .build());
            createRooms(h12, "Ocean King Room", 34000.0, 30, "Underwater Suite", 115000.0, 2);
        }
    }

    private void createRooms(Hotel hotel, String type1, double price1, int avail1, String type2, double price2, int avail2) {
        HotelRoom r1 = HotelRoom.builder()
                .hotelId(hotel.getId())
                .roomType(type1)
                .price(price1)
                .totalRooms(avail1 + 10)
                .availableRooms(avail1)
                .amenities("King Bed,City/Nature View,Free High-speed WiFi,Mini Bar,Bathtub,LED Smart TV")
                .refundable(true)
                .build();

        HotelRoom r2 = HotelRoom.builder()
                .hotelId(hotel.getId())
                .roomType(type2)
                .price(price2)
                .totalRooms(avail2 + 5)
                .availableRooms(avail2)
                .amenities("Master Suite,Panoramic View,Butler Service,Complimentary Lounge Access,Jacuzzi,Breakfast Included")
                .refundable(true)
                .build();

        roomRepo.saveAll(Arrays.asList(r1, r2));
    }
}
