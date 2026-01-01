// FILE: ui/DetailDefaults.kt
package com.example.denikplus.ui

import com.example.denikplus.data.DetailCategory
import com.example.denikplus.data.DetailItem

fun defaultDetailCategories(): List<DetailCategory> = listOf(
    DetailCategory(
        id = "sexual",
        title = "Sexuální aktivita",
        items = listOf(
            DetailItem("sex", "Sex"),
            DetailItem("masturbation", "Masturbace"),
            DetailItem("menstruation", "Menstruace"),
            DetailItem("orgasm", "Orgasmus"),
            DetailItem("contraception", "Antikoncepce")
        )
    ),
    DetailCategory(
        id = "physical",
        title = "Fyzická aktivita",
        items = listOf(
            DetailItem("walk", "Chůze"),
            DetailItem("run", "Běh"),
            DetailItem("gym", "Posilovna"),
            DetailItem("bike", "Cyklistika"),
            DetailItem("hike", "Turistika"),
            DetailItem("swim", "Plavání"),
            DetailItem("yoga", "Jóga")
        )
    ),
    DetailCategory(
        id = "sleep",
        title = "Spánek",
        items = listOf(
            DetailItem("good_sleep", "Kvalitní"),
            DetailItem("bad_sleep", "Nekvalitní"),
            DetailItem("late", "Pozdě do postele"),
            DetailItem("early", "Brzy do postele"),
            DetailItem("nightmares", "Noční můry")
        )
    ),
    DetailCategory(
        id = "food",
        title = "Jídlo & pití",
        items = listOf(
            DetailItem("healthy", "Zdravě"),
            DetailItem("fastfood", "Fastfood"),
            DetailItem("coffee", "Káva"),
            DetailItem("sweets", "Sladké"),
            DetailItem("alcohol", "Alkohol")
        )
    ),
    DetailCategory(
        id = "work",
        title = "Práce/Škola",
        items = listOf(
            DetailItem("productive", "Produktivita"),
            DetailItem("stress", "Stres"),
            DetailItem("deadline", "Deadline"),
            DetailItem("meeting", "Meeting")
        )
    ),
    DetailCategory(
        id = "social",
        title = "Sociální",
        items = listOf(
            DetailItem("family", "Rodina"),
            DetailItem("friends", "Přátelé"),
            DetailItem("party", "Party"),
            DetailItem("alone", "Samota")
        )
    ),
    DetailCategory(
        id = "health",
        title = "Zdraví",
        items = listOf(
            DetailItem("pain", "Bolest"),
            DetailItem("ill", "Nemoc"),
            DetailItem("meds", "Léky"),
            DetailItem("doctor", "Doktor")
        )
    ),
    DetailCategory(
        id = "brainstorm",
        title = "Brainstorming",
        items = listOf(
            DetailItem("idea", "Nápad"),
            DetailItem("project", "Projekt"),
            DetailItem("goal", "Cíl")
        )
    ),
    DetailCategory(
        id = "other",
        title = "Ostatní",
        items = listOf(
            DetailItem("travel", "Cestování"),
            DetailItem("shopping", "Nákupy"),
            DetailItem("finance", "Finance"),
            DetailItem("cleaning", "Úklid")
        )
    )
)
