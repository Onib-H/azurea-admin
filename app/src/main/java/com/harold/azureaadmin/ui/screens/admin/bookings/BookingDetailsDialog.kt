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
            if (statusUpdateMessage != null) {
                isUpdatingStatus = false
                onDismiss()
                viewModel.refreshBookings()
                viewModel.clearStatusMessage()
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
                color = Color.White
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center).size(40.dp),
                                color = Color(0xFF7B1FA2)
                            )
                        }

                        error != null -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = error ?: "Error loading booking details",
                                    color = Color(0xFFD32F2F),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.getBookingDetails(bookingId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                                ) {
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
                                                    currentActionType = BookingActionType.RESERVE
                                                    isUpdatingStatus = true
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

                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { enteredAmount = it }

                                        val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
                                            ?: booking.area_details?.discounted_price?.toDoubleOrNull()
                                            ?: booking.total_price
                                        val downPayment = booking.down_payment ?: 0.0
                                        val remainingBalance = totalAmount - downPayment

                                        val today = LocalDate.now()
                                        val checkInDate = LocalDate.parse(booking.check_in_date)

                                        val canMarkNoShow = today.isAfter(checkInDate)

                                        val canCheckIn = today.isEqual(checkInDate) &&
                                                (enteredAmount == remainingBalance || remainingBalance == 0.0)

                                        ActionButtonsTriple(
                                            onMarkNoShow = { showConfirmDialog = BookingAction.MarkNoShow(booking.id) },
                                            onCancel = { showCancelDialog = true },
                                            onCheckIn = {
                                                if (canCheckIn) {
                                                    currentActionType = BookingActionType.CHECK_IN
                                                    isUpdatingStatus = true
                                                    val totalPayment = if (downPayment == totalAmount) downPayment else downPayment + enteredAmount
                                                    if (enteredAmount > 0) {
                                                        viewModel.recordPaymentAndCheckIn(
                                                            bookingId = booking.id,
                                                            amount = totalPayment
                                                        )
                                                    } else {
                                                        viewModel.updateBookingStatus(
                                                            bookingId = booking.id,
                                                            newStatus = "checked_in"
                                                        )
                                                    }
                                                }
                                            },
                                            checkInEnabled = canCheckIn,
                                            markNoShowEnabled = canMarkNoShow
                                        )
                                    }

                                    "CHECKED_IN" -> {
                                        var checkedInEnteredAmount by remember { mutableStateOf(0.0) }
                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { checkedInEnteredAmount = it }
                                        ActionButtonsSingle(
                                            label = "Check Out Guest",
                                            onClick = { showConfirmDialog = BookingAction.CheckOut(booking.id) }
                                        )
                                    }

                                    "CHECKED_OUT" -> {
                                        var checkedOutEnteredAmount by remember { mutableStateOf(0.0) }
                                        PaymentProofSection(booking.payment_proof)
                                        PaymentDetailsSection(booking) { checkedOutEnteredAmount = it }
                                    }
                                }
                            }
                        }
                    }

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
                        is BookingAction.Reserve -> {
                            currentActionType = BookingActionType.RESERVE
                            isUpdatingStatus = true
                            viewModel.updateBookingStatus(bookingId = action.bookingId, newStatus = "reserved")
                        }
                        is BookingAction.CheckIn -> {
                            currentActionType = BookingActionType.CHECK_IN
                            isUpdatingStatus = true
                            viewModel.updateBookingStatus(bookingId = action.bookingId, newStatus = "checked_in")
                        }
                        is BookingAction.CheckOut -> {
                            currentActionType = BookingActionType.CHECK_OUT
                            isUpdatingStatus = true
                            viewModel.updateBookingStatus(bookingId = action.bookingId, newStatus = "checked_out", setAvailable = true)
                        }
                        is BookingAction.MarkNoShow -> {
                            currentActionType = BookingActionType.MARK_NO_SHOW
                            isUpdatingStatus = true
                            viewModel.updateBookingStatus(bookingId = action.bookingId, newStatus = "no_show", setAvailable = true)
                        }
                    }
                    showConfirmDialog = null
                }
            )
        }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Payment Details",
                    color = Color(0xFF7B1FA2),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                PaymentInfoRow("Required Down Payment", requiredDownPayment, Color(0xFF7B1FA2))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE0E0E0))
                PaymentInfoRow("Total Booking Amount", totalPrice, Color(0xFF2E7D32))
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = enteredAmount,
                onValueChange = {
                    if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        enteredAmount = it
                        onAmountChange(it.toDoubleOrNull() ?: 0.0)
                    }
                },
                placeholder = { Text("Enter payment amount", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = {
                    Text(text = "₱", style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold))
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
            )

            Spacer(Modifier.height(10.dp))

            when {
                enteredAmount.isEmpty() -> InfoMessage("Enter an amount to continue.", Color(0xFFED6C02))
                isTooMuch -> InfoMessage("Amount exceeds total price.", Color(0xFFD32F2F))
                amount < requiredDownPayment -> InfoMessage("Minimum: ₱${"%,.2f".format(requiredDownPayment)}", Color(0xFFED6C02))
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                            Spacer(Modifier.width(6.dp))
                            Text("Payment Summary", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        PaymentSummaryRow("You will pay now:", amount, Color(0xFF2E7D32))
                        Spacer(Modifier.height(4.dp))
                        PaymentSummaryRow("Remaining balance:", totalPrice - amount,
                            if (amount >= totalPrice) Color(0xFF2E7D32) else Color(0xFFED6C02))
                        if (amount >= totalPrice) {
                            Spacer(Modifier.height(6.dp))
                            Text("✓ Booking will be fully paid", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PaymentDetailsSection(booking: BookingDetails, onAmountChange: (Double) -> Unit) {
        val totalAmount = booking.room_details?.discounted_price?.toDoubleOrNull()
            ?: booking.area_details?.discounted_price?.toDoubleOrNull() ?: booking.total_price
        val downPayment = booking.down_payment ?: 0.0
        val totalPaid = booking.total_amount
        var enteredAmount by remember { mutableStateOf("") }
        val amount = enteredAmount.toDoubleOrNull() ?: 0.0
        val currentRemainingBalance = totalAmount - downPayment
        val isFullyPaid = totalPaid == totalAmount || downPayment == totalAmount
        val isTooMuch = amount > currentRemainingBalance

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, null, tint = Color(0xFF7B1FA2), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Payment Details", color = Color(0xFF7B1FA2), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text("Current Status", fontSize = 13.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                PaymentInfoRow("Total Booking", totalAmount, Color(0xFF1A1A1A))
                Spacer(Modifier.height(6.dp))
                PaymentInfoRow("Already Paid", if (totalPaid == 0.0) downPayment else totalPaid, Color(0xFF1A1A1A))
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE0E0E0))

                if (isFullyPaid || currentRemainingBalance <= 0.0) {
                    Text("✓ Fully Paid", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                } else {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remaining Balance", fontSize = 13.sp, color = Color(0xFF757575))
                            Text("₱${"%,.2f".format(currentRemainingBalance)}", fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = enteredAmount,
                            onValueChange = {
                                if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    enteredAmount = it
                                    onAmountChange(it.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            placeholder = { Text("Enter payment amount", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = { Text("₱", style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)) },
                            trailingIcon = {
                                if (enteredAmount.isNotEmpty()) {
                                    IconButton(onClick = { enteredAmount = ""; onAmountChange(0.0) }) {
                                        Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        when {
                            enteredAmount.isEmpty() -> InfoMessage("Enter an amount to continue.", Color(0xFFED6C02))
                            isTooMuch -> InfoMessage("Amount exceeds balance.", Color(0xFFD32F2F))
                            amount == 0.0 -> InfoMessage("Amount cannot be zero.", Color(0xFFED6C02))
                            else -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Final Summary", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    PaymentSummaryRow("Amount to pay:", amount, Color(0xFF2E7D32))
                                    Spacer(Modifier.height(4.dp))
                                    PaymentSummaryRow("Balance after:", currentRemainingBalance - amount,
                                        if (amount >= currentRemainingBalance) Color(0xFF2E7D32) else Color(0xFFED6C02))
                                    if (amount >= currentRemainingBalance) {
                                        Spacer(Modifier.height(6.dp))
                                        Text("✓ Booking will be fully completed", fontSize = 11.sp,
                                            color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
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
    private fun PaymentInfoRow(label: String, amount: Double, color: Color) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = Color(0xFF757575))
            Text("₱${"%,.2f".format(amount)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }

    @Composable
    private fun PaymentSummaryRow(label: String, amount: Double, color: Color) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = Color(0xFF1B5E20))
            Text("₱${"%,.2f".format(amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
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
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Payment Proof",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A)
                )
            }

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
        val totalAmount = booking.room_details?.discounted_price ?: booking.area_details?.discounted_price
        val propertyType = if (booking.is_venue_booking) "Area" else "Room"
        val guestName = "${booking.user.first_name} ${booking.user.last_name}"
        val checkInDate = FormatDate.format(booking.check_in_date)
        val checkOutDate = FormatDate.format(booking.check_out_date)
        val createdAt = FormatDate.format(booking.created_at)
        val timeOfArrival = booking.time_of_arrival?.let { FormatTime.format(it) } ?: "08:00 AM"

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoRowDisplay("Guest", guestName)
            InfoRowDisplay(label = "Property", content = { PropertyChip(type = propertyType) })
            InfoRowDisplay("Check-in", checkInDate)
            InfoRowDisplay("Check-out", checkOutDate)
            InfoRowDisplay("ETA", timeOfArrival)
            InfoRowDisplay(label = "Status", content = { BookingStatusChip(status = booking.status) })
            InfoRowDisplay(
                label = "Amount",
                content = {
                    Text(
                        text = if (totalAmount.isNullOrEmpty())
                            "₱${"%,.2f".format(booking.total_price)}"
                        else totalAmount,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            )
            InfoRowDisplay("Booking Date", createdAt)
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

    @Composable
    fun RejectBookingDialog(
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var selectedReason by remember { mutableStateOf("") }
        var customReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Reject Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    Text("Select a reason for rejecting:", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Reject")
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
            title = { Text("Cancel Booking", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    Text("Select a reason for cancelling:", fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED6C02))
                ) {
                    Text("Cancel Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF757575))
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
            is BookingAction.Reserve -> "Reserve Booking" to "Are you sure you want to reserve this booking?"
            is BookingAction.CheckIn -> "Check In Guest" to "Are you sure you want to check in this guest?"
            is BookingAction.CheckOut -> "Check Out Guest" to "Are you sure you want to check out this guest?"
            is BookingAction.MarkNoShow -> "Mark as No Show" to "Are you sure you want to mark this booking as no show?"
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