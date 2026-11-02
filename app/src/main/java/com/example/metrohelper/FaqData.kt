package com.example.metrohelper

data class FaqItem(
    val id: Int,
    val question: String,
    val answer: String,
    val category: String
)

object FaqDataProvider {
    val categories = listOf(
        "All",
        "Ticketing",
        "Timings",
        "Luggage & Rules",
        "Helpline",
        "App Features"
    )

    val faqList = listOf(
        FaqItem(
            id = 1,
            question = "What is the minimum balance required on a Metro Smart Card?",
            answer = "The minimum balance required to enter the Delhi Metro network using a Smart Card is ₹50. Ensure your card has at least this amount to avoid automatic gate closure at entry.",
            category = "Ticketing"
        ),
        FaqItem(
            id = 2,
            question = "What discount is available on Metro Smart Cards?",
            answer = "Smart Card users receive a 10% discount on every journey. An additional 10% discount (total 20%) is provided during non-peak hours (entry before 8:00 AM, between 12:00 PM and 5:00 PM, and after 9:00 PM), and all day on Sundays and National Holidays.",
            category = "Ticketing"
        ),
        FaqItem(
            id = 3,
            question = "How do WhatsApp and Mobile QR Tickets work?",
            answer = "You can generate a QR ticket using the DMRC WhatsApp bot (+91 9650855800) or the official DMRC app. Simply present the QR code on your phone screen to the scanner at dedicated QR AFC gates at both entry and exit stations.",
            category = "Ticketing"
        ),
        FaqItem(
            id = 4,
            question = "What are the standard Delhi Metro operational hours?",
            answer = "Metro train services typically operate from 06:00 AM to 11:00 PM on most lines. The Airport Express Line begins earlier at 04:45 AM. Timings may vary slightly on Sundays and festival days.",
            category = "Timings"
        ),
        FaqItem(
            id = 5,
            question = "What is the maximum baggage weight and dimension permitted?",
            answer = "Passengers are allowed to carry one piece of luggage weighing up to 25 kg with maximum dimensions of 80 cm x 50 cm x 30 cm. Bulky items or luggage exceeding these specifications are not permitted through security checks.",
            category = "Luggage & Rules"
        ),
        FaqItem(
            id = 6,
            question = "Are alcohol bottles or pets allowed inside the metro?",
            answer = "Carrying up to two sealed bottles of alcohol per person is permitted on Delhi Metro trains (except on Airport Express Line where rules may differ). Consumption of alcohol, open containers, and pets/animals are strictly forbidden.",
            category = "Luggage & Rules"
        ),
        FaqItem(
            id = 7,
            question = "Where is the DMRC Lost and Found office located?",
            answer = "The central DMRC Lost and Found office is situated at Kashmere Gate Metro Station. If you lose an item, report it at the Station Control Room of your destination station or contact the 24x7 helpline at 155370.",
            category = "Helpline"
        ),
        FaqItem(
            id = 8,
            question = "What are the key emergency contact numbers for Delhi Metro?",
            answer = "• DMRC 24x7 Helpline: 155370\n• Women Helpline: 011-23415440\n• CISF Control Room (Security): 011-25693131\n• Delhi Police Metro Unit: 1511\nInside the train coaches, you can also operate the Passenger Emergency Alarm (PEA) handle to speak to the train operator.",
            category = "Helpline"
        ),
        FaqItem(
            id = 9,
            question = "How does the DMRC Helper Route Planner work?",
            answer = "The Route Planner allows you to select your source and destination stations to view shortest path connections, interchange stations, travel time, and estimated fares across all operational Delhi Metro lines.",
            category = "App Features"
        ),
        FaqItem(
            id = 10,
            question = "How does the Nearest Metro Station Finder locate stations?",
            answer = "The Nearest Station utility uses your device's GPS coordinates to find and sort Delhi Metro stations by aerial distance, displaying the closest stations along with direct navigation options.",
            category = "App Features"
        )
    )
}
