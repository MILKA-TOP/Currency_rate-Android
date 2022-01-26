package proj.stocks

import android.app.Application
import androidx.room.Room
import com.jakewharton.threetenabp.AndroidThreeTen
import proj.stocks.cbr_conntecion.StockCBRService
import proj.stocks.database.AppDatabase
import proj.stocks.util.DATABASE_NAME
import proj.stocks.util.SCRIPT_CBR_LINK
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jaxb.JaxbConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class AppMain : Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        retrofitConnection()

        dataBaseInfo = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    }

    private fun retrofitConnection() {
        val mRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCRIPT_CBR_LINK)
            //.addConverterFactory(JaxbConverterFactory.create())
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