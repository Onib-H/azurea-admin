package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.harold.azureaadmin.data.models.BookingDetails
import com.harold.azureaadmin.ui.components.button.DropdownField
import com.harold.azureaadmin.ui.theme.AzureaColors
import com.harold.azureaadmin.utils.FormatDate
import com.harold.azureaadmin.utils.FormatDateTimeLong
import com.harold.azureaadmin.utils.FormatTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime


sealed class BookingAction(val bookingId: Int) {
    class Reserve(bookingId: Int) : BookingAction(bookingId)
    class CheckIn(bookingId: Int) : BookingAction(bookingId)
    class CheckOut(bookingId: Int) : BookingAction(bookingId)
    class MarkNoShow(bookingId: Int) : BookingAction(bookingId)
}

enum class BookingActionType {
    RESERVE, CHECK_IN, CHECK_OUT, MARK_NO_SHOW, REJECT, CANCEL
}

@Composable
fun BookingDetailsDialog(
    bookingId: Int,
    viewModel: BookingViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val bookingDetails by viewModel.bookingDetails.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val statusUpdateMessage by viewModel.statusUpdateMessage.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf<BookingAction?>(null) }
    var isUpdatingStatus by remember { mutableStateOf(false) }
    var currentActionType by remember { mutableStateOf(BookingActionType.RESERVE) }

    LaunchedEffect(bookingId) {
        viewModel.clearStatusMessage()
        viewModel.clearError()
        viewModel.getBookingDetails(bookingId)
    }

    LaunchedEffect(statusUpdateMessage) {
        statusUpdateMessage?.let {
            delay(300)
            isUpdatingStatus = false
            viewModel.clearStatusMessage()
            onDismiss()
        }
    }

    BookingApprovalLoader(
        isLoading = isUpdatingStatus,
        actionType = currentActionType,
        onComplete = {}
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                BookingDetailsHeader(onDismiss)


                when {
                    loading -> LoadingState()
                    error != null -> ErrorState(
                        error = error,
                        onRetry = { viewModel.getBookingDetails(bookingId) }
                    )
                    bookingDetails != null -> {
                        BookingDetailsContent(
                            booking = bookingDetails!!,
                            onReserve = { amount ->
                                currentActionType = BookingActionType.RESERVE
                                isUpdatingStatus = true
                                viewModel.updateBookingStatus(
                                    bookingId = bookingDetails!!.id,
                                    newStatus = "reserved",
                                    downPayment = amount
                                )
                            },
                            onReject = { showRejectDialog = true },
                            onCancel = { showCancelDialog = true },
                            onCheckIn = { amount ->
                                currentActionType = BookingActionType.CHECK_IN
                                isUpdatingStatus = true
                                val totalPayment = (bookingDetails!!.down_payment ?: 0.0) + amount
                                if (amount > 0) {
                                    viewModel.recordPaymentAndCheckIn(bookingDetails!!.id, totalPayment)
                                } else {
                                    viewModel.updateBookingStatus(bookingDetails!!.id, "checked_in")
                                }
                            },
                            onCheckOut = {
                                showConfirmDialog = BookingAction.CheckOut(bookingDetails!!.id)
                            },
                            onMarkNoShow = {
                                showConfirmDialog = BookingAction.MarkNoShow(bookingDetails!!.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs (unchanged)
    if (showRejectDialog) {
        RejectBookingDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                currentActionType = BookingActionType.REJECT
                isUpdatingStatus = true
                viewModel.updateBookingStatus(
                    bookingId = bookingDetails!!.id,
                    newStatus = "rejected",
                    reason = reason,
                    setAvailable = true
                )
                showRejectDialog = false
            }
        )
    }

    if (showCancelDialog) {
        CancelBookingDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = { reason ->
                currentActionType = BookingActionType.CANCEL
                isUpdatingStatus = true
                viewModel.updateBookingStatus(
                    bookingId = bookingDetails!!.id,
                    newStatus = "cancelled",
                    reason = reason,
                    setAvailable = true
                )
                showCancelDialog = false
            }
        )
    }

    showConfirmDialog?.let { action ->
        ConfirmActionDialog(
            action = action,
            onDismiss = { showConfirmDialog = null },
            onConfirm = {
                when (action) {
                    is BookingAction.CheckOut -> {
                        currentActionType = BookingActionType.CHECK_OUT
                        isUpdatingStatus = true
                        viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "checked_out",
                            setAvailable = true
                        )
                    }
                    is BookingAction.MarkNoShow -> {
                        currentActionType = BookingActionType.MARK_NO_SHOW
                        isUpdatingStatus = true
                        viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "no_show",
                            setAvailable = true
                        )
                    }
                    else -> {}
                }
                showConfirmDialog = null
            }
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = Color(0xFF0066CC)
        )
    }
}

