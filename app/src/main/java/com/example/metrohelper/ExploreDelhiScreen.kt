package com.example.metrohelper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Recommendation(
    val id: Int,
    val title: String,
    val category: String,
    val rating: Double,
    val reviewCount: String,
    val transportMode: String,
    val description: String,
    val imageResId: Int
)

data class StationRecommendations(
    val stationName: String,
    val line: String,
    val recommendations: List<Recommendation>
)

val exploreDelhiData = listOf(
    StationRecommendations(
        stationName = "Rajiv Chowk",
        line = "Blue & Yellow Line",
        recommendations = listOf(
            Recommendation(
                id = 101,
                title = "Connaught Place (CP)",
                category = "Heritage & Shopping",
                rating = 4.6,
                reviewCount = "142k",
                transportMode = "🚶 Immediate (Exit Gate 1-8)",
                description = "Iconic colonial Georgian-style colonnaded market with brand stores, street fashion, cafes and lively nightlife.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 102,
                title = "Kake Da Hotel",
                category = "Iconic Food",
                rating = 4.2,
                reviewCount = "24k",
                transportMode = "🚶 4 min walk (Outer Circle, Gate 6)",
                description = "Famous non-veg institution operating since 1931, renowned for dahi meat, butter chicken, and tandoori breads.",
                imageResId = R.drawable.kake_food
            ),
            Recommendation(
                id = 103,
                title = "Pracheen Hanuman Mandir",
                category = "Spiritual",
                rating = 4.7,
                reviewCount = "38k",
                transportMode = "🚶 5 min walk (Baba Kharak Singh Marg)",
                description = "One of the oldest temples in Delhi, believed to date back to the Mahabharata era with continuous Ram chanting.",
                imageResId = R.drawable.prachin_hanuman_mandir
            ),
            Recommendation(
                id = 104,
                title = "Palika Bazaar",
                category = "Underground Market",
                rating = 4.1,
                reviewCount = "32k",
                transportMode = "🚶 2 min walk (Exit Gate 6)",
                description = "Vast air-conditioned underground bazaar known for electronics, street fashion, footwear, and bargaining.",
                imageResId = R.drawable.palika_bazaar
            ),
            Recommendation(
                id = 105,
                title = "Wenger's Deli & Bakery",
                category = "Bakery & Desserts",
                rating = 4.5,
                reviewCount = "19k",
                transportMode = "🚶 3 min walk (A Block, Radial 3)",
                description = "Delhi's legendary 1926 Swiss confectionery celebrated for chicken patties, chocolate éclairs, and shammi kebabs.",
                imageResId = R.drawable.wengers
            )
        )
    ),
    StationRecommendations(
        stationName = "Chandni Chowk",
        line = "Yellow Line",
        recommendations = listOf(
            Recommendation(
                id = 201,
                title = "Red Fort (Lal Qila)",
                category = "UNESCO Heritage",
                rating = 4.5,
                reviewCount = "190k",
                transportMode = "🛺 5 min E-Rickshaw (₹20) or 10 min walk",
                description = "Majestic 17th-century Mughal red sandstone citadel built by Shah Jahan, symbolizing India's sovereignty.",
                imageResId = R.drawable.red_fort
            ),
            Recommendation(
                id = 202,
                title = "Jama Masjid",
                category = "Historic Monument",
                rating = 4.6,
                reviewCount = "85k",
                transportMode = "🛺 6 min E-Rickshaw (₹20) from Gate 1",
                description = "One of India's largest and most majestic mosques with grand courtyards accommodating 25,000 worshippers.",
                imageResId = R.drawable.jama_masjid
            ),
            Recommendation(
                id = 203,
                title = "Paranthe Wali Gali",
                category = "Famous Street Food",
                rating = 4.1,
                reviewCount = "41k",
                transportMode = "🚶 6 min walk inside Old Delhi alleys",
                description = "Centuries-old culinary lane serving traditional deep-fried stuffed parathas with aloo subzi, kaddu, and chutneys.",
                imageResId = R.drawable.prantha_gali
            ),
            Recommendation(
                id = 204,
                title = "Gurudwara Sis Ganj Sahib",
                category = "Spiritual & History",
                rating = 4.8,
                reviewCount = "52k",
                transportMode = "🚶 5 min walk (Chandni Chowk Main Road)",
                description = "Historic Sikh pilgrimage site commemorating the martyrdom of Guru Tegh Bahadur with 24/7 peaceful community langar.",
                imageResId = R.drawable.sis_ganj
            ),
            Recommendation(
                id = 205,
                title = "Natraj Dahi Bhalle Corner",
                category = "Street Food Gem",
                rating = 4.4,
                reviewCount = "16k",
                transportMode = "🚶 1 min walk (Near Metro Gate 5)",
                description = "Established in 1940, famous for its melt-in-mouth Dahi Bhalla and crispy spiced Aloo Tikki.",
                imageResId = R.drawable.natraj
            )
        )
    ),
    StationRecommendations(
        stationName = "Central Secretariat",
        line = "Yellow & Violet Line",
        recommendations = listOf(
            Recommendation(
                id = 301,
                title = "India Gate & Kartavya Path",
                category = "Monument & Promenade",
                rating = 4.7,
                reviewCount = "240k",
                transportMode = "🚶 10 min walk or 🛺 4 min Auto from Gate 3",
                description = "National war memorial triumphal arch standing 42m tall amidst open green boulevards and fountain illuminations.",
                imageResId = R.drawable.india_gate
            ),
            Recommendation(
                id = 302,
                title = "Rashtrapati Bhavan & Amrit Udyan",
                category = "Presidential Heritage",
                rating = 4.7,
                reviewCount = "72k",
                transportMode = "🛺 5 min Auto (₹40) or 15 min walk",
                description = "Official residence of the President of India, stunning classical architecture with grand gardens.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 303,
                title = "National Museum",
                category = "Art & History",
                rating = 4.6,
                reviewCount = "29k",
                transportMode = "🚶 8 min walk (Janpath road)",
                description = "Houses over 200,000 works of art spanning 5,000 years, including treasures of the Indus Valley Civilization.",
                imageResId = R.drawable.hauz_khas_fort
            ),
            Recommendation(
                id = 304,
                title = "National War Memorial",
                category = "Patriotic Monument",
                rating = 4.8,
                reviewCount = "64k",
                transportMode = "🚶 12 min walk or 🛺 5 min Auto",
                description = "State-of-the-art national monument paying homage to armed forces personnel who sacrificed for the country.",
                imageResId = R.drawable.india_gate
            ),
            Recommendation(
                id = 305,
                title = "Andhra Bhavan Canteen",
                category = "Authentic Dining",
                rating = 4.4,
                reviewCount = "38k",
                transportMode = "🚕 6 min Auto/Cab from station",
                description = "Legendary South Indian non-veg feasts, fiery Andhra mutton fry, unlimited vegetarian thalis, and hyderabadi biryani.",
                imageResId = R.drawable.kake_food
            )
        )
    ),
    StationRecommendations(
        stationName = "Hauz Khas",
        line = "Yellow & Magenta Line",
        recommendations = listOf(
            Recommendation(
                id = 401,
                title = "Hauz Khas Fort & Lake",
                category = "Medieval Ruins",
                rating = 4.6,
                reviewCount = "98k",
                transportMode = "🛺 5 min E-Rickshaw (₹20) from Gate 3",
                description = "Picturesque 13th-century Delhi Sultanate madrasa ruins and tombs overlooking the royal water reservoir.",
                imageResId = R.drawable.hauz_khas_fort
            ),
            Recommendation(
                id = 402,
                title = "Hauz Khas Social",
                category = "Cafe & Nightlife",
                rating = 4.4,
                reviewCount = "31k",
                transportMode = "🛺 5 min E-Rickshaw to HKV entry",
                description = "Multi-level bohemian cafe and co-working workspace with panoramic views of the water reservoir.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 403,
                title = "Deer Park",
                category = "Nature & Wildlife",
                rating = 4.5,
                reviewCount = "36k",
                transportMode = "🚶 10 min walk or 🛺 5 min Rickshaw",
                description = "Lush green oasis with roaming spotted deer, peacocks, jogging trails, and Mughal era domes.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 404,
                title = "Mia Bella Romantic Kitchen",
                category = "Rooftop Dining",
                rating = 4.3,
                reviewCount = "12k",
                transportMode = "🛺 6 min Auto to HKV Village",
                description = "Charming European aesthetic rooftop restaurant overlooking the fort lake, known for Italian delicacies.",
                imageResId = R.drawable.kake_food
            ),
            Recommendation(
                id = 405,
                title = "Hauz Khas Designer Boutiques",
                category = "Boho Shopping",
                rating = 4.3,
                reviewCount = "8k",
                transportMode = "🛺 5 min E-Rickshaw",
                description = "Quirky street market packed with indie apparel designers, antique jewellery, curio shops, and art studios.",
                imageResId = R.drawable.dilli_haat
            )
        )
    ),
    StationRecommendations(
        stationName = "Qutub Minar",
        line = "Yellow Line",
        recommendations = listOf(
            Recommendation(
                id = 501,
                title = "Qutub Minar Complex",
                category = "UNESCO World Heritage",
                rating = 4.7,
                reviewCount = "165k",
                transportMode = "🛺 5 min E-Rickshaw (₹20) from Gate 2",
                description = "73-meter fluted red sandstone victory minaret founded in 1192 along with the mysterious rustless Iron Pillar.",
                imageResId = R.drawable.qutub_minar
            ),
            Recommendation(
                id = 502,
                title = "Mehrauli Archaeological Park",
                category = "Heritage Forest",
                rating = 4.5,
                reviewCount = "18k",
                transportMode = "🚶 6 min walk from Qutub Minar",
                description = "Over 200 historic ruins spanning 1,000 years hidden amidst wooded parkland, including Jamali Kamali tomb.",
                imageResId = R.drawable.hauz_khas_fort
            ),
            Recommendation(
                id = 503,
                title = "Olive Bar & Kitchen",
                category = "Fine Dining",
                rating = 4.6,
                reviewCount = "14k",
                transportMode = "🚕 6 min Cab/Auto to One Style Mile",
                description = "Critically acclaimed white-walled Mediterranean dining under the canopy of a majestic banyan tree.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 504,
                title = "Garden of Five Senses",
                category = "Landscaped Park",
                rating = 4.2,
                reviewCount = "29k",
                transportMode = "🛺 8 min Auto (₹50) from station",
                description = "20-acre themed leisure park designed to stimulate taste, sight, smell, touch and hearing with stone sculptures.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 505,
                title = "Jamali Kamali Mosque & Stepwell",
                category = "Architecture & Mystery",
                rating = 4.4,
                reviewCount = "11k",
                transportMode = "🚶 10 min walk inside archaeological park",
                description = "16th-century Sufi saint mosque and stepwell, famous for exquisite plasterwork and local lore.",
                imageResId = R.drawable.qutub_minar
            )
        )
    ),
    StationRecommendations(
        stationName = "Akshardham",
        line = "Blue Line",
        recommendations = listOf(
            Recommendation(
                id = 601,
                title = "Swaminarayan Akshardham Temple",
                category = "Temple Marvel",
                rating = 4.8,
                reviewCount = "185k",
                transportMode = "🚶 5 min walk (Directly opposite station)",
                description = "Monumental Hindu temple complex exhibiting millennia of traditional Indian art, culture and spiritual values.",
                imageResId = R.drawable.akshardham
            ),
            Recommendation(
                id = 602,
                title = "Sahaj Anand Musical Water Show",
                category = "Light & Sound Spectacle",
                rating = 4.8,
                reviewCount = "45k",
                transportMode = "🚶 Inside Akshardham complex grounds",
                description = "Breathtaking 24-minute evening multimedia presentation with water fountains, laser projections and fire.",
                imageResId = R.drawable.akshardham
            ),
            Recommendation(
                id = 603,
                title = "Cultural Boat Ride (Sanskruti)",
                category = "Cultural Experience",
                rating = 4.7,
                reviewCount = "38k",
                transportMode = "🚶 Inside Akshardham exhibition halls",
                description = "A 12-minute immersive indoor boat voyage depicting 10,000 years of India's heritage, science and Vedic discoveries.",
                imageResId = R.drawable.akshardham
            ),
            Recommendation(
                id = 604,
                title = "Premvati Vegetarian Food Court",
                category = "Pure Vegetarian Dining",
                rating = 4.5,
                reviewCount = "22k",
                transportMode = "🚶 Located within temple premises",
                description = "Wholesome traditional snacks, Rajasthani thalis, dosas, and fresh sweets served in a clean heritage hall.",
                imageResId = R.drawable.paranthe_food
            ),
            Recommendation(
                id = 605,
                title = "Sanjay Lake Nature Park",
                category = "Ecolake & Boating",
                rating = 4.1,
                reviewCount = "16k",
                transportMode = "🛺 8 min Auto / E-Rickshaw",
                description = "Large artificial rainforest-edged freshwater lake with walking tracks, pedal boating, and migratory birdwatching.",
                imageResId = R.drawable.lodhi_garden
            )
        )
    ),
    StationRecommendations(
        stationName = "Kalkaji Mandir",
        line = "Violet & Magenta Line",
        recommendations = listOf(
            Recommendation(
                id = 701,
                title = "Lotus Temple (Bahá'í House)",
                category = "Modern Architecture",
                rating = 4.6,
                reviewCount = "175k",
                transportMode = "🚶 6 min walk from Exit Gate 2",
                description = "Stunning petal-shaped white marble sanctuary open to people of all faiths for silent meditation.",
                imageResId = R.drawable.lotus_temple
            ),
            Recommendation(
                id = 702,
                title = "Kalkaji Devi Mandir",
                category = "Ancient Hindu Temple",
                rating = 4.7,
                reviewCount = "68k",
                transportMode = "🚶 3 min walk (Adjacent to station)",
                description = "One of Delhi's most venerated Kali temples, dating back to Satya Yuga with vibrant daily aarti rituals.",
                imageResId = R.drawable.hanuman_temple
            ),
            Recommendation(
                id = 703,
                title = "ISKCON Temple Delhi",
                category = "Spiritual Center",
                rating = 4.7,
                reviewCount = "76k",
                transportMode = "🛺 6 min Auto (₹40) or 12 min walk",
                description = "Magnificent Hare Krishna shrine famous for spiritual art, Vedic museum, robot show and Govinda's pure veg buffet.",
                imageResId = R.drawable.akshardham
            ),
            Recommendation(
                id = 704,
                title = "Epicuria Food Mall (Nehru Place)",
                category = "Food Hub & Cafes",
                rating = 4.4,
                reviewCount = "39k",
                transportMode = "🚶 4 min walk (Connected under Nehru Place metro)",
                description = "Chic underground hub hosting premium burger joints, Japanese sushi, pan-Asian dining, and dessert parlours.",
                imageResId = R.drawable.kake_food
            ),
            Recommendation(
                id = 705,
                title = "Nehru Place Electronics Market",
                category = "Asia's Tech Hub",
                rating = 4.2,
                reviewCount = "48k",
                transportMode = "🚶 5 min walk",
                description = "Largest hardware and computer accessories market in South Asia offering parts, repairs, gadgets and deals.",
                imageResId = R.drawable.cp_image
            )
        )
    ),
    StationRecommendations(
        stationName = "Dilli Haat - INA",
        line = "Yellow & Pink Line",
        recommendations = listOf(
            Recommendation(
                id = 801,
                title = "Dilli Haat Crafts & Food Village",
                category = "Cultural Crafts Fair",
                rating = 4.6,
                reviewCount = "92k",
                transportMode = "🚶 1 min walk (Directly outside Gate 5)",
                description = "Permanent open-air bazaar showcasing rural crafts, handlooms, woodwork, and authentic food stalls from every state.",
                imageResId = R.drawable.dilli_haat
            ),
            Recommendation(
                id = 802,
                title = "Sarojini Nagar Market",
                category = "Fashion Street Market",
                rating = 4.4,
                reviewCount = "130k",
                transportMode = "🛺 5 min E-Rickshaw (₹20) or 10 min walk",
                description = "Delhi's most famous budget fashion hub for surplus export apparel, footwear, bags, and street snacks.",
                imageResId = R.drawable.dilli_haat
            ),
            Recommendation(
                id = 803,
                title = "INA Gourmet Food & Spice Market",
                category = "Exotic Food Bazaar",
                rating = 4.3,
                reviewCount = "24k",
                transportMode = "🚶 2 min walk across the footbridge",
                description = "Specialty culinary destination for fresh seafood, imported cheese, gourmet spices, fruits and pan-Asian ingredients.",
                imageResId = R.drawable.paranthe_food
            ),
            Recommendation(
                id = 804,
                title = "Safdarjung's Tomb",
                category = "Mughal Garden Tomb",
                rating = 4.5,
                reviewCount = "44k",
                transportMode = "🛺 5 min Auto (₹40) or 15 min walk",
                description = "Striking marble and sandstone garden mausoleum built in 1754, representing the last flame of Mughal architecture.",
                imageResId = R.drawable.hauz_khas_fort
            ),
            Recommendation(
                id = 805,
                title = "Momo Mia (Dilli Haat Nagaland Stall)",
                category = "Culinary Legend",
                rating = 4.5,
                reviewCount = "18k",
                transportMode = "🚶 Inside Dilli Haat (Stall 28)",
                description = "Celebrated for piping hot pork and chicken steamed momos served with fiery raja mircha chili chutney and fruit beer.",
                imageResId = R.drawable.kake_food
            )
        )
    ),
    StationRecommendations(
        stationName = "JLN Stadium",
        line = "Violet Line",
        recommendations = listOf(
            Recommendation(
                id = 901,
                title = "Lodhi Garden",
                category = "Historical Park",
                rating = 4.7,
                reviewCount = "88k",
                transportMode = "🛺 5 min Auto (₹40) or 12 min walk",
                description = "90-acre scenic city park containing 15th-century Sayyid and Lodhi architectural tombs, rose garden, and jogging path.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 902,
                title = "Lodhi Art District",
                category = "Open Air Murals",
                rating = 4.6,
                reviewCount = "21k",
                transportMode = "🚶 8 min walk into Lodhi Colony blocks",
                description = "India's first public street art district featuring giant murals by celebrated international and Indian artists.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 903,
                title = "India Habitat Centre (IHC)",
                category = "Arts & Culture",
                rating = 4.6,
                reviewCount = "34k",
                transportMode = "🛺 4 min Auto from Gate 2",
                description = "Prime cultural hub featuring art galleries, open amphitheatre, botanical courtyard, and the iconic All American Diner.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 904,
                title = "Khan Market",
                category = "High-End Shopping & Cafes",
                rating = 4.5,
                reviewCount = "45k",
                transportMode = "🚕 6 min Auto/Cab or 1 metro stop",
                description = "Elite lifestyle market packed with lifestyle bookstores, bespoke tailoring, artisanal bakeries, and fine restaurants.",
                imageResId = R.drawable.dilli_haat
            ),
            Recommendation(
                id = 905,
                title = "Jawaharlal Nehru Stadium Arena",
                category = "Sports & Mega Concerts",
                rating = 4.5,
                reviewCount = "52k",
                transportMode = "🚶 1 min walk (Directly at Gate 1)",
                description = "60,000-seater national stadium holding international sports, food festivals, marathons, and musical concerts.",
                imageResId = R.drawable.qutub_minar
            )
        )
    ),
    StationRecommendations(
        stationName = "Vishwa Vidyalaya",
        line = "Yellow Line",
        recommendations = listOf(
            Recommendation(
                id = 1001,
                title = "Kamla Nagar Market (K-Nags)",
                category = "Youth Shopping & Fashion",
                rating = 4.4,
                reviewCount = "62k",
                transportMode = "🛺 4 min E-Rickshaw (₹15) from Gate 3",
                description = "Bustling North Campus university bazaar famous for trendy apparel, sneaker shops, thrift clothing, and student hangouts.",
                imageResId = R.drawable.dilli_haat
            ),
            Recommendation(
                id = 1002,
                title = "Hudson Lane Cafes",
                category = "Student Cafes & Fast Food",
                rating = 4.3,
                reviewCount = "28k",
                transportMode = "🛺 5 min E-Rickshaw from Gate 4",
                description = "Vibrant foodie street lined with quirky themed cafes, peri-peri shakes, woodfired pizza, and student budget meals.",
                imageResId = R.drawable.kake_food
            ),
            Recommendation(
                id = 1003,
                title = "Tom Uncle's Maggi Point",
                category = "Campus Food Legend",
                rating = 4.2,
                reviewCount = "19k",
                transportMode = "🛺 5 min E-Rickshaw to Faculty of Arts",
                description = "Legendary DU hangout serving over 40 varieties of cheesy, butter, schezwan, and vegetable Maggi since 1978.",
                imageResId = R.drawable.paranthe_food
            ),
            Recommendation(
                id = 1004,
                title = "Northern Ridge (Bonta Park)",
                category = "Forest & Trekking",
                rating = 4.3,
                reviewCount = "15k",
                transportMode = "🚶 6 min walk from station",
                description = "Historic hilly forested tract with historic monuments like the Mutiny Memorial, Pir Ghaib, and serene cycling trails.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 1005,
                title = "Sudama Tea Stall",
                category = "Iconic Chai Spot",
                rating = 4.6,
                reviewCount = "14k",
                transportMode = "🛺 5 min E-Rickshaw (Near Hansraj College)",
                description = "Delhi's most celebrated student tea stall, renowned for ginger-cardamom cutting chai that draws queues late into the night.",
                imageResId = R.drawable.paranthe_food
            )
        )
    ),
    StationRecommendations(
        stationName = "Mandi House",
        line = "Blue & Violet Line",
        recommendations = listOf(
            Recommendation(
                id = 1101,
                title = "National School of Drama (NSD)",
                category = "Theatre & Culture",
                rating = 4.7,
                reviewCount = "16k",
                transportMode = "🚶 3 min walk from Gate 1",
                description = "Premier theatre training institute in India, hosting live stage plays, experimental drama, and the Bharat Rang Mahotsav.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 1102,
                title = "Triveni Terrace Cafe",
                category = "Open Air Arts Cafe",
                rating = 4.4,
                reviewCount = "24k",
                transportMode = "🚶 5 min walk (Triveni Kala Sangam)",
                description = "Peaceful cultural cafe overlooking a grass amphitheatre, loved for masala chai, palak patta chaat, and shammi kebabs.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 1103,
                title = "Bengali Market Sweets",
                category = "Heritage Confectionery",
                rating = 4.4,
                reviewCount = "36k",
                transportMode = "🚶 6 min walk or 🛺 2 min Auto",
                description = "Famous for Bengali Sweet House & Nathu's Sweets serving rasgullas, chole bhature, samosas, and raj kachori.",
                imageResId = R.drawable.paranthe_food
            ),
            Recommendation(
                id = 1104,
                title = "Kamani Auditorium",
                category = "Performance Theatre",
                rating = 4.6,
                reviewCount = "18k",
                transportMode = "🚶 4 min walk (Copernicus Marg)",
                description = "Delhi's landmark 600-seater classical performance venue staging ballets, classical musical concerts, and dramas.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 1105,
                title = "Shri Ram Centre for Performing Arts",
                category = "Independent Drama",
                rating = 4.6,
                reviewCount = "14k",
                transportMode = "🚶 2 min walk directly opposite metro",
                description = "Active cultural theatre hub with round-the-year Hindi and English theatrical productions and drama workshops.",
                imageResId = R.drawable.cp_image
            )
        )
    ),
    StationRecommendations(
        stationName = "Kashmere Gate",
        line = "Red, Yellow & Violet Line",
        recommendations = listOf(
            Recommendation(
                id = 1201,
                title = "St. James' Church (Skinner's Church)",
                category = "Historic Church",
                rating = 4.6,
                reviewCount = "12k",
                transportMode = "🚶 4 min walk from Gate 1",
                description = "Consecrated in 1836, this Renaissance-style church is one of the oldest surviving worship sanctuaries in Delhi.",
                imageResId = R.drawable.cp_image
            ),
            Recommendation(
                id = 1202,
                title = "Kashmiri Gate Monument",
                category = "Historical Gate",
                rating = 4.3,
                reviewCount = "14k",
                transportMode = "🚶 3 min walk (Gate 2)",
                description = "Northern gate of the historic walled city of Shahjahanabad, marked with cannonball scars from the 1857 siege.",
                imageResId = R.drawable.red_fort
            ),
            Recommendation(
                id = 1203,
                title = "Roshanara Bagh & Tomb",
                category = "Mughal Garden",
                rating = 4.3,
                reviewCount = "22k",
                transportMode = "🛺 8 min Auto (₹50) from station",
                description = "Expansive historic Mughal pleasure garden with a central pavilion and tomb of Shah Jahan's daughter, Roshanara.",
                imageResId = R.drawable.lodhi_garden
            ),
            Recommendation(
                id = 1204,
                title = "ISBT Kashmere Gate Food Plaza",
                category = "Transit Food Hub",
                rating = 4.1,
                reviewCount = "31k",
                transportMode = "🚶 Directly connected via metro concourse",
                description = "Major transit hub food court offering North Indian thalis, quick snacks, hot tea, and bakeries for travelers.",
                imageResId = R.drawable.kake_food
            ),
            Recommendation(
                id = 1205,
                title = "Nicholson Cemetery",
                category = "Victorian Heritage",
                rating = 4.3,
                reviewCount = "8k",
                transportMode = "🚶 5 min walk (Near Metro Gate 4)",
                description = "Peaceful colonial heritage cemetery established in the 1850s, surrounded by ancient tamarind and neem trees.",
                imageResId = R.drawable.hauz_khas_fort
            )
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreDelhiScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStation by remember { mutableStateOf<StationRecommendations?>(null) }

    val filteredStations = exploreDelhiData.filter {
        it.stationName.contains(searchQuery, ignoreCase = true) ||
        it.line.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp)
    ) {
        if (selectedStation != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(onClick = { selectedStation = null }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to stations",
                        tint = Color(0xFFD32F2F)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = selectedStation!!.stationName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Text(
                        text = selectedStation!!.line,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "${selectedStation!!.recommendations.size} Must-Visit Places Nearby",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(selectedStation!!.recommendations) { rec ->
                    RecommendationCard(recommendation = rec)
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            Text(
                text = "Explore Delhi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Text(
                text = "Discover top food, monuments & attractions around popular metro stations",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by station or line (e.g. Rajiv Chowk, Yellow)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFD32F2F)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Popular Metro Hubs (${filteredStations.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredStations) { station ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStation = station },
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = station.stationName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = station.line,
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${station.recommendations.size} places",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = recommendation.imageResId),
                contentDescription = recommendation.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = recommendation.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = recommendation.category,
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${recommendation.rating}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${recommendation.reviewCount} on Google)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Transport",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = recommendation.transportMode,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = recommendation.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}
