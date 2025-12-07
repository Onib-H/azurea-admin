package com.harold.azureaadmin.ui.screens.admin.bookings

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
import com.harold.azureaadmin.utils.FormatDate
import com.harold.azureaadmin.utils.FormatTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

// ============================================================================
// BOOKING ACTIONS
// ============================================================================

sealed class BookingAction(val bookingId: Int) {
    class Reserve(bookingId: Int) : BookingAction(bookingId)
    class CheckIn(bookingId: Int) : BookingAction(bookingId)
    class CheckOut(bookingId: Int) : BookingAction(bookingId)
    class MarkNoShow(bookingId: Int) : BookingAction(bookingId)
}

// ============================================================================
// MAIN DIALOG
// ============================================================================

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

    // Initialize data on first load
    LaunchedEffect(bookingId) {
        viewModel.clearStatusMessage()
        viewModel.clearError()
        viewModel.getBookingDetails(bookingId)
    }

    // Handle successful status update
    LaunchedEffect(statusUpdateMessage) {
        statusUpdateMessage?.let {
            // Small delay to show completion animation
            kotlinx.coroutines.delay(300)
            isUpdatingStatus = false
            viewModel.clearStatusMessage()
            onDismiss()
        }
    }

    // Show loading animation when updating status
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
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF666666)
                    )
                }
            }
        }
    }

    // Dialogs
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
                    else -> {} // Other actions handled elsewhere
                }
                showConfirmDialog = null
            }
        )
    }
}

// ============================================================================
// LOADING & ERROR STATES
// ============================================================================

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = Color(0xFF7B1FA2)
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
        ) {
            Text("Retry")
        }
    }
}

// ============================================================================
// BOOKING DETAILS CONTENT
// ============================================================================

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
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Booking Details",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A1A1A)
            )
        )

        Spacer(Modifier.height(16.dp))
        BookingInfoSection(booking)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFE0E0E0))
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
    }
}

// ============================================================================
// STATUS-SPECIFIC SECTIONS
// ============================================================================

@Composable
private fun PendingBookingSection(
    booking: BookingDetails,
    onReserve: (Double) -> Unit,
    onReject: () -> Unit
) {
    var enteredAmount by remember { mutableStateOf(0.0) }
    val canReserve = enteredAmount >= booking.total_price / 2 &&
            enteredAmount <= booking.total_price

    PaymentProofSection(booking.payment_proof)
    DownPaymentSection(
        totalPrice = booking.total_price,
        onAmountChange = { enteredAmount = it }
    )
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
    val currentTime = remember { java.time.LocalTime.now() }
    val checkInDate = LocalDate.parse(booking.check_in_date)
    val checkInTime = java.time.LocalTime.of(14, 0) // 2:00 PM

    val canMarkNoShow = today.isAfter(checkInDate)

    // Can only check in if:
    // 1. Today is the check-in date
    // 2. Current time is 2:00 PM or later
    // 3. Payment is complete (remaining balance is 0 or payment entered equals remaining balance)
    val isCheckInDay = today.isEqual(checkInDate)
    val isAfter2PM = currentTime.isAfter(checkInTime) || currentTime.equals(checkInTime)
    val isPaymentComplete = enteredAmount == remainingBalance || remainingBalance == 0.0

    val canCheckIn = isCheckInDay && isAfter2PM && isPaymentComplete

    PaymentProofSection(booking.payment_proof)
    PaymentDetailsSection(booking) { enteredAmount = it }

    // Show check-in time restriction notice
    Spacer(Modifier.height(12.dp))
    CheckInTimeNotice(
        canCheckIn = canCheckIn,
        isCheckInDay = isCheckInDay,
        isBeforeCheckIn = today.isBefore(checkInDate),
        isAfter2PM = isAfter2PM,
        isPaymentComplete = isPaymentComplete
    )

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

    PaymentProofSection(booking.payment_proof)
    PaymentDetailsSection(booking) { enteredAmount = it }

    Spacer(Modifier.height(12.dp))
    CheckOutTimeNotice()


    ActionButtonsSingle(
        label = "Check Out Guest",
        onClick = onCheckOut,
    )
}