@Composable
private fun ErrorState(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error ?: "Error loading booking details",
            color = Color(0xFFD32F2F),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066CC))
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun BookingDetailsContent(
    booking: BookingDetails,
    onReserve: (Double) -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onCheckIn: (Double) -> Unit,
    onCheckOut: () -> Unit,
    onMarkNoShow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Guest Info Card - Clean design
        GuestInfoCard(booking)

        Spacer(Modifier.height(16.dp))

        PropertyInformationCard(booking)

        Spacer(Modifier.height(16.dp))

        BookingInformationCard(booking)


        Spacer(Modifier.height(16.dp))

        // Special Request if exists
        if (!booking.special_request.isNullOrBlank()) {
            SpecialRequestCard(booking.special_request)
            Spacer(Modifier.height(16.dp))
        }

        // Payment Summary
        PaymentSummaryCard(booking)

        Spacer(Modifier.height(16.dp))

        // Status-specific content
        when (booking.status.uppercase()) {
            "PENDING" -> PendingBookingSection(
                booking = booking,
                onReserve = onReserve,
                onReject = onReject
            )
            "RESERVED" -> ReservedBookingSection(
                booking = booking,
                onCheckIn = onCheckIn,
                onCancel = onCancel,
                onMarkNoShow = onMarkNoShow
            )
            "CHECKED_IN" -> CheckedInBookingSection(
                booking = booking,
                onCheckOut = onCheckOut
            )
            "CHECKED_OUT" -> CheckedOutBookingSection(booking)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun GuestInfoCard(booking: BookingDetails) {
    val user = booking.user
    val guestName = "${user.first_name} ${user.last_name}"
    val isVerified = user.is_verified == "verified"

    val ringColor = if (isVerified) Color(0xFF2E7D32) else Color(0xFFBDBDBD)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Guest Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            // Profile + Info Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Profile Image
                AsyncImage(
                    model = user.profile_image,
                    contentDescription = "Guest Image",
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .border(3.dp, ringColor, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                Column {

                    // Name + Verified check
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guestName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        if (isVerified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )
                }
            }
        }
    }
}


@Composable
private fun PropertyInformationCard(booking: BookingDetails) {

    val room = booking.room_details
    val area = booking.area_details

    val propertyName = room?.room_name ?: area?.area_name ?: "N/A"
    val isRoom = room != null
    val propertyType = if (isRoom) "ROOM" else "AREA"


    val imageUrl = room?.images?.firstOrNull()?.room_image
        ?: area?.images?.firstOrNull()?.area_image
        ?: ""

    val roomType = room?.room_type?.replaceFirstChar { it.uppercase() } ?: "—"
    val bedType = room?.bed_type?.replaceFirstChar { it.uppercase() } ?: "—"

    val maxGuests = room?.max_guests ?: area?.capacity ?: 0

    val originalPrice = booking.original_price?.let {
        "₱" + "%,.0f".format(it)
    } ?: room?.price_per_night?.let {
        "₱" + "%,.0f".format(it)
    } ?: "N/A"


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Property Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))


            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Property Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }


            Column {
                Text(
                    text = propertyName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )

                Spacer(Modifier.height(6.dp))
                PropertyChip(propertyType)
            }

            Spacer(Modifier.height(16.dp))


            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                if (isRoom) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Room Type", fontSize = 12.sp, color = Color(0xFF777777))
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = roomType,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bed Type", fontSize = 12.sp, color = Color(0xFF777777))
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = bedType,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max Guests", fontSize = 12.sp, color = Color(0xFF777777))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = maxGuests.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Original Price", fontSize = 12.sp, color = Color(0xFF777777))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = originalPrice,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}




@Composable
private fun BookingInformationCard(booking: BookingDetails) {

    val formattedCreatedAt = FormatDateTimeLong.format(booking.created_at ?: "")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF8A2BE2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Booking Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Status + Created At moved directly under header
            Column {

                // Status Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Status:",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.width(8.dp))
                    BookingStatusChip(status = booking.status)
                }

                Spacer(Modifier.height(6.dp))

                // Created At
                Text(
                    text = "Created: $formattedCreatedAt",
                    fontSize = 12.5.sp,
                    color = Color(0xFF777777)
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(12.dp))

            // Dates Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column(Modifier.weight(1f)) {
                    Text("Check-in", fontSize = 12.sp, color = Color(0xFF666666))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        FormatDate.format(booking.check_in_date),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text("Check-out", fontSize = 12.sp, color = Color(0xFF666666))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        FormatDate.format(booking.check_out_date),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ETA & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column(Modifier.weight(1f)) {
                    Text("ETA", fontSize = 12.sp, color = Color(0xFF666666))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        booking.time_of_arrival?.let { FormatTime.format(it) } ?: "08:00 AM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text("Payment Method", fontSize = 12.sp, color = Color(0xFF666666))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        booking.payment_method ?: "N/A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}




@Composable
private fun SpecialRequestCard(specialRequest: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "SPECIAL REQUEST",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = specialRequest,
                fontSize = 14.sp,
                color = Color(0xFF5D4037),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PaymentSummaryCard(booking: BookingDetails) {
    val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
        ?: booking.area_details?.discounted_price?.toDoubleOrNull()
        ?: booking.total_price
    val alreadyPaid = if (booking.total_amount == 0.0) booking.down_payment ?: 0.0 else booking.total_amount
    val remaining = totalAmount - alreadyPaid

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Payment Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Total Booking
            PaymentSummaryItem(
                label = "Total Booking",
                amount = totalAmount,
                color = Color(0xFF333333)
            )

            Spacer(Modifier.height(12.dp))

            // Already Paid
            PaymentSummaryItem(
                label = "Already Paid",
                amount = alreadyPaid,
                color = Color(0xFF333333)
            )

            Spacer(Modifier.height(12.dp))

            // Remaining Balance
            PaymentSummaryItem(
                label = "Remaining",
                amount = remaining,
                color = if (remaining > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                isBold = true,
                showIcon = remaining <= 0
            )
        }
    }
}

@Composable
private fun PaymentSummaryItem(
    label: String,
    amount: Double,
    color: Color,
    isBold: Boolean = false,
    showIcon: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isBold) color else Color(0xFF666666),
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            if (showIcon) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = "₱${"%,.2f".format(amount)}",
            fontSize = if (isBold) 16.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun PendingBookingSection(
    booking: BookingDetails,
    onReserve: (Double) -> Unit,
    onReject: () -> Unit
) {
    var enteredAmount by remember { mutableStateOf(0.0) }
    val canReserve = enteredAmount >= booking.total_price / 2 &&
            enteredAmount <= booking.total_price

    // Payment Proof (if any)
    if (!booking.payment_proof.isNullOrEmpty()) {
        PaymentProofSection(booking.payment_proof)
        Spacer(Modifier.height(16.dp))
    }

    // Down Payment Input
    DownPaymentSection(
        totalPrice = booking.total_price,
        onAmountChange = { enteredAmount = it }
    )

    Spacer(Modifier.height(16.dp))

    // Action Buttons
    ActionButtonsDouble(
        onReserve = { if (canReserve) onReserve(enteredAmount) },
        onReject = onReject,
        reserveEnabled = canReserve
    )
}

@Composable
private fun ReservedBookingSection(
    booking: BookingDetails,
    onCheckIn: (Double) -> Unit,
    onCancel: () -> Unit,
    onMarkNoShow: () -> Unit
) {
    var enteredAmount by remember { mutableStateOf(0.0) }

    val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
        ?: booking.area_details?.discounted_price?.toDoubleOrNull()
        ?: booking.total_price
    val downPayment = booking.down_payment ?: 0.0
    val remainingBalance = totalAmount - downPayment

    val today = LocalDate.now()
    val currentTime = remember { LocalTime.now() }
    val checkInDate = LocalDate.parse(booking.check_in_date)
    val checkInTime = LocalTime.of(14, 0) // 2:00 PM

    val canMarkNoShow = today.isAfter(checkInDate)
    val isCheckInDay = today.isEqual(checkInDate)
    val isAfter2PM = currentTime.isAfter(checkInTime) || currentTime.equals(checkInTime)
    val isPaymentComplete = enteredAmount == remainingBalance || remainingBalance == 0.0
    val canCheckIn = isCheckInDay && isAfter2PM && isPaymentComplete

    // Payment Proof (if any)
    if (!booking.payment_proof.isNullOrEmpty()) {
        PaymentProofSection(booking.payment_proof)
        Spacer(Modifier.height(16.dp))
    }

    // Payment Details Input
    PaymentDetailsSection(booking) { enteredAmount = it }

    Spacer(Modifier.height(16.dp))

    // Check-in Notice
    CheckInTimeNotice(
        canCheckIn = canCheckIn,
        isCheckInDay = isCheckInDay,
        isBeforeCheckIn = today.isBefore(checkInDate),
        isAfter2PM = isAfter2PM,
        isPaymentComplete = isPaymentComplete
    )

    Spacer(Modifier.height(16.dp))

    // Action Buttons
    ActionButtonsTriple(
        onMarkNoShow = onMarkNoShow,
        onCancel = onCancel,
        onCheckIn = { if (canCheckIn) onCheckIn(enteredAmount) },
        checkInEnabled = canCheckIn,
        markNoShowEnabled = canMarkNoShow
    )
}

@Composable
private fun CheckedInBookingSection(
    booking: BookingDetails,
    onCheckOut: () -> Unit
) {
    var enteredAmount by remember { mutableStateOf(0.0) }

    // Payment Proof (if any)
    if (!booking.payment_proof.isNullOrEmpty()) {
        PaymentProofSection(booking.payment_proof)
        Spacer(Modifier.height(16.dp))
    }

    // Payment Details (if any remaining)
    PaymentDetailsSection(booking) { enteredAmount = it }

    if (enteredAmount > 0) {
        Spacer(Modifier.height(16.dp))
    }

    Spacer(Modifier.height(16.dp))

    // Check-in Notice
    CheckOutTimeNotice()

    // Action Button
    ActionButtonsSingle(
        label = "Check Out Guest",
        onClick = onCheckOut,
    )
}

@Composable
private fun CheckedOutBookingSection(booking: BookingDetails) {
    var enteredAmount by remember { mutableStateOf(0.0) }

    // Payment Proof (if any)
    if (!booking.payment_proof.isNullOrEmpty()) {
        PaymentProofSection(booking.payment_proof)
        Spacer(Modifier.height(16.dp))
    }

    // Payment Details (read-only view)
    PaymentDetailsSection(booking) { enteredAmount = it }
}







@Composable
fun DownPaymentSection(totalPrice: Double, onAmountChange: (Double) -> Unit) {
    var enteredAmount by remember { mutableStateOf("") }
    val requiredDownPayment = totalPrice / 2
    val amount = enteredAmount.toDoubleOrNull() ?: 0.0
    val isTooMuch = amount > totalPrice

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Enter Down Payment",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Minimum required: ₱${"%,.2f".format(requiredDownPayment)}",
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )

        Spacer(Modifier.height(12.dp))

        AmountInputField(
            value = enteredAmount,
            onValueChange = {
                if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    enteredAmount = it
                    onAmountChange(it.toDoubleOrNull() ?: 0.0)
                }
            },
            onClear = {
                enteredAmount = ""
                onAmountChange(0.0)
            }
        )

        Spacer(Modifier.height(10.dp))

        PaymentValidationMessage(
            enteredAmount = enteredAmount,
            amount = amount,
            isTooMuch = isTooMuch,
            requiredDownPayment = requiredDownPayment,
            totalPrice = totalPrice
        )
    }
}

