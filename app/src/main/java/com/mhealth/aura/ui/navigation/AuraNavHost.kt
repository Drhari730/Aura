package com.mhealth.aura.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mhealth.aura.BuildConfig
import com.mhealth.aura.data.db.AuraDatabase
import com.mhealth.aura.data.db.entity.MedicationEntity
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.data.prefs.AppPreferences
import com.mhealth.aura.data.remote.BrevoOtpService
import com.mhealth.aura.data.repository.DoseRepository
import com.mhealth.aura.data.repository.MedicationRepository
import com.mhealth.aura.data.repository.UserRepository
import com.mhealth.aura.notifications.NotificationHelper
import com.mhealth.aura.notifications.ReminderScheduler
import com.mhealth.aura.ui.screens.SplashScreen
import com.mhealth.aura.ui.screens.features.AdrScreen
import com.mhealth.aura.ui.screens.features.AskAuraScreen
import com.mhealth.aura.ui.screens.features.LearnScreen
import com.mhealth.aura.ui.screens.features.ProgressScreen
import com.mhealth.aura.ui.screens.home.HomeScreen
import com.mhealth.aura.ui.screens.home.HomeViewModel
import com.mhealth.aura.ui.screens.medication.MedicationDiaryScreen
import com.mhealth.aura.ui.screens.onboarding.LanguageScreen
import com.mhealth.aura.ui.screens.onboarding.OtpScreen
import com.mhealth.aura.ui.screens.onboarding.RegStep1Screen
import com.mhealth.aura.ui.screens.onboarding.RegStep2Screen
import com.mhealth.aura.ui.screens.onboarding.RegStep3Screen
import com.mhealth.aura.ui.screens.onboarding.WelcomeScreen
import com.mhealth.aura.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AuraNavHost(
    navController: NavHostController,
    db: AuraDatabase,
    prefs: AppPreferences
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onboardingDone by prefs.isOnboardingDone.collectAsStateWithLifecycle(initialValue = false)
    val authenticatedEmail by prefs.authenticatedEmail.collectAsStateWithLifecycle(initialValue = "")
    var draftUser by remember { mutableStateOf(UserEntity()) }
    var registrationMode by remember { mutableStateOf(false) }
    val authService = remember {
        BrevoOtpService(
            apiBaseUrl = BuildConfig.AURA_API_BASE_URL
        )
    }

    fun saveUserAndReschedule(user: UserEntity) {
        scope.launch {
            db.userDao().saveUser(user)
            ReminderScheduler.scheduleCourses(
                context.applicationContext,
                user,
                db.medicationDao().getAllSnapshot()
            )
        }
    }

    fun saveMedicationAndReschedule(medication: MedicationEntity) {
        scope.launch {
            if (medication.id == 0L) {
                db.medicationDao().insert(medication)
            } else {
                db.medicationDao().update(medication)
            }
            val medications = db.medicationDao().getAllSnapshot()
            db.userDao().getUserSnapshot()?.let { user ->
                val primary = medications.firstOrNull { it.isActive } ?: medications.firstOrNull()
                val updatedUser = if (primary != null) {
                    user.copy(
                        antibiotic = primary.name,
                        dose = primary.dose,
                        frequency = primary.frequency,
                        durationDays = (
                            ((primary.endDateMillis - primary.startDateMillis)
                                .coerceAtLeast(0L) / 86_400_000L) + 1
                            ).toInt(),
                        startDateMillis = primary.startDateMillis,
                        endDateMillis = primary.endDateMillis,
                        doseTimesCsv = primary.doseTimesCsv
                    )
                } else {
                    user.copy(antibiotic = "", dose = "")
                }
                db.userDao().saveUser(updatedUser)
                ReminderScheduler.scheduleCourses(
                    context.applicationContext,
                    updatedUser,
                    medications
                )
            }
        }
    }

    fun deleteMedicationAndReschedule(medication: MedicationEntity) {
        scope.launch {
            db.medicationDao().delete(medication)
            val medications = db.medicationDao().getAllSnapshot()
            db.userDao().getUserSnapshot()?.let { user ->
                val primary = medications.firstOrNull { it.isActive } ?: medications.firstOrNull()
                val updatedUser = if (primary != null) {
                    user.copy(
                        antibiotic = primary.name,
                        dose = primary.dose,
                        frequency = primary.frequency,
                        durationDays = (
                            ((primary.endDateMillis - primary.startDateMillis)
                                .coerceAtLeast(0L) / 86_400_000L) + 1
                            ).toInt(),
                        startDateMillis = primary.startDateMillis,
                        endDateMillis = primary.endDateMillis,
                        doseTimesCsv = primary.doseTimesCsv
                    )
                } else {
                    user.copy(antibiotic = "", dose = "")
                }
                db.userDao().saveUser(updatedUser)
                ReminderScheduler.scheduleCourses(
                    context.applicationContext,
                    updatedUser,
                    medications
                )
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(isOnboardingDone = onboardingDone) { done ->
                val destination = when {
                    authenticatedEmail.isBlank() -> Screen.Welcome.route
                    done -> Screen.Home.route
                    else -> Screen.Language.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onEmailLogin = {
                    registrationMode = false
                    navController.navigate(Screen.Otp.route)
                },
                onNewRegistration = {
                    registrationMode = true
                    navController.navigate(Screen.Language.route)
                }
            )
        }
        composable(Screen.Language.route) {
            LanguageScreen { language ->
                scope.launch { prefs.setLanguage(language) }
                draftUser = draftUser.copy(language = language)
                navController.navigate(Screen.Otp.route)
            }
        }
        composable(Screen.Otp.route) {
            OtpScreen(authService = authService) { email ->
                scope.launch { prefs.setAuthenticatedEmail(email) }
                draftUser = draftUser.copy(email = email)
                if (!registrationMode && onboardingDone) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.RegStep1.route)
                }
            }
        }
        composable(Screen.RegStep1.route) {
            RegStep1Screen(initialEmail = draftUser.email) {
                    name, age, gender, email, state, district, city, pincode ->
                draftUser = draftUser.copy(
                    name = name,
                    age = age,
                    gender = gender,
                    email = email,
                    state = state,
                    district = district,
                    city = city,
                    pincode = pincode
                )
                navController.navigate(Screen.RegStep2.route)
            }
        }
        composable(Screen.RegStep2.route) {
            RegStep2Screen {
                    condition, antibiotic, dose, frequency, startDate, endDate, timesCsv ->
                val durationDays = (((endDate - startDate) / 86_400_000L) + 1).toInt()
                draftUser = draftUser.copy(
                    condition = condition,
                    antibiotic = antibiotic,
                    dose = dose,
                    frequency = frequency,
                    durationDays = durationDays,
                    startDateMillis = startDate,
                    endDateMillis = endDate,
                    doseTimesCsv = timesCsv
                )
                navController.navigate(Screen.RegStep3.route)
            }
        }
        composable(Screen.RegStep3.route) {
            RegStep3Screen { doctorName, hospital, hospitalLocation ->
                val completedUser = draftUser.copy(
                    doctorName = doctorName,
                    hospitalName = hospital,
                    hospitalLocation = hospitalLocation
                )
                scope.launch {
                    db.userDao().saveUser(completedUser)
                    val initialMedication = MedicationEntity(
                        name = completedUser.antibiotic,
                        dose = completedUser.dose,
                        frequency = completedUser.frequency,
                        startDateMillis = completedUser.startDateMillis,
                        endDateMillis = completedUser.endDateMillis,
                        doseTimesCsv = completedUser.doseTimesCsv
                    )
                    val medicationId = db.medicationDao().insert(initialMedication)
                    prefs.setOnboardingDone(true)
                    ReminderScheduler.scheduleCourses(
                        context.applicationContext,
                        completedUser,
                        listOf(initialMedication.copy(id = medicationId))
                    )
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Language.route) { inclusive = true }
                    }
                }
            }
        }
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(
                    UserRepository(db.userDao()),
                    MedicationRepository(db.medicationDao()),
                    DoseRepository(db.doseLogDao())
                )
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateTo = navController::navigate
            )
        }

        listOf(Screen.Diary, Screen.MyMeds, Screen.MedPlanner, Screen.TakeDose).forEach { screen ->
            composable(screen.route) {
                val user by db.userDao().getUser()
                    .collectAsStateWithLifecycle(initialValue = null)
                val logs by db.doseLogDao().getAllLogs()
                    .collectAsStateWithLifecycle(initialValue = emptyList())
                val medications by db.medicationDao().getAll()
                    .collectAsStateWithLifecycle(initialValue = emptyList())
                val doseRepository = remember { DoseRepository(db.doseLogDao()) }
                MedicationDiaryScreen(
                    user = user,
                    medications = medications,
                    logs = logs,
                    onBack = navController::popBackStack,
                    onSave = ::saveMedicationAndReschedule,
                    onDelete = ::deleteMedicationAndReschedule,
                    onMarkDose = { medication, label, dayStart, status ->
                        scope.launch {
                            doseRepository.logDoseForDay(
                                medication.id,
                                medication.name,
                                label,
                                dayStart,
                                status
                            )
                        }
                    }
                )
            }
        }

        composable(Screen.Settings.route) {
            val user by db.userDao().getUser()
                .collectAsStateWithLifecycle(initialValue = null)
            SettingsScreen(
                user = user,
                onBack = navController::popBackStack,
                onSave = ::saveUserAndReschedule,
                onTestReminder = {
                    NotificationHelper.showTestReminder(context.applicationContext)
                },
                onOpenMedicationDiary = { navController.navigate(Screen.Diary.route) },
                onSignOut = {
                    scope.launch {
                        prefs.clearAuthenticatedEmail()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Adr.route) {
            val user by db.userDao().getUser()
                .collectAsStateWithLifecycle(initialValue = null)
            val reports by db.adrReportDao().getAll()
                .collectAsStateWithLifecycle(initialValue = emptyList())
            AdrScreen(
                user = user,
                reports = reports,
                onBack = navController::popBackStack,
                onSubmit = { report ->
                    scope.launch { db.adrReportDao().insert(report) }
                }
            )
        }

        composable(Screen.Progress.route) {
            val user by db.userDao().getUser()
                .collectAsStateWithLifecycle(initialValue = null)
            val logs by db.doseLogDao().getAllLogs()
                .collectAsStateWithLifecycle(initialValue = emptyList())
            val medications by db.medicationDao().getAll()
                .collectAsStateWithLifecycle(initialValue = emptyList())
            ProgressScreen(
                user = user,
                medications = medications,
                logs = logs,
                onBack = navController::popBackStack
            )
        }

        composable(Screen.Learn.route) {
            val user by db.userDao().getUser()
                .collectAsStateWithLifecycle(initialValue = null)
            val medications by db.medicationDao().getAll()
                .collectAsStateWithLifecycle(initialValue = emptyList())
            LearnScreen(
                user = user,
                medications = medications,
                onBack = navController::popBackStack
            )
        }

        composable(Screen.Ask.route) {
            val user by db.userDao().getUser()
                .collectAsStateWithLifecycle(initialValue = null)
            val medications by db.medicationDao().getAll()
                .collectAsStateWithLifecycle(initialValue = emptyList())
            AskAuraScreen(
                user = user,
                medications = medications,
                onBack = navController::popBackStack
            )
        }
    }
}
