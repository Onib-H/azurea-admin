package com.example.azureaadmin.data.remote

import com.example.azureaadmin.data.models.AdminLoginRequest
import com.example.azureaadmin.data.models.AdminLoginResponse
import com.example.azureaadmin.data.models.AdminStatsResponse
import com.example.azureaadmin.data.models.AmenityRequest
import com.example.azureaadmin.data.models.AmenityResponse
import com.example.azureaadmin.data.models.ApproveIdRequest
import com.example.azureaadmin.data.models.ApproveIdResponse
import com.example.azureaadmin.data.models.AreaBookingResponse
import com.example.azureaadmin.data.models.AreaDetailResponse
import com.example.azureaadmin.data.models.AreaResponse
import com.example.azureaadmin.data.models.AreaResponseSingle
import com.example.azureaadmin.data.models.AreaRevenueResponse
import com.example.azureaadmin.data.models.BookingDetailsResponse
import com.example.azureaadmin.data.models.BookingResponse
import com.example.azureaadmin.data.models.BookingStatusResponse
import com.example.azureaadmin.data.models.DailyBookingsResponse
import com.example.azureaadmin.data.models.DailyCancellationsResponse
import com.example.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.example.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.example.azureaadmin.data.models.DailyRevenueResponse
import com.example.azureaadmin.data.models.MessageResponse
import com.example.azureaadmin.data.models.MonthlyReportResponse
import com.example.azureaadmin.data.models.RecordPaymentRequest
import com.example.azureaadmin.data.models.RecordPaymentResponse
import com.example.azureaadmin.data.models.RejectIdRequest
import com.example.azureaadmin.data.models.RejectIdResponse
import com.example.azureaadmin.data.models.RoomBookingResponse
import com.example.azureaadmin.data.models.RoomDetailResponse
import com.example.azureaadmin.data.models.RoomResponse
import com.example.azureaadmin.data.models.RoomResponseSingle
import com.example.azureaadmin.data.models.RoomRevenueResponse
import com.example.azureaadmin.data.models.UpdateBookingStatusRequest
import com.example.azureaadmin.data.models.UpdateBookingStatusResponse
import com.example.azureaadmin.data.models.UserAuthResponse
import com.example.azureaadmin.data.models.UsersResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {

    // api
    @POST("api/auth/admin/login")
    suspend fun login(@Body request: AdminLoginRequest): Response<AdminLoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Void>

    @GET("api/auth/user")
    suspend fun checkSession(): Response<UserAuthResponse>


    // property

    @GET("property/amenities")
    suspend fun fetchAmenities(): Response<AmenityResponse>

    // master

    // Dashboard

    @GET("master/stats")
    suspend fun getStats(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<AdminStatsResponse>


    @GET("master/booking_status_counts")
    suspend fun getBookingStatusCounts(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<BookingStatusResponse>

    @GET("master/daily_revenue")
    suspend fun getDailyRevenue(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<DailyRevenueResponse>

    @GET("master/daily_bookings")
    suspend fun getDailyBookings(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<DailyBookingsResponse>

    @GET("master/daily_cancellations")
    suspend fun getDailyCancellations(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<DailyCancellationsResponse>

    @GET("master/daily_checkins_checkouts")
    suspend fun getDailyCheckinsCheckouts(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<DailyCheckInCheckoutResponse>

    @GET("master/daily_no_shows_rejected")
    suspend fun getDailyNoShowRejected(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<DailyNoShowRejectedResponse>

    @GET("master/room_revenue")
    suspend fun getRoomRevenue(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<RoomRevenueResponse>

    @GET("master/room_bookings")
    suspend fun getRoomBookings(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<RoomBookingResponse>

    @GET("master/area_revenue")
    suspend fun getAreaRevenue(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<AreaRevenueResponse>

    @GET("master/area_bookings")
    suspend fun getAreaBookings(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<AreaBookingResponse>

    @GET("master/generate_monthly_report")
    suspend fun getMonthlyReport(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<MonthlyReportResponse>

    // Booking

    @GET("master/bookings")
    suspend fun getBookings(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 9,
        @Query("status") status: String? = null
    ): Response<BookingResponse>

    @GET("master/booking/{booking_id}")
    suspend fun getBookingDetails(
        @Path("booking_id") bookingId: Int
    ): Response<BookingDetailsResponse>


    @PUT("master/booking/{booking_id}/status")
    suspend fun updateBookingStatus(
        @Path("booking_id") bookingId: Int,
        @Body request: UpdateBookingStatusRequest
    ): Response<UpdateBookingStatusResponse>

    @POST("master/booking/{booking_id}/payment")
    suspend fun recordPayment(
        @Path("booking_id") bookingId: Int,
        @Body request: RecordPaymentRequest
    ): Response<RecordPaymentResponse>

    // Areas

    @GET("master/areas")
    suspend fun fetchAreas(): Response<AreaResponse>

    @Multipart
    @POST("master/add_area")
    suspend fun addAreaMultipart(
        @Part("area_name") areaName: RequestBody,
        @Part("description") description: RequestBody,
        @Part("capacity") capacity: RequestBody,
        @Part("price_per_hour") pricePerHour: RequestBody,
        @Part("discount_percent") discountPercent: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<AreaResponseSingle>

    @GET("master/show_area/{id}")
    suspend fun fetchAreaDetail(@Path("id") id: Int): Response<AreaDetailResponse>

    @Multipart
    @PUT("master/edit_area/{id}")
    suspend fun editAreaMultipart(
        @Path("id") areaId: Int,
        @Part("area_name") areaName: RequestBody,
        @Part("description") description: RequestBody,
        @Part("capacity") capacity: RequestBody,
        @Part("status") status: RequestBody,
        @Part("price_per_hour") pricePerHour: RequestBody,
        @Part("discount_percent") discountPercent: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("existing_images") existingImages: Array<RequestBody> // Changed from List to Array
    ): Response<AreaResponseSingle>

    @DELETE("master/delete_area/{area_id}")
    suspend fun deleteArea(@Path("area_id") areaId: Int): Response<MessageResponse>

    // Rooms

    @GET("master/rooms")
    suspend fun fetchRooms(): Response<RoomResponse>

    @Multipart
    @POST("master/add_room")
    suspend fun addRoomMultipart(
        @Part("room_name") roomName: RequestBody,
        @Part("room_type") roomType: RequestBody,
        @Part("bed_type") bedType: RequestBody,
        @Part("max_guests") maxGuests: RequestBody,
        @Part("room_price") roomPrice: RequestBody,
        @Part("description") description: RequestBody,
        @Part("discount_percent") discountPercent: RequestBody,
        @Part amenities: List<MultipartBody.Part>, // ✅ Changed to MultipartBody.Part
        @Part images: List<MultipartBody.Part>
    ): Response<RoomResponseSingle>

    @GET("master/show_room/{id}")
    suspend fun fetchRoomDetail(@Path("id") id: Int): Response<RoomDetailResponse>

    @Multipart
    @PUT("master/edit_room/{id}")
    suspend fun editRoomMultipart(
        @Path("id") roomId: Int,
        @Part("room_name") roomName: RequestBody,
        @Part("room_type") roomType: RequestBody,
        @Part("bed_type") bedType: RequestBody,
        @Part("max_guests") maxGuests: RequestBody,
        @Part("room_price") roomPrice: RequestBody,
        @Part("description") description: RequestBody,
        @Part("discount_percent") discountPercent: RequestBody,
        @Part("status") status: RequestBody,
        @Part amenities: List<MultipartBody.Part>,
        @Part images: List<MultipartBody.Part>,
        @Part("existing_images") existingImages: Array<RequestBody>
    ): Response<RoomResponseSingle>

    @DELETE("master/delete_room/{room_id}")
    suspend fun deleteRoom(@Path("room_id") roomId: Int): Response<MessageResponse>


    // Amenities

    @POST("master/add_amenity")
    suspend fun addAmenity(@Body request: AmenityRequest): Response<MessageResponse>

    @PUT("master/edit_amenity/{id}")
    suspend fun editAmenity(
        @Path("id") id: Int,
        @Body request: AmenityRequest
    ): Response<MessageResponse>

    @DELETE("master/delete_amenity/{pk}")
    suspend fun deleteAmenity(@Path("pk") amenityId: Int): Response<MessageResponse>

    // Users
    @GET("master/users")
    suspend fun fetchAllUsers(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<UsersResponse>

    @PUT("master/archived_user/{user_id}")
    suspend fun archiveUser(@Path("user_id") userId: Int): Response<MessageResponse>

    @PUT("master/approve_valid_id/{user_id}")
    suspend fun approveValidId(
        @Path("user_id") userId: Int,
        @Body request: ApproveIdRequest
    ): Response<ApproveIdResponse>

    @PUT("master/reject_valid_id/{user_id}")
    suspend fun rejectValidId(
        @Path("user_id") userId: Int,
        @Body request: RejectIdRequest
    ): Response<RejectIdResponse>


    // Archived Users
    @GET("master/archived_users")
    suspend fun fetchAllArchivedUsers(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<UsersResponse>


    @POST("master/restore_user/{user_id}")
    suspend fun restoreUser(@Path("user_id") userId: Int): Response<MessageResponse>










}