package com.harold.azureaadmin.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.harold.azureaadmin.data.models.AdminLoginRequest
import com.harold.azureaadmin.data.models.AdminLoginResponse
import com.harold.azureaadmin.data.models.AdminStatsResponse
import com.harold.azureaadmin.data.models.Amenity
import com.harold.azureaadmin.data.models.AmenityRequest
import com.harold.azureaadmin.data.models.ApproveIdRequest
import com.harold.azureaadmin.data.models.ApproveIdResponse
import com.harold.azureaadmin.data.models.Area
import com.harold.azureaadmin.data.models.AreaBookingResponse
import com.harold.azureaadmin.data.models.AreaDetail
import com.harold.azureaadmin.data.models.AreaRevenueResponse
import com.harold.azureaadmin.data.models.BookingDetailsResponse
import com.harold.azureaadmin.data.models.BookingResponse
import com.harold.azureaadmin.data.models.BookingStatusCounts
import com.harold.azureaadmin.data.models.BookingStatusResponse
import com.harold.azureaadmin.data.models.DailyBookingsResponse
import com.harold.azureaadmin.data.models.DailyCancellationsResponse
import com.harold.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.harold.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.harold.azureaadmin.data.models.DailyRevenueResponse
import com.harold.azureaadmin.data.models.MessageResponse
import com.harold.azureaadmin.data.models.MonthlyReportResponse
import com.harold.azureaadmin.data.models.RecordPaymentRequest
import com.harold.azureaadmin.data.models.RecordPaymentResponse
import com.harold.azureaadmin.data.models.RejectIdRequest
import com.harold.azureaadmin.data.models.RejectIdResponse
import com.harold.azureaadmin.data.models.Room
import com.harold.azureaadmin.data.models.RoomBookingResponse
import com.harold.azureaadmin.data.models.RoomDetail
import com.harold.azureaadmin.data.models.RoomRevenueResponse
import com.harold.azureaadmin.data.models.UpdateBookingStatusRequest
import com.harold.azureaadmin.data.models.UpdateBookingStatusResponse
import com.harold.azureaadmin.data.models.UserAuthResponse
import com.harold.azureaadmin.data.models.UsersResponse
import com.harold.azureaadmin.data.remote.AdminApiService
import jakarta.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AdminRepository @Inject constructor(
    private val context: Context,
    private val api: AdminApiService
) {


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

    // DASHBOARD

    suspend fun getStats(month: Int, year: Int): Response<AdminStatsResponse> {
        return api.getStats(month, year)
    }


    suspend fun getBookingStatusCounts(month: Int, year: Int): Response<BookingStatusResponse> {
        return api.getBookingStatusCounts(month, year)
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

    suspend fun getDailyRevenue(month: Int, year: Int): Response<DailyRevenueResponse> {
        return api.getDailyRevenue(month, year)
    }

    suspend fun getDailyBookings(month: Int, year: Int): Response<DailyBookingsResponse> {
        return api.getDailyBookings(month, year)
    }

    suspend fun getDailyCancellations(month: Int, year: Int): Response<DailyCancellationsResponse> {
        return api.getDailyCancellations(month, year)
    }

    suspend fun getDailyCheckinsCheckouts(month: Int, year: Int): Response<DailyCheckInCheckoutResponse> {
        return api.getDailyCheckinsCheckouts(month, year)
    }

    suspend fun getDailyNoShowRejected(month: Int, year: Int): Response<DailyNoShowRejectedResponse> {
        return api.getDailyNoShowRejected(month, year)
    }

    suspend fun getRoomRevenue(month: Int, year: Int): Response<RoomRevenueResponse>{
        return api.getRoomRevenue(month, year)
    }

    suspend fun getRoomBookings(month: Int, year: Int): Response<RoomBookingResponse>{
        return api.getRoomBookings(month, year)
    }

    suspend fun getAreaRevenue(month: Int, year: Int): Response<AreaRevenueResponse>{
        return api.getAreaRevenue(month, year)
    }

    suspend fun getAreaBookings(month: Int, year: Int): Response<AreaBookingResponse>{
        return api.getAreaBookings(month, year)
    }

    suspend fun getMonthlyReport(month: Int, year: Int ): Response<MonthlyReportResponse>{
        return api.getMonthlyReport(month, year)
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
                MultipartBody.Part.createFormData(
                    "images",
                    it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
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

        images?.forEach { uri ->
            createFileFromUri(context, uri)?.delete()
        }

        if (response.isSuccessful) {
            return response.body()?.data
                ?: throw Exception("Empty response when editing area")
        } else {
            val errorBody = response.errorBody()?.string()

            val errorMessage = try {
                val json = JSONObject(errorBody ?: "{}")
                json.optString(
                    "message",
                    json.optString("error", "Something went wrong")
                )
            } catch (e: Exception) {
                "Something went wrong"
            }

            throw Exception(errorMessage)
        }
    }


    suspend fun deleteArea(areaId: Int): String {
        val response = api.deleteArea(areaId)

        if (response.isSuccessful) {
            return response.body()?.message ?: "Area deleted"
        } else {
            // Read the error JSON coming from Django
            val errorBody = response.errorBody()?.string()

            val errorMessage = try {
                val json = JSONObject(errorBody ?: "{}")
                // backend uses either "message" or "error"
                json.optString("message", json.optString("error", "Something went wrong"))
            } catch (e: Exception) {
                "Something went wrong"
            }

            throw Exception(errorMessage)
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
            val errorBody = response.errorBody()?.string()

            val errorMessage = try {
                val json = JSONObject(errorBody ?: "{}")
                json.optString(
                    "message",
                    json.optString("error", "Something went wrong")
                )
            } catch (e: Exception) {
                "Something went wrong"
            }

            throw Exception(errorMessage)
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

    // Users

    suspend fun fetchAllUsers(page: Int, pageSize: Int): Response<UsersResponse> {
        return api.fetchAllUsers(page, pageSize)
    }

    suspend fun archiveUser(userId: Int): Response<MessageResponse> {
        return api.archiveUser(userId)
    }

    suspend fun approveValidId(userId: Int, isSeniorOrPwd: Boolean): Response<ApproveIdResponse> {
        return api.approveValidId(userId, ApproveIdRequest(isSeniorOrPwd))
    }

    suspend fun rejectValidId(userId: Int, reason: String): Response<RejectIdResponse> {
        return api.rejectValidId(userId, RejectIdRequest(reason))
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
