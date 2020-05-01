package com.msaber.openapiapp.ui

interface DataStateChangeListener {
    //any data state
    fun onDataStateChange(dataState: DataState<*>?)
}