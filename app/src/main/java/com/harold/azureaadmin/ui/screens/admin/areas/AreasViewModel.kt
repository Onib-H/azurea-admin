package com.harold.azureaadmin.ui.screens.admin.areas

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.Area
import com.harold.azureaadmin.data.models.AreaDetail
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AreasViewModel @Inject constructor(
    private val repository: AdminRepository
): ViewModel() {

    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _selectedArea = MutableStateFlow<AreaDetail?>(null)
    val selectedArea: StateFlow<AreaDetail?> = _selectedArea

    fun fetchAreas() {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                _areas.value = repository.getAreas()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unable to load areas"
            }

            _loading.value = false
        }
    }


    fun addArea(
        name: String,
        description: String,
        capacity: Int,
        pricePerHour: String,
        discountPercent: Int,
        images: List<Uri>,
        context: Context
    ) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                val newArea = repository.addArea(
                    name = name,
                    description = description,
                    capacity = capacity,
                    pricePerHour = pricePerHour,
                    discountPercent = discountPercent,
                    images = images,
                    context = context
                )
                _areas.value = _areas.value + newArea
                _error.value = null
                println("Add success: ${newArea.area_name}")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error adding area"
            } finally {
                _loading.value = false
            }
        }
    }

    fun showArea(areaId: Int) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                val areaDetail = repository.showArea(areaId)
                _selectedArea.value = areaDetail
                _error.value = null
                println("Show success: ${areaDetail.area_name}")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error fetching area detail"
                println("Show error: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    // Updated editArea method with existingImages parameter
    fun editArea(
        areaId: Int,
        name: String,
        description: String,
        capacity: Int,
        status: String,
        pricePerHour: String,
        discountPercent: Int,
        images: List<Uri>,
        existingImages: List<String>,
        context: Context
    ) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                // Validate discount_percent before sending
                if (discountPercent < 0 || discountPercent > 99) {
                    _error.value = "Discount must be between 0 and 99"
                    _loading.value = false
                    return@launch
                }

                val editedArea = repository.editArea(
                    areaId = areaId,
                    name = name,
                    description = description,
                    capacity = capacity,
                    status = status,
                    pricePerHour = pricePerHour,
                    discountPercent = discountPercent,
                    images = images,
                    existingImages = existingImages,
                    context = context
                )
                _areas.value = _areas.value.map { if (it.id == areaId) editedArea else it }
                _error.value = null
                println("Edit success: ${editedArea.area_name}")
            } catch (e: Exception) {
                // Handle specific error for maintenance status with active bookings
                val errorMessage = e.localizedMessage ?: "Error editing area"
                _error.value = when {
                    errorMessage.contains("Cannot change status to maintenance") ->
                        "Cannot set area to maintenance - there are active or reserved bookings"
                    errorMessage.contains("Discount must be between") ->
                        "Discount must be between 0 and 99"
                    else -> errorMessage
                }
                println("Edit error: ${_error.value}")
            } finally {
                _loading.value = false
            }
        }
    }


    fun deleteArea(id: Int) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                val message = repository.deleteArea(id)
                _areas.value = _areas.value.filterNot { it.id == id }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error deleting area"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearAreaDetail() {
        _selectedArea.value = null
    }


}
