    package com.example.azureaadmin.ui.screens.admin.bookings

    import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.azureaadmin.data.models.BookingDetails
import com.example.azureaadmin.ui.components.button.DropdownField
import com.example.azureaadmin.utils.FormatDate

    @Composable
    fun BookingDetailsDialog(
        bookingId: Int,
        viewModel: BookingViewModel,
        onDismiss: () -> Unit
    ) {
        val bookingDetails by viewModel.bookingDetails.collectAsState()
        val loading by viewModel.loading.collectAsState()
        val error by viewModel.error.collectAsState()
        val statusUpdateMessage by viewModel.statusUpdateMessage.collectAsState()

        var showRejectDialog by remember { mutableStateOf(false) }
        var showCancelDialog by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf<BookingAction?>(null) }


        LaunchedEffect(bookingId) {
            viewModel.clearStatusMessage()
            viewModel.clearError()
            viewModel.getBookingDetails(bookingId)
        }

        LaunchedEffect(statusUpdateMessage) {
            if (statusUpdateMessage != null) {
                onDismiss()
                viewModel.refreshBookings()
                viewModel.clearStatusMessage()
            }
        }

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
                        loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                            )
                        }

                        error != null -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = error ?: "Error loading booking details",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { viewModel.getBookingDetails(bookingId) }) {
                                    Text("Retry")
                                }
                            }
                        }

                        bookingDetails != null -> {
                            val booking = bookingDetails!!

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                            ) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Booking Details",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A1A)
                                        )
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // Info Section
                                BookingInfoSection(booking)
                                Spacer(Modifier.height(24.dp))
                                HorizontalDivider(color = Color(0xFFE0E0E0))
                                Spacer(Modifier.height(24.dp))

                                // Booking status sections
                                when (booking.status.uppercase()) {
                                    "PENDING" -> {
                                        var pendingEnteredAmount by remember { mutableStateOf(0.0) }

                                        PaymentProofSection(booking.payment_proof)

                                        DownPaymentSection(
                                            totalPrice = booking.total_price,
                                            onAmountChange = { pendingEnteredAmount = it }
                                        )

                                        val canReserve = pendingEnteredAmount >= booking.total_price / 2
                                                && pendingEnteredAmount <= booking.total_price


                                        ActionButtonsDouble(
                                            onReserve = {
                                                if (canReserve) {
                                                    // ✅ Backend accepts down_payment only for 'reserved' status
                                                    viewModel.updateBookingStatus(
                                                        bookingId = booking.id,
                                                        newStatus = "reserved",
                                                        downPayment = pendingEnteredAmount
                                                    )
                                                }
                                            },
                                            onReject = { showRejectDialog = true },
                                            reserveEnabled = canReserve
                                        )
                                    }

                                    "RESERVED" -> {
                                        var enteredAmount by remember { mutableStateOf(0.0) }

                                        // Display existing payment proof and input field
                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { enteredAmount = it }

                                        // Compute totals
                                        val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
                                            ?: booking.area_details?.discounted_price?.toDoubleOrNull()
                                            ?: booking.total_price
                                        val downPayment = booking.down_payment ?: 0.0
                                        val remainingBalance = totalAmount - downPayment
                                        val totalPayment = if (downPayment == totalAmount) downPayment else downPayment + enteredAmount

                                        // User must pay full remaining balance to check in
                                        val canCheckIn = enteredAmount == remainingBalance || remainingBalance == 0.0

                                        ActionButtonsTriple(
                                            onMarkNoShow = {
                                                showConfirmDialog = BookingAction.MarkNoShow(booking.id)
                                            },
                                            onCancel = { showCancelDialog = true },
                                            onCheckIn = {
                                                if (canCheckIn) {

                                                    if (enteredAmount > 0) {
                                                        // ✅ Record the full payment (down payment + entered amount), then check in
                                                        viewModel.recordPaymentAndCheckIn(
                                                            bookingId = booking.id,
                                                            amount = totalPayment
                                                        )
                                                    } else {
                                                        // Already fully paid — just update booking status
                                                        viewModel.updateBookingStatus(
                                                            bookingId = booking.id,
                                                            newStatus = "checked_in"
                                                        )
                                                    }
                                                }
                                            },
                                            checkInEnabled = canCheckIn
                                        )
                                    }


                                    "CHECKED_IN" -> {
                                        var checkedInEnteredAmount by remember { mutableStateOf(0.0) }

                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { checkedInEnteredAmount = it }
                                        ActionButtonsSingle(
                                            label = "Check Out Guest",
                                            onClick = {
                                                showConfirmDialog = BookingAction.CheckOut(booking.id)
                                            }
                                        )
                                    }

                                    "CHECKED_OUT" -> {
                                        var checkedOutEnteredAmount by remember { mutableStateOf(0.0) }

                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { checkedOutEnteredAmount = it }

                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
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

        // Reject Dialog
        if (showRejectDialog) {
            RejectBookingDialog(
                onDismiss = { showRejectDialog = false },
                onConfirm = { reason ->
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

        // Cancel Dialog
        if (showCancelDialog) {
            CancelBookingDialog(
                onDismiss = { showCancelDialog = false },
                onConfirm = { reason ->
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

        // Confirmation Dialog
        showConfirmDialog?.let { action ->
            ConfirmActionDialog(
                action = action,
                onDismiss = { showConfirmDialog = null },
                onConfirm = {
                    when (action) {
                        is BookingAction.Reserve -> viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "reserved"
                        )

                        is BookingAction.CheckIn -> viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "checked_in"
                        )

                        is BookingAction.CheckOut -> viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "checked_out",
                            setAvailable = true
                        )

                        is BookingAction.MarkNoShow -> viewModel.updateBookingStatus(
                            bookingId = action.bookingId,
                            newStatus = "no_show",
                            setAvailable = true
                        )
                    }
                    showConfirmDialog = null
                }
            )
        }
    }




    @Composable
    fun DownPaymentSection(
        totalPrice: Double,
        onAmountChange: (Double) -> Unit
    ) {
        var enteredAmount by remember { mutableStateOf("") }

        val maxAmount = totalPrice
        val requiredDownPayment = maxAmount / 2
        val amount = enteredAmount.toDoubleOrNull() ?: 0.0
        val isTooMuch = amount > maxAmount

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8EDFF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFBFC6FF), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Payment Details",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Required Down Payment",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "₱${"%,.2f".format(requiredDownPayment)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2)
                    )
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    color = Color(0xFFE0E0E0)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Booking Amount",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "₱${"%,.2f".format(totalPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }


            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = enteredAmount,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        enteredAmount = it
                        onAmountChange(it.toDoubleOrNull() ?: 0.0)
                    }
                },
                placeholder = {
                    Text("Enter payment amount", fontSize = 13.sp, color = Color.Gray)
                },
                leadingIcon = {
                    Text(
                        text = "₱",
                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                    )
                },
                trailingIcon = {
                    if (enteredAmount.isNotEmpty()) {
                        IconButton(onClick = {
                            enteredAmount = ""
                            onAmountChange(0.0)
                        }) {
                            Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            )

            Spacer(Modifier.height(10.dp))

            when {
                enteredAmount.isEmpty() -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFED6C02),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Enter an amount to continue.",
                            color = Color(0xFFED6C02),
                            fontSize = 10.sp
                        )
                    }
                }

                isTooMuch -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Amount exceeds total price. Please adjust.",
                            color = Color(0xFFD32F2F),
                            fontSize = 10.sp
                        )
                    }
                }

                amount < requiredDownPayment -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFED6C02),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Minimum down payment is ₱${"%,.2f".format(requiredDownPayment)}.",
                            color = Color(0xFFED6C02),
                            fontSize = 10.sp
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Payment Summary",
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "You will pay now:",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "₱${"%,.2f".format(amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Remaining balance:",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "₱${"%,.2f".format(totalPrice - amount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amount >= totalPrice) Color(0xFF2E7D32) else Color(0xFFED6C02)
                            )
                        }

                        if (amount >= totalPrice) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "✓ Booking will be fully paid",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun PaymentDetailsSection(
        booking: BookingDetails,
        onAmountChange: (Double) -> Unit
    ) {
        val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
            ?: booking.area_details?.discounted_price?.toDoubleOrNull()
            ?: booking.total_price

        val downPayment = booking.down_payment ?: 0.0
        val totalPaid = booking.total_amount
        val status = booking.status.lowercase()

        var enteredAmount by remember { mutableStateOf("") }
        val amount = enteredAmount.toDoubleOrNull() ?: 0.0

        val currentRemainingBalance = totalAmount - downPayment
        val newTotalPaid = totalPaid + amount

        val isFullyPaid = totalPaid == totalAmount || downPayment == totalAmount
        val isTooMuch = amount > currentRemainingBalance
        val isZero = amount == 0.0

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8EDFF), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFBFC6FF), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Payment Details",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current Payment Status",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Booking Amount", fontSize = 13.sp, color = Color(0xFF757575))
                    Text(
                        "₱${"%,.2f".format(totalAmount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Already Paid", fontSize = 13.sp, color = Color(0xFF757575))
                    val paidDisplay = if (totalPaid == 0.0) downPayment else totalPaid
                    Text(
                        "₱${"%,.2f".format(paidDisplay)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    color = Color(0xFFE0E0E0)
                )

                if (isFullyPaid || currentRemainingBalance <= 0.0) {
                    Text(
                        text = "✓ Fully Paid",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Balance", fontSize = 13.sp, color = Color(0xFF757575))
                            Text(
                                "₱${"%,.2f".format(currentRemainingBalance)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = enteredAmount,
                            onValueChange = {
                                if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    enteredAmount = it
                                    onAmountChange(it.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            placeholder = {
                                Text("Enter payment amount", fontSize = 12.sp, color = Color.Gray)
                            },
                            leadingIcon = {
                                Text(
                                    text = "₱",
                                    style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            trailingIcon = {
                                if (enteredAmount.isNotEmpty()) {
                                    IconButton(onClick = {
                                        enteredAmount = ""
                                        onAmountChange(0.0)
                                    }) {
                                        Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(min = 48.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        when {
                            enteredAmount.isEmpty() -> {
                                InfoRow("Enter an amount to continue.", Color(0xFFED6C02))
                            }

                            isTooMuch -> {
                                InfoRow("Amount exceeds remaining balance. Please adjust.", Color(0xFFD32F2F))
                            }

                            isZero -> {
                                InfoRow("Payment amount cannot be zero.", Color(0xFFED6C02))
                            }

                            else -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Final Payment Summary",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Amount to pay now:",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "₱${"%,.2f".format(amount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Balance after payment:",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "₱${"%,.2f".format(currentRemainingBalance - amount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (amount >= currentRemainingBalance)
                                                Color(0xFF2E7D32)
                                            else Color(0xFFED6C02)
                                        )
                                    }

                                    if (amount >= currentRemainingBalance) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "✓ The booking will be fully completed once this payment is made.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoRow(message: String, color: Color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = message,
                color = color,
                fontSize = 10.sp
            )
        }
    }






    @Composable
    fun ActionButtonsDouble(
        onReserve: () -> Unit,
        onReject: () -> Unit,
        reserveEnabled: Boolean = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onReserve,
                enabled = reserveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reserve Booking")
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reject Booking")
            }
        }
    }
    
    @Composable
    fun ActionButtonsTriple(
        onMarkNoShow: () -> Unit,
        onCancel: () -> Unit,
        onCheckIn: () -> Unit,
        checkInEnabled: Boolean = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCheckIn,
                enabled = checkInEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check In Guest")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel Booking")
            }
            OutlinedButton(onClick = onMarkNoShow, modifier = Modifier.fillMaxWidth()) {
                Text("Mark as No Show")
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
        ) {
            Text(label)
        }
    }
    
    
    @Composable
    fun InfoRow(label: String, value: String) {
        InfoRow(label) {
            Text(text = value)
        }
    }
    
    @Composable
    fun InfoRow(label: String, content: @Composable () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                content()
            }
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
                .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Payment Proof",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
    
            Spacer(Modifier.height(10.dp))
    
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFBFC6FF), RoundedCornerShape(8.dp)),
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
        val totalAmount = booking.room_details?.discounted_price ?: booking.area_details?.discounted_price
        val propertyType = if (booking.is_venue_booking) "Area" else "Room"
        val guestName = "${booking.user.first_name} ${booking.user.last_name}"
        val checkInDate = FormatDate.format(booking.check_in_date)
        val checkOutDate = FormatDate.format(booking.check_out_date)
        val createdAt = FormatDate.format(booking.created_at)
    
    
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoRow("Guest", guestName)
    
            InfoRow(
                label = "Property",
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PropertyChip(type = propertyType)
                    }
                }
            )
    
            InfoRow("Check-in", checkInDate)
            InfoRow("Check-out", checkOutDate)
    
            InfoRow(
                label = "Status",
                content = { BookingStatusChip(status = booking.status) }
            )
    
            InfoRow(
                label = "Amount",
                content = {
                    Text(
                        text = if (totalAmount.isNullOrEmpty())
                            "₱${"%,.2f".format(booking.total_price)}"
                        else totalAmount,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
    
            InfoRow("Booking Date", createdAt)
        }
    }


    sealed class BookingAction(val bookingId: Int) {
        class Reserve(bookingId: Int) : BookingAction(bookingId)
        class CheckIn(bookingId: Int) : BookingAction(bookingId)
        class CheckOut(bookingId: Int) : BookingAction(bookingId)
        class MarkNoShow(bookingId: Int) : BookingAction(bookingId)
    }

    val rejectionReasons = listOf(
        "Room not available for selected dates",
        "Double booking or overbooking conflict",
        "Property maintenance or repairs scheduled",
        "Property temporarily closed",
        "Invalid or incomplete booking information",
        "Guest does not meet booking requirements (e.g., age, ID)",
        "Suspicious or fraudulent booking attempt",
        "Previous booking violations or blacklisted guest",
        "Payment failed or not received within required timeframe",
        "Invalid payment method",
        "Booking made past allowed reservation window",
        "Booking duration exceeds maximum allowed stay",
        "Room blocked for VIP or event use",
        "Restricted dates due to local government/health regulations",
        "Other (please specify)"
    )

    @Composable
    fun RejectBookingDialog(
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var selectedReason by remember { mutableStateOf("") }
        var customReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Reject Booking") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Please select a reason for rejecting this booking:")
                    Spacer(Modifier.height(8.dp))

                    DropdownField(
                        label = "Select reason",
                        value = selectedReason,
                        options = rejectionReasons,
                        onSelect = {
                            selectedReason = it
                            if (it != "Other (please specify)") {
                                customReason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedReason == "Other (please specify)") {
                        Spacer(Modifier.height(8.dp))
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
                            (selectedReason != "Other (please specify)" || customReason.isNotBlank())
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    @Composable
    fun CancelBookingDialog(
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var selectedReason by remember { mutableStateOf("") }
        var customReason by remember { mutableStateOf("") }

        val cancellationReasons = listOf(
            "Customer requested cancellation",
            "Payment issue",
            "Scheduling conflict",
            "Other (please specify)"
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Cancel Booking") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Please select a reason for cancelling this booking:")
                    Spacer(Modifier.height(8.dp))

                    DropdownField(
                        label = "Select reason",
                        value = selectedReason,
                        options = cancellationReasons,
                        onSelect = {
                            selectedReason = it
                            if (it != "Other (please specify)") {
                                customReason = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedReason == "Other (please specify)") {
                        Spacer(Modifier.height(8.dp))
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
                            (selectedReason != "Other (please specify)" || customReason.isNotBlank())
                ) {
                    Text("Cancel Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }


    @Composable
    fun ConfirmActionDialog(
        action: BookingAction,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        val (title, message) = when (action) {
            is BookingAction.Reserve -> "Reserve Booking" to "Are you sure you want to reserve this booking?"
            is BookingAction.CheckIn -> "Check In Guest" to "Are you sure you want to check in this guest?"
            is BookingAction.CheckOut -> "Check Out Guest" to "Are you sure you want to check out this guest?"
            is BookingAction.MarkNoShow -> "Mark as No Show" to "Are you sure you want to mark this booking as no show?"
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }