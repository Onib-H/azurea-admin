package com.example.azureaadmin.ui.screens.admin.areas

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.Area
import com.example.azureaadmin.data.models.AreaDetail
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AreasViewModel(private val repository: AdminRepository) : ViewModel() {

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
            _loading.value = true
            try {
                _areas.value = repository.getAreas()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _loading.value = false
            }
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
                _areas.value = repository.getAreas() // refresh list
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
        existingImages: List<String>, // Added this parameter
        context: Context
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val editedArea = repository.editArea(
                    areaId = areaId,
                    name = name,
                    description = description,
                    capacity = capacity,
                    status = status,
                    pricePerHour = pricePerHour,
                    discountPercent = discountPercent,
                    images = images,
                    existingImages = existingImages, // Pass the existing images parameter
                    context = context
                )
                _areas.value = repository.getAreas() // refresh list
                _error.value = null
                println("Edit success: ${editedArea.area_name}")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error editing area"
                println("Edit error: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }


    fun deleteArea(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val message = repository.deleteArea(id)
                _areas.value = repository.getAreas()
                _error.value = null
                println("Delete success: $message")
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
