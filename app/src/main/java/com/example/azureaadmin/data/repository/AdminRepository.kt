package com.example.azureaadmin.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.azureaadmin.data.models.AdminLoginRequest
import com.example.azureaadmin.data.models.AdminLoginResponse
import com.example.azureaadmin.data.models.AdminStatsResponse
import com.example.azureaadmin.data.models.Amenity
import com.example.azureaadmin.data.models.AmenityRequest
import com.example.azureaadmin.data.models.Area
import com.example.azureaadmin.data.models.AreaDetail
import com.example.azureaadmin.data.models.BookingDetailsResponse
import com.example.azureaadmin.data.models.BookingResponse
import com.example.azureaadmin.data.models.BookingStatusCounts
import com.example.azureaadmin.data.models.BookingStatusResponse
import com.example.azureaadmin.data.models.MessageResponse
import com.example.azureaadmin.data.models.RecordPaymentRequest
import com.example.azureaadmin.data.models.RecordPaymentResponse
import com.example.azureaadmin.data.models.Room
import com.example.azureaadmin.data.models.RoomDetail
import com.example.azureaadmin.data.models.UpdateBookingStatusRequest
import com.example.azureaadmin.data.models.UpdateBookingStatusResponse
import com.example.azureaadmin.data.models.UserAuthResponse
import com.example.azureaadmin.data.models.UsersResponse
import com.example.azureaadmin.data.remote.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar

class AdminRepository(private val context: Context) {

    private val api = RetrofitInstance.getApi(context)

    // Authentication

    suspend fun login(email: String, password: String): Response<AdminLoginResponse> {
        return api.login(AdminLoginRequest(email, password))
    }

    suspend fun logout(): Response<Void> {
        return api.logout()
    }

    suspend fun checkSession(): Response<UserAuthResponse> {
        return api.checkSession()
    }


    // BOOKING

    suspend fun getBookings(
        page: Int = 1,
        pageSize: Int = 9,
        status: String? = null
    ): Response<BookingResponse> {
        return api.getBookings(page, pageSize, status)
    }

    suspend fun getBookingDetails(bookingId: Int): Response<BookingDetailsResponse> {
        return api.getBookingDetails(bookingId)
    }

    // Update booking status
    suspend fun updateBookingStatus(
        bookingId: Int,
        request: UpdateBookingStatusRequest
    ): Response<UpdateBookingStatusResponse> {
        return api.updateBookingStatus(bookingId, request)
    }

    suspend fun recordPayment(
        bookingId: Int,
        amount: Double
    ): Response<RecordPaymentResponse> {
        val request = RecordPaymentRequest(
            amount = amount,
            transaction_type = "remaining_balance"
        )
        return api.recordPayment(bookingId, request)
    }


    // AREAS

