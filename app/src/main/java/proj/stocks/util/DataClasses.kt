package proj.stocks.util

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import kotlinx.parcelize.Parcelize
import org.threeten.bp.temporal.TemporalUnit

@Parcelize
@Entity(tableName = DATABASE_NAME)
@JacksonXmlRootElement(localName = "Valute")
data class CurrencyDataCBR(
    @PrimaryKey @field:JacksonXmlProperty(localName = "ID", isAttribute = true) var currId: String,
    @ColumnInfo(name = "NumCode") @field:JacksonXmlProperty(localName = "NumCode") var numCode: String?,
    @ColumnInfo(name = "CharCode") @field:JacksonXmlProperty(localName = "CharCode") var charCode: String?,
    @ColumnInfo(name = "Nominal") @field:JacksonXmlProperty(localName = "Nominal") var nominal: String?,
    @ColumnInfo(name = "Name") @field:JacksonXmlProperty(localName = "Name") var name: String?,
    @ColumnInfo(name = "Value") @field:JacksonXmlProperty(localName = "Value") var value: String?,
    @ColumnInfo(name = "isFavourite") var isFavourite: Boolean = false
) : Parcelable {
    constructor() : this("", null, null, null, null, null, false)

    fun sameEditText(s: String): Boolean {
        return currId.lowercase().contains(s) || name!!.lowercase()
            .contains(s) || charCode!!.lowercase().contains(s)

    }
}


@Parcelize
@JacksonXmlRootElement(localName = "ValCurs")
data class CurrencyListDailyCBR(
    @field:JacksonXmlProperty(localName = "Date", isAttribute = true) var date: String?,
    @field:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String?,

    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "Valute") var list: ArrayList<CurrencyDataCBR>?
) : Parcelable {
    constructor() : this(null, null, null)
}

@Parcelize
@JacksonXmlRootElement(localName = "Record")
data class CurrencyRecordCBR(
    @field:JacksonXmlProperty(localName = "Date", isAttribute = true) var date: String?,
    @field:JacksonXmlProperty(localName = "Id", isAttribute = true) var val_id: String?,
    @field:JacksonXmlProperty(localName = "Nominal") var nominal: String?,
    @field:JacksonXmlProperty(localName = "Value") var value: String?
) : Parcelable {
    constructor() : this(null, null, null, null)
}

@Parcelize
@JacksonXmlRootElement(localName = "ValCurs")
data class CurrencyListDynamicCBR(
    @field:JacksonXmlProperty(localName = "ID", isAttribute = true) var val_id: String?,
    @field:JacksonXmlProperty(localName = "DateRange1", isAttribute = true) var dateRange1: String?,
    @field:JacksonXmlProperty(localName = "DateRange2", isAttribute = true) var dateRange2: String?,
    @field:JacksonXmlProperty(localName = "name", isAttribute = true) var name: String?,

    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "Record") var list: ArrayList<CurrencyRecordCBR>?
) : Parcelable {
    constructor() : this(null, null, null, null, null)
}

data class GraphDynamicPoint(
    val date: String,
    val value: Float
)

data class DynamicPeriodMinus(
    val count: Long,
    val minusType: TemporalUnit,
    val bindId: Int
)


