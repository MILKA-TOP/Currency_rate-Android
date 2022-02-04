package proj.stocks.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import proj.stocks.util.CurrencyDataCBR
import proj.stocks.util.DATABASE_NAME


@Database(entities = [CurrencyDataCBR::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun responseDao(): ResponseCBR_DAO

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                return INSTANCE!!
            }
        }
    }
}


