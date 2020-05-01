package com.msaber.openapiapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    val TAG = "BaseViewModel"

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState : LiveData<ViewState>
    get() = _viewState

    val dataState : LiveData<DataState<ViewState>> = Transformations.switchMap(_stateEvent){stateEvent ->
        stateEvent.let {
            handleStateEvent(stateEvent)
        }

    }
    fun getCurrentViewStateOrNew(): ViewState{
        return viewState.value?.let<ViewState, ViewState> {it}?: initNewViewState()
    }


    fun setStateEvent(event: StateEvent){
        _stateEvent.value = event
    }


    abstract fun initNewViewState(): ViewState

    abstract fun handleStateEvent(stateEvent: StateEvent) :LiveData<DataState<ViewState>>

}