    suspend fun getAreas(): List<Area> {
        val response = api.fetchAreas()
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Error fetching areas: ${response.code()}")
        }
    }

    suspend fun showArea(areaId: Int): AreaDetail {
        val response = api.fetchAreaDetail(areaId)
        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Area detail not found")
        } else {
            throw Exception("Error fetching area detail: ${response.code()}")
        }
    }

    suspend fun addArea(
        name: String,
        description: String,
        capacity: Int,
        pricePerHour: String,
        discountPercent: Int,
        images: List<Uri>?,
        context: Context
    ): Area {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val capacityPart = capacity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = pricePerHour.toRequestBody("text/plain".toMediaTypeOrNull())
        val discountPart = discountPercent.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imageParts = images?.mapNotNull { uri ->
            val file = createFileFromUri(context, uri)
            file?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", it.name, reqFile)
            }
        } ?: emptyList()

        val response = api.addAreaMultipart(
            areaName = namePart,
            description = descPart,
            capacity = capacityPart,
            pricePerHour = pricePart,
            discountPercent = discountPart,
            images = imageParts
        )

        images?.forEach { uri -> createFileFromUri(context, uri)?.delete() }

        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Empty response when adding area")
        } else {
            throw Exception("Error adding area: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }

    suspend fun editArea(
        areaId: Int,
        name: String,
        description: String,
        capacity: Int,
        status: String,
        pricePerHour: String,
        discountPercent: Int,
        images: List<Uri>?,
        existingImages: List<String>?,
        context: Context
    ): Area {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val capacityPart = capacity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val statusPart = status.lowercase().toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = pricePerHour.toRequestBody("text/plain".toMediaTypeOrNull())
        val discountPart = discountPercent.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imageParts = images?.mapNotNull { uri ->
            val file = createFileFromUri(context, uri)
            file?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", it.name, reqFile)
            }
        } ?: emptyList()

        val existingImageParts = existingImages?.map { url ->
            url.toRequestBody("text/plain".toMediaTypeOrNull())
        }?.toTypedArray() ?: emptyArray()

        val response = api.editAreaMultipart(
            areaId = areaId,
            areaName = namePart,
            description = descPart,
            capacity = capacityPart,
            status = statusPart,
            pricePerHour = pricePart,
            discountPercent = discountPart,
            images = imageParts,
            existingImages = existingImageParts
        )

        images?.forEach { uri -> createFileFromUri(context, uri)?.delete() }

        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Empty response when editing area")
        } else {
            throw Exception("Error editing area: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }

    suspend fun deleteArea(areaId: Int): String {
        val response = api.deleteArea(areaId)
        if (response.isSuccessful) {
            return response.body()?.message ?: "Area deleted"
        } else {
            throw Exception("Error deleting area: ${response.code()}")
        }
    }

    // ROOMS

    suspend fun getRooms(): List<Room> {
        val response = api.fetchRooms()
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Error fetching rooms: ${response.code()}")
        }
    }

    suspend fun showRoom(roomId: Int): RoomDetail {
        val response = api.fetchRoomDetail(roomId)
        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Room detail not found")
        } else {
            throw Exception("Error fetching room detail: ${response.code()}")
        }
    }

    suspend fun addRoom(
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
    ): Room {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val typePart = roomType.toRequestBody("text/plain".toMediaTypeOrNull())
        val bedPart = bedType.toRequestBody("text/plain".toMediaTypeOrNull())
        val guestsPart = maxGuests.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val discountPart = discountPercent.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val amenityParts = amenityIds.map {
            MultipartBody.Part.createFormData("amenities", it.toString())
        }

        val imageParts = images?.mapNotNull { uri ->
            val file = createFileFromUri(context, uri)
            file?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", it.name, reqFile)
            }
        } ?: emptyList()

        val response = api.addRoomMultipart(
            roomName = namePart,
            roomType = typePart,
            bedType = bedPart,
            maxGuests = guestsPart,
            roomPrice = pricePart,
            description = descPart,
            discountPercent = discountPart,
            amenities = amenityParts,
            images = imageParts
        )

        images?.forEach { uri -> createFileFromUri(context, uri)?.delete() }

        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Empty response when adding room")
        } else {
            throw Exception("Error adding room: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }

    suspend fun editRoom(
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
    ): Room {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val typePart = roomType.toRequestBody("text/plain".toMediaTypeOrNull())
        val bedPart = bedType.toRequestBody("text/plain".toMediaTypeOrNull())
        val guestsPart = maxGuests.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val discountPart = discountPercent.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val statusPart = status.lowercase().toRequestBody("text/plain".toMediaTypeOrNull())

        val amenityParts = amenityIds.map {
            MultipartBody.Part.createFormData("amenities", it.toString())
        }

        val imageParts = newImages?.mapNotNull { uri ->
            val file = createFileFromUri(context, uri)
            file?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("images", it.name, reqFile)
            }
        } ?: emptyList()

        val existingImageParts = existingImages?.map {
            it.toRequestBody("text/plain".toMediaTypeOrNull())
        }?.toTypedArray() ?: emptyArray()

        val response = api.editRoomMultipart(
            roomId = roomId,
            roomName = namePart,
            roomType = typePart,
            bedType = bedPart,
            maxGuests = guestsPart,
            roomPrice = pricePart,
            description = descPart,
            discountPercent = discountPart,
            status = statusPart,
            amenities = amenityParts,
            images = imageParts,
            existingImages = existingImageParts
        )

        newImages?.forEach { uri -> createFileFromUri(context, uri)?.delete() }

        if (response.isSuccessful) {
            return response.body()?.data ?: throw Exception("Empty response when editing room")
        } else {
            throw Exception("Error editing room: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }

    suspend fun deleteRoom(roomId: Int): String {
        val response = api.deleteRoom(roomId)
        if (response.isSuccessful) {
            return response.body()?.message ?: "Room deleted"
        } else {
            throw Exception("Error deleting room: ${response.code()}")
        }
    }

    // AMENITIES

    suspend fun getAmenities(): List<Amenity> {
        val response = api.fetchAmenities()
        if (response.isSuccessful) {
            return response.body()?.data ?: emptyList()
        } else {
            throw Exception("Error fetching amenities: ${response.code()}")
        }
    }

    suspend fun addAmenity(description: String): Response<MessageResponse> {
        return api.addAmenity(AmenityRequest(description))
    }

    suspend fun editAmenity(id: Int, description: String): Response<MessageResponse> {
        return api.editAmenity(id, AmenityRequest(description))
    }

    suspend fun deleteAmenity(amenityId: Int): String {
        val response = api.deleteAmenity(amenityId)
        if (response.isSuccessful) {
            return response.body()?.message ?: "Amenity deleted"
        } else {
            throw Exception("Error deleting amenity: ${response.code()}")
        }
    }

    // DASHBOARD

    suspend fun getStats(): Response<AdminStatsResponse> {
        return api.getStats()
    }

    suspend fun getBookingStatusCounts(month: Int, year: Int): Response<BookingStatusResponse> {
        return api.getBookingStatusCounts(month, year)
    }

    suspend fun getBookingStatusCountsForCurrentMonth(): Response<BookingStatusResponse> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return getBookingStatusCounts(month, year)
    }

    fun mapBookingStatusToList(response: BookingStatusResponse): List<BookingStatusCounts> {
        val total = response.pending + response.reserved + response.checked_in +
                response.checked_out + response.cancelled + response.no_show + response.rejected

        return listOf(
            BookingStatusCounts("Pending", response.pending, if (total > 0) (response.pending.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("Reserved", response.reserved, if (total > 0) (response.reserved.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("Checked In", response.checked_in, if (total > 0) (response.checked_in.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("Checked Out", response.checked_out, if (total > 0) (response.checked_out.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("Cancelled", response.cancelled, if (total > 0) (response.cancelled.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("No Show", response.no_show, if (total > 0) (response.no_show.toFloat() / total) * 100 else 0f),
            BookingStatusCounts("Rejected", response.rejected, if (total > 0) (response.rejected.toFloat() / total) * 100 else 0f)
        )
    }

    // Users

    suspend fun fetchAllUsers(page: Int, pageSize: Int): Response<UsersResponse> {
        return api.fetchAllUsers(page, pageSize)
    }

    suspend fun archiveUser(userId: Int): Response<MessageResponse> {
        return api.archiveUser(userId)
    }

    // Archived Users

    suspend fun fetchAllArchivedUsers(page: Int, pageSize: Int): Response<UsersResponse> {
        return api.fetchAllArchivedUsers(page, pageSize)
    }

    suspend fun restoreUser(userId: Int): Response<MessageResponse> {
        return api.restoreUser(userId)
    }

    // FILE HELPERS

    private fun createFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let { stream ->
                val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
                val tempFile = File(context.cacheDir, fileName)
                val outputStream = FileOutputStream(tempFile)
                stream.copyTo(outputStream)
                outputStream.close()
                stream.close()
                tempFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName ?: "image_${System.currentTimeMillis()}.jpg"
    }
}