@Composable
fun PaymentDetailsSection(booking: BookingDetails, onAmountChange: (Double) -> Unit) {
    val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
        ?: booking.area_details?.discounted_price?.toDoubleOrNull()
        ?: booking.total_price
    val downPayment = booking.down_payment ?: 0.0
    val totalPaid = booking.total_amount
    val currentRemainingBalance = totalAmount - downPayment
    val isFullyPaid = totalPaid == totalAmount || downPayment == totalAmount

    var enteredAmount by remember { mutableStateOf("") }
    val amount = enteredAmount.toDoubleOrNull() ?: 0.0
    val isTooMuch = amount > currentRemainingBalance

    if (!isFullyPaid && currentRemainingBalance > 0.0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Enter Remaining Payment",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(Modifier.height(12.dp))

            AmountInputField(
                value = enteredAmount,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        enteredAmount = it
                        onAmountChange(it.toDoubleOrNull() ?: 0.0)
                    }
                },
                onClear = {
                    enteredAmount = ""
                    onAmountChange(0.0)
                }
            )

            Spacer(Modifier.height(10.dp))

            when {
                enteredAmount.isEmpty() ->
                    InfoMessage("Enter an amount to continue.", Color(0xFFED6C02))

                isTooMuch ->
                    InfoMessage("Amount exceeds balance.", Color(0xFFD32F2F))

                amount == 0.0 ->
                    InfoMessage("Amount cannot be zero.", Color(0xFFED6C02))

                else -> PaymentPreviewCard(
                    amountPaying = amount,
                    remainingBalance = currentRemainingBalance - amount,
                    isFullPayment = amount >= currentRemainingBalance
                )
            }
        }
    }
}