@Composable
private fun CheckedOutBookingSection(booking: BookingDetails) {
    var enteredAmount by remember { mutableStateOf(0.0) }

    PaymentProofSection(booking.payment_proof)
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
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.CreditCard, title = "Payment Details")
        Spacer(Modifier.height(12.dp))

        // Payment info
        PaymentInfoCard {
            PaymentInfoRow("Required Down Payment", requiredDownPayment, Color(0xFF7B1FA2))
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE0E0E0))
            PaymentInfoRow("Total Booking Amount", totalPrice, Color(0xFF2E7D32))
        }

        Spacer(Modifier.height(12.dp))

        // Amount input field
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

        // Validation messages and summary
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        SectionHeader(icon = Icons.Default.CreditCard, title = "Payment Details")
        Spacer(Modifier.height(12.dp))

        PaymentInfoCard {
            Text("Current Status", fontSize = 13.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            PaymentInfoRow("Total Booking", totalAmount, Color(0xFF1A1A1A))
            Spacer(Modifier.height(6.dp))
            PaymentInfoRow("Already Paid", if (totalPaid == 0.0) downPayment else totalPaid, Color(0xFF1A1A1A))
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE0E0E0))

            if (isFullyPaid || currentRemainingBalance <= 0.0) {
                Text("✓ Fully Paid", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            } else {
                RemainingBalanceSection(
                    remainingBalance = currentRemainingBalance,
                    enteredAmount = enteredAmount,
                    amount = amount,
                    isTooMuch = isTooMuch,
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
            }
        }
    }
}



@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color(0xFF7B1FA2), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun PaymentInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        content()
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
        modifier = Modifier.fillMaxWidth()
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
            InfoMessage("Enter an amount to continue.", Color(0xFFED6C02))

        isTooMuch ->
            InfoMessage("Amount exceeds total price.", Color(0xFFD32F2F))

        amount < requiredDownPayment ->
            InfoMessage("Minimum: ₱${"%,.2f".format(requiredDownPayment)}", Color(0xFFED6C02))

        else -> PaymentSummaryCard(
            amountPaying = amount,
            remainingBalance = totalPrice - amount,
            isFullPayment = amount >= totalPrice
        )
    }
}

@Composable
private fun RemainingBalanceSection(
    remainingBalance: Double,
    enteredAmount: String,
    amount: Double,
    isTooMuch: Boolean,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Remaining Balance", fontSize = 13.sp, color = Color(0xFF757575))
            Text(
                "₱${"%,.2f".format(remainingBalance)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }

        Spacer(Modifier.height(12.dp))

        AmountInputField(
            value = enteredAmount,
            onValueChange = onValueChange,
            onClear = onClear
        )

        Spacer(Modifier.height(10.dp))

        when {
            enteredAmount.isEmpty() ->
                InfoMessage("Enter an amount to continue.", Color(0xFFED6C02))

            isTooMuch ->
                InfoMessage("Amount exceeds balance.", Color(0xFFD32F2F))

            amount == 0.0 ->
                InfoMessage("Amount cannot be zero.", Color(0xFFED6C02))

            else -> PaymentSummaryCard(
                amountPaying = amount,
                remainingBalance = remainingBalance - amount,
                isFullPayment = amount >= remainingBalance,
                label = "Final Summary"
            )
        }
    }
}

