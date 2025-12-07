package com.harold.azureaadmin.ui.screens.admin.amenities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.Amenity
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmenitiesViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _amenities = MutableStateFlow<List<Amenity>>(emptyList())
    val amenities: StateFlow<List<Amenity>> = _amenities

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private fun handleError(e: Exception) {
        _error.value = e.localizedMessage ?: "Unknown error"
    }

    fun fetchAmenities() {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                _amenities.value = repository.getAmenities()
            } catch (e: Exception) {
                handleError(e)
            }

            _loading.value = false
        }
    }

    fun addAmenity(description: String) {
        viewModelScope.launch {
            try {
                val res = repository.addAmenity(description)
                if (!res.isSuccessful) {
                    _error.value = "Failed: ${res.code()}"
                    return@launch
                }

                // local update instead of fetch
                fetchAmenities()

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun updateAmenity(id: Int, description: String) {
        viewModelScope.launch {
            try {
                val res = repository.editAmenity(id, description)
                if (!res.isSuccessful) {
                    _error.value = "Failed: ${res.code()}"
                    return@launch
                }

                fetchAmenities()

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun deleteAmenity(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteAmenity(id)
                fetchAmenities()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