@Composable
private fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Enter payment amount", fontSize = 13.sp, color = Color.Gray) },
        leadingIcon = {
            Text("₱", style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold))
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp))
                }
            }
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFE0E0E0)
        )
    )
}

@Composable
private fun PaymentValidationMessage(
    enteredAmount: String,
    amount: Double,
    isTooMuch: Boolean,
    requiredDownPayment: Double,
    totalPrice: Double
) {


    when {
        enteredAmount.isEmpty() ->
            InfoMessage(
                "Enter an amount to continue.",
                AzureaColors.Warning
            )

        isTooMuch ->
            InfoMessage(
                "Amount exceeds total price.",
                AzureaColors.Error
            )

        amount < requiredDownPayment ->
            InfoMessage(
                "Minimum: ₱${"%,.2f".format(requiredDownPayment)}",
                AzureaColors.Warning
            )

        else -> PaymentPreviewCard(
            amountPaying = amount,
            remainingBalance = totalPrice - amount,
            isFullPayment = amount >= totalPrice
        )
    }
}


@Composable
private fun PaymentPreviewCard(
    amountPaying: Double,
    remainingBalance: Double,
    isFullPayment: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
            Spacer(Modifier.width(6.dp))
            Text("Payment Preview", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        PaymentPreviewRow(
            label = "You will pay:",
            amount = amountPaying,
            color = Color(0xFF2E7D32)
        )
        Spacer(Modifier.height(4.dp))

        PaymentPreviewRow(
            label = "Remaining balance:",
            amount = remainingBalance,
            color = if (isFullPayment) Color(0xFF2E7D32) else Color(0xFFED6C02)
        )

        if (isFullPayment) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "✓ Booking will be fully paid",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
private fun PaymentPreviewRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF1B5E20))
        Text(
            "₱${"%,.2f".format(amount)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoMessage(message: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(message, color = color, fontSize = 11.sp)
    }
}



