package proj.stocks.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import proj.stocks.util.CurrencyDataCBR
import proj.stocks.util.DATABASE_NAME


@Dao
interface ResponseCBR_DAO {

    @Query("SELECT * FROM $DATABASE_NAME")
    fun getAll(): List<CurrencyDataCBR>

    @Insert
    fun insertAll(currency: ArrayList<CurrencyDataCBR>)

    @Query("SELECT * FROM $DATABASE_NAME WHERE isFavourite = 1")
    fun getFavouriteList(): List<CurrencyDataCBR>


    @Query("SELECT * FROM $DATABASE_NAME WHERE currId = :currId")
    fun getCurrency(currId: String): CurrencyDataCBR

    @Update
    fun updateCurrency(currency: CurrencyDataCBR)

    @Query("DELETE FROM $DATABASE_NAME")
    fun deleteAll()


}