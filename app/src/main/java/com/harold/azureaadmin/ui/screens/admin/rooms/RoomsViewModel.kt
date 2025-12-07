package com.harold.azureaadmin.ui.screens.admin.rooms

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.Room
import com.harold.azureaadmin.data.models.RoomDetail
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val repository: AdminRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _selectedRoomDetail = MutableStateFlow<RoomDetail?>(null)
    val selectedRoomDetail: StateFlow<RoomDetail?> = _selectedRoomDetail

    fun fetchRooms() {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            _error.value = null
            try {
                _rooms.value = repository.getRooms()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            }

            _loading.value = false

        }
    }

    fun addRoom(
        name: String,
        roomType: String,
        bedType: String,
        maxGuests: Int,
        price: Double,
        description: String,
        discountPercent: Int,
        amenityIds: List<Int>,
        images: List<Uri>?,
        context: Context
    ) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                val newRoom = repository.addRoom(
                    name = name,
                    roomType = roomType.lowercase(),
                    bedType = bedType.lowercase(),
                    maxGuests = maxGuests,
                    price = price,
                    description = description,
                    discountPercent = discountPercent,
                    amenityIds = amenityIds,
                    images = images,
                    context = context
                )

                _rooms.value = _rooms.value + newRoom


                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error adding room"
            } finally {
                _loading.value = false
            }
        }
    }

    fun editRoom(
        roomId: Int,
        name: String,
        roomType: String,
        bedType: String,
        maxGuests: Int,
        price: Double,
        description: String,
        discountPercent: Int,
        status: String,
        amenityIds: List<Int>,
        newImages: List<Uri>?,
        existingImages: List<String>?,
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

                val updated = repository.editRoom(
                    roomId = roomId,
                    name = name,
                    roomType = roomType.lowercase(),
                    bedType = bedType.lowercase(),
                    maxGuests = maxGuests,
                    price = price,
                    description = description,
                    discountPercent = discountPercent,
                    status = status,
                    amenityIds = amenityIds,
                    newImages = newImages,
                    existingImages = existingImages,
                    context = context
                )
                _rooms.value = _rooms.value.map { if (it.id == roomId) updated else it }
                _error.value = null
            } catch (e: Exception) {
                // Handle specific error for unavailable status with active bookings
                val errorMessage = e.localizedMessage ?: "Error editing room"
                _error.value = when {
                    errorMessage.contains("Cannot change status to unavailable") ->
                        "Cannot set room to unavailable - there are active or reserved bookings"
                    errorMessage.contains("Discount must be between") ->
                        "Discount must be between 0 and 99"
                    else -> errorMessage
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRoom(id: Int) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                val message = repository.deleteRoom(id)

                _rooms.value = _rooms.value.filterNot { it.id == id }
                _error.value = null

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error deleting room"
            } finally {
                _loading.value = false
            }
        }
    }


    fun fetchRoomDetail(roomId: Int) {
        viewModelScope.launch {
            if (_loading.value) return@launch
            _loading.value = true
            try {
                _selectedRoomDetail.value = repository.showRoom(roomId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error fetching room details"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearRoomDetail() {
        _selectedRoomDetail.value = null
    }

}