@Composable
fun ActionButtonsDouble(
    onReserve: () -> Unit,
    onReject: () -> Unit,
    reserveEnabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onReserve,
            enabled = reserveEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color(0xFFBDBDBD)
            )
        ) {
            Text("Reserve Booking", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
            border = BorderStroke(1.dp, Color(0xFFD32F2F))
        ) {
            Text("Reject Booking", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ActionButtonsTriple(
    onMarkNoShow: () -> Unit,
    onCancel: () -> Unit,
    onCheckIn: () -> Unit,
    checkInEnabled: Boolean = true,
    markNoShowEnabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onCheckIn,
            enabled = checkInEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32),
                disabledContainerColor = Color(0xFFBDBDBD)
            )
        ) {
            Text("Check In Guest", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFED6C02)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFED6C02))
        ) {
            Text("Cancel Booking", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onMarkNoShow,
            enabled = markNoShowEnabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF757575),
                disabledContentColor = Color(0xFFBDBDBD)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF757575))
        ) {
            Text("Mark as No Show", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ActionButtonsSingle(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}



@Composable
fun PaymentProofSection(paymentProof: String?) {
    if (paymentProof.isNullOrEmpty()) return

    Spacer(Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment Proof",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = paymentProof,
                contentDescription = "Payment Proof",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}


val rejectionReasons = listOf(
    "Room not available for selected dates",
    "Double booking or overbooking conflict",
    "Property maintenance or repairs scheduled",
    "Property temporarily closed",
    "Invalid or incomplete booking information",
    "Guest does not meet booking requirements",
    "Suspicious or fraudulent booking attempt",
    "Previous booking violations or blacklisted guest",
    "Payment failed or not received",
    "Invalid payment method",
    "Booking made past allowed reservation window",
    "Booking duration exceeds maximum allowed stay",
    "Room blocked for VIP or event use",
    "Restricted dates due to regulations",
    "Other (please specify)"
)

val cancellationReasons = listOf(
    "Customer requested cancellation",
    "Payment issue",
    "Scheduling conflict",
    "Other (please specify)"
)

@Composable
fun RejectBookingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    ReasonSelectionDialog(
        title = "Reject Booking",
        reasons = rejectionReasons,
        buttonLabel = "Reject",
        buttonColor = Color(0xFFD32F2F),
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun CancelBookingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    ReasonSelectionDialog(
        title = "Cancel Booking",
        reasons = cancellationReasons,
        buttonLabel = "Cancel Booking",
        buttonColor = Color(0xFFED6C02),
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
private fun ReasonSelectionDialog(
    title: String,
    reasons: List<String>,
    buttonLabel: String,
    buttonColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text("Select a reason:", fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))

                DropdownField(
                    label = "Select reason",
                    value = selectedReason,
                    options = reasons,
                    onSelect = {
                        selectedReason = it
                        if (it != "Other (please specify)") {
                            customReason = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedReason == "Other (please specify)") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Specify reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other (please specify)") {
                        customReason
                    } else {
                        selectedReason
                    }
                    if (finalReason.isNotBlank()) onConfirm(finalReason)
                },
                enabled = selectedReason.isNotBlank() &&
                        (selectedReason != "Other (please specify)" || customReason.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(buttonLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (title == "Cancel Booking") "Close" else "Cancel", color = Color(0xFF757575))
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun ConfirmActionDialog(
    action: BookingAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, message) = when (action) {
        is BookingAction.Reserve ->
            "Reserve Booking" to "Are you sure you want to reserve this booking?"
        is BookingAction.CheckIn ->
            "Check In Guest" to "Are you sure you want to check in this guest?"
        is BookingAction.CheckOut ->
            "Check Out Guest" to "Are you sure you want to check out this guest?"
        is BookingAction.MarkNoShow ->
            "Mark as No Show" to "Are you sure you want to mark this booking as no show?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 14.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF757575))
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun CheckInTimeNotice(
    canCheckIn: Boolean,
    isCheckInDay: Boolean,
    isBeforeCheckIn: Boolean,
    isAfter2PM: Boolean,
    isPaymentComplete: Boolean
) {
    val (backgroundColor, borderColor, iconColor, textColor, icon, title, message) = when {
        isBeforeCheckIn -> {
            Tuple7(
                Color(0xFFFFF3E0),
                Color(0xFFFFB74D),
                Color(0xFFF57C00),
                Color(0xFFE65100),
                Icons.Default.Schedule,
                "Not Yet Check-in Date",
                "Check-in is only available on the booking date from 2:00 PM onwards"
            )
        }
        isCheckInDay && !isAfter2PM -> {
            Tuple7(
                Color(0xFFFFF9C4),
                Color(0xFFFDD835),
                Color(0xFFF9A825),
                Color(0xFFF57F17),
                Icons.Default.AccessTime,
                "Too Early for Check-in",
                "Check-in time starts at 2:00 PM. Current time is before check-in hours."
            )
        }
        isCheckInDay && isAfter2PM && !isPaymentComplete -> {
            Tuple7(
                Color(0xFFE3F2FD),
                Color(0xFF64B5F6),
                Color(0xFF1976D2),
                Color(0xFF0D47A1),
                Icons.Default.Payment,
                "Payment Required",
                "Please complete the remaining balance payment to check in the guest."
            )
        }
        canCheckIn -> {
            Tuple7(
                Color(0xFFE8F5E9),
                Color(0xFF81C784),
                Color(0xFF388E3C),
                Color(0xFF1B5E20),
                Icons.Default.CheckCircle,
                "Ready for Check-in",
                "All requirements met! You can now check in the guest."
            )
        }
        else -> {
            Tuple7(
                Color(0xFFFFEBEE),
                Color(0xFFEF5350),
                Color(0xFFD32F2F),
                Color(0xFFB71C1C),
                Icons.Default.Warning,
                "Check-in Date Passed",
                "Consider marking this booking as no-show if guest did not arrive."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun CheckOutTimeNotice() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "Check-out Policy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Standard check-out time is before 11:00 AM. Late check-out may incur additional charges.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100).copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDetailsHeader(onDismiss: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Booking Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}





private data class Tuple7<A, B, C, D, E, F, G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G
)