@Composable
private fun PaymentSummaryCard(
    amountPaying: Double,
    remainingBalance: Double,
    isFullPayment: Boolean,
    label: String = "Payment Summary"
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
            Text(label, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        PaymentSummaryRow(
            label = if (label == "Final Summary") "Amount to pay:" else "You will pay now:",
            amount = amountPaying,
            color = Color(0xFF2E7D32)
        )
        Spacer(Modifier.height(4.dp))

        PaymentSummaryRow(
            label = if (label == "Final Summary") "Balance after:" else "Remaining balance:",
            amount = remainingBalance,
            color = if (isFullPayment) Color(0xFF2E7D32) else Color(0xFFED6C02)
        )

        if (isFullPayment) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (label == "Final Summary")
                    "✓ Booking will be fully completed"
                else
                    "✓ Booking will be fully paid",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PaymentInfoRow(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF757575))
        Text(
            "₱${"%,.2f".format(amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun PaymentSummaryRow(label: String, amount: Double, color: Color) {
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
                containerColor = Color(0xFF7B1FA2),
                disabledContainerColor = Color(0xFFBDBDBD)
            )
        ) {
            Text("Reserve Booking", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
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
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}



@Composable
fun PaymentProofSection(paymentProof: String?) {
    if (paymentProof.isNullOrEmpty()) return

    Spacer(Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        SectionHeader(icon = Icons.Filled.Image, title = "Payment Proof")
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp)),
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

@Composable
fun BookingInfoSection(booking: BookingDetails) {
    val totalAmount = booking.room_details?.discounted_price
        ?: booking.area_details?.discounted_price
    val propertyType = if (booking.is_venue_booking) "Area" else "Room"
    val guestName = "${booking.user.first_name} ${booking.user.last_name}"

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        InfoRowDisplay("Guest", guestName)
        InfoRowDisplay(label = "Property", content = { PropertyChip(type = propertyType) })
        InfoRowDisplay("Check-in", FormatDate.format(booking.check_in_date))
        InfoRowDisplay("Check-out", FormatDate.format(booking.check_out_date))
        InfoRowDisplay("ETA", booking.time_of_arrival?.let { FormatTime.format(it) } ?: "08:00 AM")
        InfoRowDisplay(label = "Status", content = { BookingStatusChip(status = booking.status) })
        InfoRowDisplay(
            label = "Amount",
            content = {
                Text(
                    text = totalAmount ?: "₱${"%,.2f".format(booking.total_price)}",
                    color = Color(0xFF7B1FA2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        )
        InfoRowDisplay("Booking Date", FormatDate.format(booking.created_at))
    }
}

@Composable
fun InfoRowDisplay(label: String, value: String) {
    InfoRowDisplay(label) {
        Text(text = value, fontSize = 14.sp, color = Color(0xFF1A1A1A))
    }
}

@Composable
fun InfoRowDisplay(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = Color(0xFF757575)
        )
        Box(contentAlignment = Alignment.CenterEnd) {
            content()
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
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
        // Before check-in date
        isBeforeCheckIn -> {
            Tuple7(
                Color(0xFFFFF3E0),
                Color(0xFFFFB74D),
                Color(0xFFF57C00),
                Color(0xFFE65100),
                Icons.Default.Schedule,
                "Not Yet Check-in Date",
                "Check-in is only available on the booking date (${
                    java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
                        .format(java.time.LocalDate.now())
                }) from 2:00 PM onwards"
            )
        }
        // On check-in day but before 2 PM
        isCheckInDay && !isAfter2PM -> {
            Tuple7(
                Color(0xFFFFF9C4),
                Color(0xFFFDD835),
                Color(0xFFF9A825),
                Color(0xFFF57F17),
                Icons.Default.AccessTime,
                "Too Early for Check-in",
                "Check-in time starts at 2:00 PM. Current time is before check-in hours. Please wait until 2:00 PM to proceed."
            )
        }
        // On check-in day, after 2 PM, but payment incomplete
        isCheckInDay && isAfter2PM && !isPaymentComplete -> {
            Tuple7(
                Color(0xFFE3F2FD),
                Color(0xFF64B5F6),
                Color(0xFF1976D2),
                Color(0xFF0D47A1),
                Icons.Default.Payment,
                "Payment Required",
                "Check-in time has started (2:00 PM onwards). Please complete the remaining balance payment to check in the guest."
            )
        }
        // Ready for check-in (all conditions met)
        canCheckIn -> {
            Tuple7(
                Color(0xFFE8F5E9),
                Color(0xFF81C784),
                Color(0xFF388E3C),
                Color(0xFF1B5E20),
                Icons.Default.CheckCircle,
                "Ready for Check-in",
                "All requirements met! You can now check in the guest. Check-in time: 2:00 PM - 11:59 PM"
            )
        }
        // Past check-in date
        else -> {
            Tuple7(
                Color(0xFFFFEBEE),
                Color(0xFFEF5350),
                Color(0xFFD32F2F),
                Color(0xFFB71C1C),
                Icons.Default.Warning,
                "Check-in Date Passed",
                "Check-in date has passed. Consider marking this booking as no-show if guest did not arrive."
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

// Helper data class for notice parameters
private data class Tuple7<A, B, C, D, E, F, G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G
)
