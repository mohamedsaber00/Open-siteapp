package com.msaber.openapiapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

const val AUTH_TOKEN_BUNDLE_KEY = "AuthToken"
@Parcelize
@Entity(
    tableName = "auth_token",
    foreignKeys = [
        ForeignKey(
        entity = AccountProperties::class,
        parentColumns = ["pk"],
        childColumns = ["account_pk"],
        onDelete = CASCADE
    )]
)
data class AuthToken(
    @PrimaryKey
    @ColumnInfo(name = "account_pk")
    var account_pk: Int? = -1,

    @SerializedName("token")
    @Expose
    @ColumnInfo(name = "token")
    var token: String? = null

) : Parcelable