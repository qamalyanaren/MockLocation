package am.solidy.mocklocation

import am.solidy.mocklocation.data.MockLocationRepository
import am.solidy.mocklocation.data.model.MockLocation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MockLocationRepository
) : ViewModel() {

    private val _mockLocation = MutableStateFlow<MockLocation?>(null)
    val mockLocation = _mockLocation.asStateFlow()

    init {
        repository.fetchMockLocation()
            .onEach {
                _mockLocation.value = it
            }
            .catch {

            }
            .launchIn(viewModelScope)
    }
}