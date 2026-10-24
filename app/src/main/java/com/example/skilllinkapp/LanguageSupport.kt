
package com.example.skilllinkapp

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object LanguageSupport {

    const val ENGLISH = "en"
    const val ZULU = "zu"
    const val AFRIKAANS = "af"

    private const val PREFERENCES_NAME = "skilllink_preferences"
    private const val LANGUAGE_KEY = "app_language"

    val currentLanguage = mutableStateOf(ENGLISH)

    private val zulu = mapOf(
        "Home" to "Ikhaya",
        "Roadmap" to "Uhlelo lokufunda",
        "My Learning" to "Ukufunda kwami",
        "Profile" to "Iphrofayela",
        "Settings" to "Izilungiselelo",
        "Account" to "I-akhawunti",
        "Preferences" to "Izintandokazi",
        "Support" to "Usizo",
        "Language" to "Ulimi",
        "Notifications" to "Izaziso",
        "Password" to "Iphasiwedi",
        "Change Password" to "Shintsha iphasiwedi",
        "Profile Settings" to "Izilungiselelo zephrofayela",
        "Full Name" to "Igama eligcwele",
        "Email" to "I-imeyili",
        "Email address" to "Ikheli le-imeyili",
        "Location" to "Indawo",
        "Cancel" to "Khansela",
        "Done" to "Kwenziwe",
        "Delete" to "Susa",
        "Logout" to "Phuma",
        "Log out" to "Phuma",
        "Login" to "Ngena",
        "Log in" to "Ngena",
        "Create Account" to "Dala i-akhawunti",
        "Create account" to "Dala i-akhawunti",
        "Save" to "Londoloza",
        "Save Changes" to "Londoloza izinguquko",
        "Edit Profile" to "Hlela iphrofayela",
        "Generate Roadmap" to "Dala uhlelo lokufunda",
        "Generate roadmap" to "Dala uhlelo lokufunda",
        "Progress" to "Inqubekelaphambili",
        "Welcome" to "Siyakwamukela",
        "Welcome back!" to "Siyakwamukela futhi!",
        "Your learning journey" to "Uhambo lwakho lokufunda",
        "Your learning journey starts here." to
                "Uhambo lwakho lokufunda luqala lapha.",
        "Loading..." to "Iyalayisha...",
        "Loading your learning journey..." to
                "Kulayishwa uhambo lwakho lokufunda...",
        "Beginner" to "Osaqalayo",
        "Intermediate" to "Osezingeni eliphakathi",
        "Advanced" to "Osezingeni eliphezulu",
        "Relaxed" to "Ngokukhululeka",
        "Balanced" to "Ngokulinganisela",
        "Fast" to "Ngokushesha",
        "Ready to learn something new?" to "Usukulungele ukufunda okusha?",
        "Teach. Learn. Connect." to "Fundisa. Funda. Xhumana.",
        "Share what you know, discover new skills, and build your personalised learning journey." to
                "Yabelana ngalokho okwaziyo, thola amakhono amasha, futhi wakhe uhambo lwakho lokufunda.",
        "Recommended skills" to "Amakhono anconyiwe",
        "Programming" to "Ukuhlela izinhlelo zekhompyutha",
        "Android" to "Android",
        "Start your learning journey" to "Qala uhambo lwakho lokufunda",
        "Your learning roadmap" to "Uhlelo lwakho lokufunda",
        "Continue working through your personalised learning roadmap." to
                "Qhubeka nohlelo lwakho lokufunda oluqondene nawe.",
        "You completed this roadmap. Keep building your skills or start a new one." to
                "Uqedile lolu hlelo lokufunda. Qhubeka uthuthukisa amakhono akho noma uqale olusha.",
        "Continue learning" to "Qhubeka nokufunda",
        "View roadmap" to "Buka uhlelo lokufunda",
        "Create another roadmap" to "Dala olunye uhlelo lokufunda",
        "Tell SkillLink what you want to learn and create a personalised roadmap." to
                "Tshela i-SkillLink ukuthi yini ofuna ukuyifunda bese udala uhlelo lokufunda oluqondene nawe.",
        "No roadmaps are saved for offline use yet. Connect to the internet and open My Learning first." to
                "Azikho izinhlelo zokufunda ezigcinelwe ukusetshenziswa ngaphandle kwe-inthanethi okwamanje. Xhuma ku-inthanethi bese uvula ingxenye ethi Ukufunda Kwami kuqala.",
        "Could not load your dashboard or saved offline roadmap." to
                "Ayikwazanga ukulayisha ikhasi lakho eliyinhloko noma uhlelo lokufunda olugciniwe olungasetshenziswa ngaphandle kwe-inthanethi."
    )

    private val afrikaans = mapOf(
        "Home" to "Tuis",
        "Roadmap" to "Leerplan",
        "My Learning" to "My leerwerk",
        "Profile" to "Profiel",
        "Settings" to "Instellings",
        "Account" to "Rekening",
        "Preferences" to "Voorkeure",
        "Support" to "Ondersteuning",
        "Language" to "Taal",
        "Notifications" to "Kennisgewings",
        "Password" to "Wagwoord",
        "Change Password" to "Verander wagwoord",
        "Profile Settings" to "Profielinstellings",
        "Full Name" to "Volle naam",
        "Email" to "E-pos",
        "Email address" to "E-posadres",
        "Location" to "Ligging",
        "Cancel" to "Kanselleer",
        "Done" to "Klaar",
        "Delete" to "Verwyder",
        "Logout" to "Teken uit",
        "Log out" to "Teken uit",
        "Login" to "Teken in",
        "Log in" to "Teken in",
        "Create Account" to "Skep rekening",
        "Create account" to "Skep rekening",
        "Save" to "Stoor",
        "Save Changes" to "Stoor veranderinge",
        "Edit Profile" to "Wysig profiel",
        "Generate Roadmap" to "Skep leerplan",
        "Generate roadmap" to "Skep leerplan",
        "Progress" to "Vordering",
        "Welcome" to "Welkom",
        "Welcome back!" to "Welkom terug!",
        "Your learning journey" to "Jou leerreis",
        "Your learning journey starts here." to
                "Jou leerreis begin hier.",
        "Loading..." to "Laai tans...",
        "Loading your learning journey..." to
                "Jou leerreis word gelaai...",
        "Beginner" to "Beginner",
        "Intermediate" to "Intermediêr",
        "Advanced" to "Gevorderd",
        "Relaxed" to "Ontspanne",
        "Balanced" to "Gebalanseerd",
        "Fast" to "Vinnig",
        "Ready to learn something new?" to "Gereed om iets nuuts te leer?",
        "Teach. Learn. Connect." to "Leer. Onderrig. Verbind.",
        "Share what you know, discover new skills, and build your personalised learning journey." to
                "Deel wat jy weet, ontdek nuwe vaardighede en bou jou persoonlike leerreis.",
        "Recommended skills" to "Aanbevole vaardighede",
        "Programming" to "Programmering",
        "Android" to "Android",
        "Start your learning journey" to "Begin jou leerreis",
        "Your learning roadmap" to "Jou leerplan",
        "Continue working through your personalised learning roadmap." to
                "Gaan voort met jou persoonlike leerplan.",
        "You completed this roadmap. Keep building your skills or start a new one." to
                "Jy het hierdie leerplan voltooi. Hou aan om jou vaardighede te ontwikkel of begin 'n nuwe een.",
        "Continue learning" to "Gaan voort met leer",
        "View roadmap" to "Bekyk leerplan",
        "Create another roadmap" to "Skep nog 'n leerplan",
        "Tell SkillLink what you want to learn and create a personalised roadmap." to
                "Vertel SkillLink wat jy wil leer en skep 'n persoonlike leerplan.",
        "No roadmaps are saved for offline use yet. Connect to the internet and open My Learning first." to
                "Geen leerplanne is nog vir vanlyn gebruik gestoor nie. Koppel aan die internet en maak eers My Leerwerk oop.",
        "Could not load your dashboard or saved offline roadmap." to
                "Kon nie jou kontroleskerm of gestoorde vanlyn leerplan laai nie."
    )

    fun load(context: Context) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        val savedLanguage = preferences.getString(
            LANGUAGE_KEY,
            ENGLISH
        ) ?: ENGLISH

        currentLanguage.value = when (savedLanguage) {
            ZULU -> ZULU
            AFRIKAANS -> AFRIKAANS
            else -> ENGLISH
        }
    }

    fun set(context: Context, language: String) {
        val validLanguage = when (language) {
            ZULU -> ZULU
            AFRIKAANS -> AFRIKAANS
            else -> ENGLISH
        }

        currentLanguage.value = validLanguage

        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putString(LANGUAGE_KEY, validLanguage)
            .apply()
    }

    fun translate(english: String): String {
        return when (currentLanguage.value) {
            ZULU -> zulu[english] ?: english
            AFRIKAANS -> afrikaans[english] ?: english
            else -> english
        }
    }

    fun localizedProgressText(
        completed: Int,
        total: Int
    ): String {
        return when (currentLanguage.value) {
            AFRIKAANS -> "$completed van $total lesse voltooi"
            ZULU -> "$completed kwezingu-$total izifundo seziqediwe"
            else -> "$completed of $total lessons completed"
        }
    }
}

fun localizedText(english: String): String {
    return LanguageSupport.translate(english)
}


