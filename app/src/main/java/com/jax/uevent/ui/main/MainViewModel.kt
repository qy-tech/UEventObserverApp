package com.jax.uevent.ui.main

import android.os.UEventObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val _wgNumber = MutableLiveData("off")
    private val _uartNumber = MutableLiveData("off")
    private val _uEventMessage = MutableLiveData("")
    val wgNumber: LiveData<String> = _wgNumber
    val uartNumber: LiveData<String> = _uartNumber
    val uEventMessage: LiveData<String> = _uEventMessage


    fun startObserverUEvent() {
        uEventObserver.startObserving(WG_MATCH)
        uEventObserver.startObserving(UART_MATCH)
    }

    private val uEventObserver = object : UEventObserver() {
        override fun onUEvent(uEvnet: UEvent?) {
            if (uEvnet == null) return
            val result = uEvnet.get(SUBSYSTEM) ?: ""
            Timber.d("onUEvent date: 2019-12-25 result: $result ")
            viewModelScope.launch(Dispatchers.Main) {
                _uEventMessage.value = uEvnet.toString()
                if (result.startsWith("serio")) {
                    //串口
                    _wgNumber.value = result.replace("serio", "")
                } else if (result.startsWith("misc")) {
                    //韦根
                    _uartNumber.value = result.replace("misc", "")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        uEventObserver.stopObserving()
    }

    companion object {
        const val SUBSYSTEM = "SUBSYSTEM"
        const val WG_MATCH = "DEVPATH=/devices/virtual/misc/wiegand_dev"
        const val UART_MATCH = "DEVPATH=/devices/virtual/tty/ttysWK2/serio0"
    }

}
