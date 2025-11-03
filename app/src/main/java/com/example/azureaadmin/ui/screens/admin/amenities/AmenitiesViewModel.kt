package com.example.azureaadmin.ui.screens.admin.amenities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.Amenity
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AmenitiesViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _amenities = MutableStateFlow<List<Amenity>>(emptyList())
    val amenities: StateFlow<List<Amenity>> = _amenities

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchAmenities() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _amenities.value = repository.getAmenities()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }


    fun addAmenity(description: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = repository.addAmenity(description)
                if (res.isSuccessful) {
                    fetchAmenities() // refresh list
                } else {
                    _error.value = "Failed to add amenity: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error adding amenity"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAmenity(id: Int, description: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = repository.editAmenity(id, description)
                if (res.isSuccessful) {
                    fetchAmenities()
                } else {
                    _error.value = "Failed to update amenity: ${res.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error updating amenity"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteAmenity(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val message = repository.deleteAmenity(id)
                // Re-fetch amenities so UI updates
                _amenities.value = repository.getAmenities()
                _error.value = null
                println("Delete success: $message")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error deleting amenity"
            } finally {
                _loading.value = false
            }
        }
    }



}
