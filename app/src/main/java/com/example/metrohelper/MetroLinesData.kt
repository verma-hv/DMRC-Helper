package com.example.metrohelper

import androidx.compose.ui.graphics.Color

data class MetroLine(
    val id: String,
    val name: String,
    val color: Color,
    val stations: List<String>
)

object MetroNetwork {
    val redLine = MetroLine(
        id = "RED",
        name = "Red Line",
        color = Color(0xFFD32F2F),
        stations = listOf(
            "Shaheed Sthal", "Hindon", "Arthala", "Mohan Nagar", "Shyam Park",
            "Major Mohit Sharma", "Raj Bagh", "Shaheed Nagar", "Dilshad Garden",
            "Jhilmil", "Mansarovar Park", "Shahdara", "Welcome", "Seelampur",
            "Shastri Park", "Kashmere Gate", "Tis Hazari", "Pul Bangash",
            "Pratap Nagar", "Shastri Nagar", "Inderlok", "Kanhaiya Nagar",
            "Keshav Puram", "Netaji Subhash Place", "Kohat Enclave", "Pitam Pura",
            "Rohini East", "Rohini West", "Rithala"
        )
    )

    val yellowLine = MetroLine(
        id = "YELLOW",
        name = "Yellow Line",
        color = Color(0xFFF9A825), // Amber Yellow
        stations = listOf(
            "Samaypur Badli", "Rohini Sector 18", "Haiderpur", "Jahangirpuri",
            "Adarsh Nagar", "Azadpur", "Model Town", "GTB Nagar",
            "Vishwa Vidyalaya", "Vidhan Sabha", "Civil Lines", "Kashmere Gate",
            "Chandni Chowk", "Chawri Bazar", "New Delhi", "Rajiv Chowk",
            "Patel Chowk", "Central Secretariat", "Udyog Bhawan", "Lok Kalyan Marg",
            "Jor Bagh", "INA", "AIIMS", "Green Park", "Hauz Khas",
            "Malviya Nagar", "Saket", "Qutab Minar", "Chhatarpur", "Sultanpur",
            "Ghitorni", "Arjan Garh", "Guru Dronacharya", "Sikandarpur", "MG Road",
            "IFFCO Chowk", "HUDA City Centre"
        )
    )

    val blueLineMain = MetroLine(
        id = "BLUE_MAIN",
        name = "Blue Line",
        color = Color(0xFF1565C0),
        stations = listOf(
            "Dwarka Sector 21", "Dwarka Sector 8", "Dwarka Sector 9", "Dwarka Sector 10",
            "Dwarka Sector 11", "Dwarka Sector 12", "Dwarka Sector 13", "Dwarka Sector 14",
            "Dwarka", "Dwarka Mor", "Nawada", "Uttam Nagar West", "Uttam Nagar East",
            "Janakpuri West", "Janakpuri East", "Tilak Nagar", "Subhash Nagar",
            "Tagore Garden", "Rajouri Garden", "Ramesh Nagar", "Moti Nagar",
            "Kirti Nagar", "Shadipur", "Patel Nagar", "Rajendra Place",
            "Karol Bagh", "Jhandewalan", "Ramakrishna Ashram Marg", "Rajiv Chowk",
            "Barakhambha Road", "Mandi House", "Supreme Court", "Indraprastha",
            "Yamuna Bank", "Akshardham", "Mayur Vihar-I", "Mayur Vihar Extension",
            "New Ashok Nagar", "Noida Sector 15", "Noida Sector 16", "Noida Sector 18",
            "Botanical Garden", "Golf Course", "Noida City Centre", "Noida Sector 34",
            "Noida Sector 52", "Noida Sector 61", "Noida Sector 59", "Noida Sector 62",
            "Noida Electronic City"
        )
    )

    val blueLineBranch = MetroLine(
        id = "BLUE_BRANCH",
        name = "Blue Line (Vaishali Branch)",
        color = Color(0xFF1565C0),
        stations = listOf(
            "Yamuna Bank", "Laxmi Nagar", "Nirman Vihar", "Preet Vihar",
            "Karkarduma", "Anand Vihar ISBT", "Kaushambi", "Vaishali"
        )
    )

    val greenLineMain = MetroLine(
        id = "GREEN_MAIN",
        name = "Green Line",
        color = Color(0xFF2E7D32),
        stations = listOf(
            "Brigadier Hoshiyar Singh", "Bahadurgarh City", "Pandit Shree Ram Sharma",
            "Tikri Border", "Tikri Kalan", "Ghevra", "Mundka Industrial Area",
            "Mundka", "Rajdhani Park", "Nangloi Railway Station", "Nangloi",
            "Maharaja Surajmal Stadium", "Udyog Nagar", "Peera Garhi",
            "Paschim Vihar West", "Paschim Vihar East", "Madipur", "Shivaji Park",
            "Punjabi Bagh", "Punjabi Bagh West", "Ashok Park Main"
        )
    )

