package com.mhealth.aura.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mhealth.aura.data.db.dao.*
import com.mhealth.aura.data.db.entity.*

@Database(
    entities = [
        UserEntity::class,
        MedicationEntity::class,
        DoseLogEntity::class,
        SymptomDiaryEntity::class,
        AdrReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun symptomDiaryDao(): SymptomDiaryDao
    abstract fun adrReportDao(): AdrReportDao

    companion object {
        @Volatile private var INSTANCE: AuraDatabase? = null

        fun getDatabase(context: Context): AuraDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AuraDatabase::class.java, "aura.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN city TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN endDateMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN doseTimesCsv TEXT NOT NULL DEFAULT '08:00,20:00'")
                db.execSQL("ALTER TABLE users ADD COLUMN doseRemindersEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN missedDoseAlertsEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN dailySummaryEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN preDoseMinutes INTEGER NOT NULL DEFAULT 15")
                db.execSQL("ALTER TABLE users ADD COLUMN missedDoseMinutes INTEGER NOT NULL DEFAULT 120")
                db.execSQL("ALTER TABLE users ADD COLUMN summaryHour INTEGER NOT NULL DEFAULT 21")
                db.execSQL("ALTER TABLE users ADD COLUMN summaryMinute INTEGER NOT NULL DEFAULT 30")
                db.execSQL(
                    "UPDATE users SET endDateMillis = startDateMillis + " +
                        "(CASE WHEN durationDays > 0 THEN durationDays - 1 ELSE 0 END) * 86400000 " +
                        "WHERE endDateMillis = 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS medications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        dose TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        startDateMillis INTEGER NOT NULL,
                        endDateMillis INTEGER NOT NULL,
                        doseTimesCsv TEXT NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO medications (
                        id, name, dose, frequency, startDateMillis, endDateMillis,
                        doseTimesCsv, isActive
                    )
                    SELECT 1, antibiotic, dose, frequency, startDateMillis, endDateMillis,
                        doseTimesCsv, 1
                    FROM users
                    WHERE antibiotic != ''
                    """.trimIndent()
                )
                db.execSQL(
                    "ALTER TABLE dose_logs ADD COLUMN medicationId INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE dose_logs ADD COLUMN medicationName TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    """
                    UPDATE dose_logs
                    SET medicationName = (
                        SELECT antibiotic FROM users WHERE users.id = 1
                    )
                    WHERE medicationName = ''
                    """.trimIndent()
                )
            }
        }
    }
}
