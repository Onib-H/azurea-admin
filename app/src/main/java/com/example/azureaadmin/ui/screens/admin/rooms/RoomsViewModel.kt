package com.example.azureaadmin.ui.screens.admin.rooms

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.Room
import com.example.azureaadmin.data.models.RoomDetail
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomsViewModel(private val repository: AdminRepository) : ViewModel() {

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
            _loading.value = true
            try {
                _rooms.value = repository.getRooms()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _loading.value = false
            }
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
            _loading.value = true
            try {
                repository.addRoom(
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

                // ✅ Refresh by calling fetchRooms()
                fetchRooms()

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
            _loading.value = true
            try {
                repository.editRoom(
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

                // ✅ Refresh
                fetchRooms()

                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error editing room"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRoom(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val message = repository.deleteRoom(id)

                // ✅ Refresh
                fetchRooms()

                _error.value = null
                println("Delete success: $message")
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error deleting room"
            } finally {
                _loading.value = false
            }
        }
    }


    fun fetchRoomDetail(roomId: Int) {
        viewModelScope.launch {
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