    val greenLineInderlokBranch = MetroLine(
        id = "GREEN_INDERLOK",
        name = "Green Line (Inderlok Branch)",
        color = Color(0xFF2E7D32),
        stations = listOf("Ashok Park Main", "Inderlok")
    )

    val greenLineKirtiBranch = MetroLine(
        id = "GREEN_KIRTI",
        name = "Green Line (Kirti Nagar Branch)",
        color = Color(0xFF2E7D32),
        stations = listOf("Ashok Park Main", "Satguru Ramsingh Marg", "Kirti Nagar")
    )

    val violetLine = MetroLine(
        id = "VIOLET",
        name = "Violet Line",
        color = Color(0xFF6A1B9A),
        stations = listOf(
            "Kashmere Gate", "Lal Qila", "Jama Masjid", "Delhi Gate", "ITO",
            "Mandi House", "Janpath", "Central Secretariat", "Khan Market",
            "Jawaharlal Nehru Stadium", "Jangpura", "Lajpat Nagar", "Moolchand",
            "Kailash Colony", "Nehru Place", "Kalkaji Mandir", "Govind Puri",
            "Harkesh Nagar Okhla", "Jasola Apollo", "Sarita Vihar", "Mohan Estate",
            "Tughlakabad Station", "Badarpur Border", "Sarai", "NHPC Chowk",
            "Mewala Maharajpur", "Sector 28", "Badkhal Mor", "Old Faridabad",
            "Neelam Chowk Ajronda", "Bata Chowk", "Escorts Mujesar",
            "Sant Surdas (Sihi)", "Raja Nahar Singh"
        )
    )

    val pinkLine = MetroLine(
        id = "PINK",
        name = "Pink Line",
        color = Color(0xFFAD1457),
        stations = listOf(
            "Majlis Park", "Azadpur", "Shalimar Bagh", "Netaji Subhash Place",
            "Shakurpur", "Punjabi Bagh West", "ESI Hospital", "Rajouri Garden",
            "Mayapuri", "Naraina Vihar", "Delhi Cantonment",
            "Durgabai Deshmukh South Campus", "Sir Vishweshwaraiah Moti Bagh",
            "Bhikaji Cama Place", "Sarojini Nagar", "INA", "South Extension",
            "Lajpat Nagar", "Vinobapuri", "Ashram", "Hazrat Nizamuddin",
            "Mayur Vihar-I", "Mayur Vihar Pocket I", "Trilokpuri Sanjay Lake",
            "East Vinod Nagar - Mayur Vihar-II", "Mandawali West Vinod Nagar",
            "IP Extension", "Anand Vihar ISBT", "Karkarduma", "Karkarduma Court",
            "Krishna Nagar", "East Azad Nagar", "Welcome", "Jaffrabad",
            "Maujpur-Babarpur", "Gokulpuri", "Johari Enclave", "Shiv Vihar"
        )
    )

    val magentaLine = MetroLine(
        id = "MAGENTA",
        name = "Magenta Line",
        color = Color(0xFF880E4F),
        stations = listOf(
            "Janakpuri West", "Dabri Mor-Janakpuri South", "Dashrath Puri",
            "Palam", "Sadar Bazaar Cantonment", "Terminal 1-IGI Airport",
            "Shankar Vihar", "Vasant Vihar", "Munirka", "R.K.Puram",
            "IIT Delhi", "Hauz Khas", "Panchsheel Park", "Chirag Delhi",
            "Greater Kailash", "Nehru Enclave", "Kalkaji Mandir", "Okhla NSIC",
            "Sukhdev Vihar", "Jamia Millia Islamia", "Okhla Vihar",
            "Jasola Vihar Shaheen Bagh", "Kalindi Kunj", "Okhla Bird Sanctuary",
            "Botanical Garden"
        )
    )

    val airportExpress = MetroLine(
        id = "ORANGE",
        name = "Airport Express",
        color = Color(0xFFE65100),
        stations = listOf(
            "New Delhi", "Shivaji Stadium", "Dhaula Kuan", "Delhi Aerocity",
            "Indira Gandhi International Airport", "Dwarka Sector 21"
        )
    )

    val greyLine = MetroLine(
        id = "GREY",
        name = "Grey Line",
        color = Color(0xFF546E7A),
        stations = listOf("Dwarka", "Nangli", "Najafgarh")
    )

    val allLines = listOf(
        redLine,
        yellowLine,
        blueLineMain,
        blueLineBranch,
        greenLineMain,
        greenLineInderlokBranch,
        greenLineKirtiBranch,
        violetLine,
        pinkLine,
        magentaLine,
        airportExpress,
        greyLine
    )
}

