package com.df4l.liftaz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    //Pousser
    Muscle::class,
    Exercice::class,
    Seance::class,
    ExerciceSeance::class,
    SeanceHistorique::class,
    Serie::class,
    Elastique::class,
    //Manger
    Aliment::class,
    Diete::class,
    DieteElements::class,
    Recette::class,
    RecetteAliments::class,
    Repas::class,
    RepasElements::class
    //Stats
                     ], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    //Pousser
    abstract fun elastiqueDao(): ElastiqueDao
    abstract fun exerciceDao(): ExerciceDao
    abstract fun exerciceSeanceDao(): ExerciceSeanceDao
    abstract fun muscleDao(): MuscleDao
    abstract fun seanceDao(): SeanceDao
    abstract fun seanceHistoriqueDao(): SeanceHistoriqueDao
    abstract fun serieDao(): SerieDao

    //Manger
    abstract fun alimentDao(): AlimentDao
    abstract fun dieteDao(): DieteDao
    abstract fun dieteElementsDao(): DieteElementsDao
    abstract fun recetteDao(): RecetteDao
    abstract fun recetteAlimentsDao(): RecetteAlimentsDao
    abstract fun repasDao(): RepasDao
    abstract fun RepasElementsDao(): RepasElementsDao

    //Stats

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