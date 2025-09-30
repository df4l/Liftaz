package com.df4l.liftaz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    Muscle::class,
    Exercice::class,
    Seance::class,
    ExerciceSeance::class,
    SeanceHistorique::class,
    Serie::class,
    Elastique::class
                     ], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun elastiqueDao(): ElastiqueDao
    abstract fun exerciceDao(): ExerciceDao
    abstract fun exerciceSeanceDao(): ExerciceSeanceDao
    abstract fun muscleDao(): MuscleDao
    abstract fun seanceDao(): SeanceDao
    abstract fun seanceHistoriqueDao(): SeanceHistoriqueDao
    abstract fun serieDao(): SerieDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return com.df4l.liftaz.data.AppDatabase.Companion.Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "liftaz_db")
                    /**
                     * Setting this option in your app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { com.df4l.liftaz.data.AppDatabase.Companion.Instance = it }
            }
        }
    }
}