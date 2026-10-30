
package com.example.skilllinkapp.screens

internal fun authText(key: String, language: String): String {
    val translations = mapOf(
        "en" to mapOf(
            "welcome" to "Welcome back!",
            "login_subtitle" to "Log in to continue your SkillLink journey.",
            "email" to "Email address",
            "password" to "Password",
            "show" to "Show",
            "hide" to "Hide",
            "login" to "Log in",
            "logging_in" to "Logging in...",
            "empty_login" to "Please enter your email and password.",
            "no_account" to "Don't have an account?",
            "create_account" to "Create an account",
            "register_title" to "Create your account",
            "register_subtitle" to "Join SkillLink and start learning from others.",
            "full_name" to "Full name",
            "confirm_password" to "Confirm password",
            "fill_fields" to "Please fill in all fields.",
            "password_mismatch" to "Passwords do not match.",
            "account_created" to "Account created successfully. Check your email.",
            "has_account" to "Already have an account?",
            "tagline" to "Teach. Learn. Connect.",
            "creating_account" to "Creating account..."
        ),
        "zu" to mapOf(
            "welcome" to "Siyakwamukela futhi!",
            "login_subtitle" to "Ngena ukuze uqhubeke nohambo lwakho lwe-SkillLink.",
            "email" to "Ikheli le-imeyili",
            "password" to "Iphasiwedi",
            "show" to "Bonisa",
            "hide" to "Fihla",
            "login" to "Ngena ngemvume",
            "logging_in" to "Kuyangena...",
            "empty_login" to "Sicela ufake ikheli lakho le-imeyili nephasiwedi.",
            "no_account" to "Awunayo i-akhawunti?",
            "create_account" to "Dala i-akhawunti",
            "register_title" to "Dala i-akhawunti yakho",
            "register_subtitle" to "Joyina i-SkillLink ufunde kwabanye.",
            "full_name" to "Igama eligcwele",
            "confirm_password" to "Qinisekisa iphasiwedi",
            "fill_fields" to "Sicela ugcwalise zonke izikhala.",
            "password_mismatch" to "Amaphasiwedi awafani.",
            "account_created" to "I-akhawunti idaliwe ngempumelelo. Hlola i-imeyili yakho.",
            "has_account" to "Usunayo i-akhawunti?",
            "tagline" to "Fundisa. Funda. Xhumana.",
            "creating_account" to "Kudalwa i-akhawunti..."
        ),
        "af" to mapOf(
            "welcome" to "Welkom terug!",
            "login_subtitle" to "Meld aan om met jou SkillLink-reis voort te gaan.",
            "email" to "E-posadres",
            "password" to "Wagwoord",
            "show" to "Wys",
            "hide" to "Versteek",
            "login" to "Meld aan",
            "logging_in" to "Besig om aan te meld...",
            "empty_login" to "Voer asseblief jou e-posadres en wagwoord in.",
            "no_account" to "Het jy nie 'n rekening nie?",
            "create_account" to "Skep 'n rekening",
            "register_title" to "Skep jou rekening",
            "register_subtitle" to "Sluit by SkillLink aan en leer by ander.",
            "full_name" to "Volle naam",
            "confirm_password" to "Bevestig wagwoord",
            "fill_fields" to "Vul asseblief al die velde in.",
            "password_mismatch" to "Die wagwoorde stem nie ooreen nie.",
            "account_created" to "Rekening suksesvol geskep. Gaan jou e-pos na.",
            "has_account" to "Het jy reeds 'n rekening?",
            "tagline" to "Leer. Onderrig. Verbind.",
            "creating_account" to "Besig om rekening te skep..."
        )
    )

    return translations[language]?.get(key)
        ?: translations["en"]?.get(key)
        ?: key
}
