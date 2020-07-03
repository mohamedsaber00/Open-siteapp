package com.msaber.openapiapp.ui.main.account.state

import android.os.Parcelable
import com.msaber.openapiapp.model.AccountProperties
import kotlinx.android.parcel.Parcelize

const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "AccountViewState"

@Parcelize
class AccountViewState(

    var accountProperties: AccountProperties? = null

) : Parcelable