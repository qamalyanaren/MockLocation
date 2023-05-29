package am.solidy.mocklocation

import am.solidy.mocklocation.data.MockLocationRepository
import am.solidy.mocklocation.data.model.MockLocation
import am.solidy.mocklocation.data.payload.MockLocationPayload
import am.solidy.mocklocation.data.persistance.PrefManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MockLocationRepository,
    private val prefManager: PrefManager
) : ViewModel() {

    private val _param1 = MutableStateFlow(prefManager.param1)
    val param1 = _param1.asStateFlow()

    fun setParam1(param1: String) {
        _param1.value = param1
    }

    private val _param2 = MutableStateFlow(prefManager.param2)
    val param2 = _param2.asStateFlow()

    fun setParam2(param2: String) {
        _param2.value = param2
    }

    val isInputsValid: Flow<Boolean> =
        combine(
            param1, param2
        ) { param1, param2 ->
            param1.isNotEmpty() && param2.isNotEmpty()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)


    private val _mockLocation = Channel<MockLocation>(Channel.BUFFERED)
    val mockLocation = _mockLocation.receiveAsFlow()

    fun getMockLocation() {
        val payload = MockLocationPayload(
            param1 = param1.value,
            param2 = param2.value
        )
        repository.fetchMockLocation(
            payload = payload
        )
            .onEach {
                _mockLocation.send(it)
                prefManager.param1 = payload.param1
                prefManager.param2 = payload.param2
            }
            .catch {

            }
            .launchIn(viewModelScope)
    }
}