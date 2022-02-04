@file:Suppress("DEPRECATION")

package proj.stocks

import android.app.Application
import androidx.room.Room
import com.jakewharton.threetenabp.AndroidThreeTen
import proj.stocks.model.database.AppDatabase
import proj.stocks.model.network.StockCBRService
import proj.stocks.util.DATABASE_NAME
import proj.stocks.util.SCRIPT_CBR_LINK
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class AppMain : Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        retrofitConnection()

    }


    /* *
    *  Причины того, что я использую deprecated SimpleXmlConverterFactory:
    *  1) Note that JAXB does not work on Android:
    * [https://github.com/square/retrofit/blob/master/retrofit-converters/jaxb/README.md];
    *
    *  2) TikXml не может адаптироваться под заданную нам кодировку
    * (при Windows-1251, которая передается от CBR, выводятся "�");
    * */

    private fun retrofitConnection() {
        val mRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCRIPT_CBR_LINK)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
        responseService = mRetrofit.create(StockCBRService::class.java)
    }

    companion object {
        lateinit var responseService: StockCBRService
        lateinit var dataBaseInfo: AppDatabase
            private set
    }

}