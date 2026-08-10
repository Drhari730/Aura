package com.mhealth.aura.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Language : Screen("language")
    object Otp : Screen("otp")
    object RegStep1 : Screen("reg_step1")
    object RegStep2 : Screen("reg_step2")
    object RegStep3 : Screen("reg_step3")
    object Main : Screen("main")
    object Home : Screen("home")
    object MyMeds : Screen("my_meds")
    object Progress : Screen("progress")
    object Learn : Screen("learn")
    object Ask : Screen("ask")
    object TakeDose : Screen("take_dose")
    object MedPlanner : Screen("med_planner")
    object Adr : Screen("adr")
    object Diary : Screen("diary")
    object Settings : Screen("settings")
}
