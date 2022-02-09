@file:Suppress("DEPRECATION")

package proj.stocks

import android.app.Application
import com.ctc.wstx.stax.WstxInputFactory
import com.ctc.wstx.stax.WstxOutputFactory
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.jakewharton.threetenabp.AndroidThreeTen
import proj.stocks.model.database.AppDatabase
import proj.stocks.model.network.StockCBRService
import proj.stocks.util.SCRIPT_CBR_LINK
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory


class AppMain : Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        retrofitConnection()

    }

    private fun retrofitConnection() {
        val inputFactory: XMLInputFactory = WstxInputFactory()

        val outFactory: XMLOutputFactory = WstxOutputFactory()

        val xf: XmlFactory = XmlFactory.builder()
            .xmlInputFactory(inputFactory)
            .xmlOutputFactory(outFactory)
            .build()
        val mapper = XmlMapper(xf)


        val mRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SCRIPT_CBR_LINK)
            //.addConverterFactory(SimpleXmlConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(mapper))
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