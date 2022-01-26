package proj.stocks.database

import androidx.room.Database
import androidx.room.RoomDatabase
import proj.stocks.util.CurrencyDataCBR


@Database(entities = [CurrencyDataCBR::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun responseDao(): ResponseCBR_DAO?
}


