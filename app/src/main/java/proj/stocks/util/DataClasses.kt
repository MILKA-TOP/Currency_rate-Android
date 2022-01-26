package proj.stocks.util

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.threeten.bp.temporal.TemporalUnit

@Parcelize
@Entity(tableName = DATABASE_NAME)
@Root(strict = false, name = "Valute")
data class CurrencyDataCBR(
    @PrimaryKey @field:Attribute(name = "ID") var currId: String,
    @ColumnInfo(name = "NumCode") @field:Element(name = "NumCode") var numCode: Int?,
    @ColumnInfo(name = "CharCode") @field:Element(name = "CharCode") var charCode: String?,
    @ColumnInfo(name = "Name") @field:Element(name = "Name") var name: String?,
    @ColumnInfo(name = "Value") @field:Element(name = "Value") var value: String?,
    @ColumnInfo(name = "Nominal") @field:Element(name = "Nominal") var nominal: String?,
    @ColumnInfo(name = "isFavourite") var isFavourite: Boolean = false
) : Parcelable {
    constructor() : this("", null, null, null, null, null, false)

    fun sameEditText(s: String): Boolean {
        return currId.lowercase().contains(s) || name!!.lowercase()
            .contains(s) || charCode!!.lowercase().contains(s)

    }
}


@Parcelize
@Root(strict = false, name = "ValCurs")
data class CurrencyListDailyCBR(
    @field:Attribute(name = "Date") var date: String?,
    @field:Attribute(name = "name") var name: String?,
    @field:ElementList(inline = true, entry = "Valute") var list: ArrayList<CurrencyDataCBR>?
) : Parcelable {
    constructor() : this(null, null, null)
}

@Parcelize
@Root(strict = false, name = "Record")
data class CurrencyRecordCBR(
    @field:Attribute(name = "Date") var date: String?,
    @field:Attribute(name = "Id") var val_id: String?,
    @field:Element(name = "Nominal") var nominal: String?,
    @field:Element(name = "Value") var value: String?
) : Parcelable {
    constructor() : this(null, null, null, null)
}

@Parcelize
@Root(strict = false, name = "ValCurs")
data class CurrencyListDynamicCBR(
    @field:Attribute(name = "name") var name: String?,
    @field:Attribute(name = "DateRange1") var dateRange1: String?,
    @field:Attribute(name = "DateRange2") var dateRange2: String?,
    @field:Attribute(name = "ID") var val_id: String?,
    @field:ElementList(inline = true, entry = "Record") var list: ArrayList<CurrencyRecordCBR>?
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


