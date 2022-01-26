package proj.stocks.cbr_conntecion

import proj.stocks.util.CurrencyListDailyCBR
import proj.stocks.util.CurrencyListDynamicCBR
import retrofit2.http.GET
import retrofit2.http.Query


interface StockCBRService {

    @GET("XML_daily.asp")
    suspend fun getAllCurrenciesRU(): CurrencyListDailyCBR

    @GET("XML_daily_eng.asp")
    suspend fun getAllCurrenciesEN(): CurrencyListDailyCBR

    /*
    @GET("XML_daily.asp")
    suspend fun getByDate(@Query("date_req") date_req: String): CurrencyListDailyCBR*/

    @GET("XML_dynamic.asp")
    suspend fun getCurrencyDynamic(
        @Query("date_req1") date_req1: String,
        @Query("date_req2") date_req2: String,
        @Query("VAL_NM_RQ") val_id: String
    ): CurrencyListDynamicCBR

}