package com.msaber.openapiapp.ui

import com.msaber.openapiapp.ui.DataState

interface DataStateChangeListener{

    fun onDataStateChange(dataState: DataState<*>?)

    fun expandAppBar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}