package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.harold.azureaadmin.data.models.BookingData
import com.harold.azureaadmin.ui.theme.AzureaColors
import com.harold.azureaadmin.utils.FormatDate
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun BookingCard(
    booking: BookingData,
    onViewClick: () -> Unit = {},
) {
    val propertyName = booking.room_details?.room_name
        ?: booking.area_details?.area_name
        ?: "Unknown"

    val image = booking.room_details?.images?.firstOrNull()?.room_image
        ?: booking.area_details?.images?.firstOrNull()?.area_image

    val guestName = "${booking.user.first_name} ${booking.user.last_name}"
    val isVerified = booking.user.is_verified == "verified"

    val checkIn = FormatDate.format(booking.check_in_date)
    val checkOut = FormatDate.format(booking.check_out_date)
    val bookingDate = formatDateTime(booking.created_at)

    val propertyType = if (booking.is_venue_booking) "AREA" else "ROOM"

    val price = "₱${"%,.2f".format(booking.total_price)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = image ?: "",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = propertyName,
                        color = AzureaColors.NeutralDark,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    PropertyChip(propertyType)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzureaColors.NeutralLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        guestName,
                        color = AzureaColors.NeutralDark,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        booking.user.email,
                        color = AzureaColors.NeutralMedium,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isVerified) {
                    VerifiedBadge()
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {

                Row(modifier = Modifier.weight(1f)) {

                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = AzureaColors.NeutralMedium,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Booked: $bookingDate",
                        color = AzureaColors.NeutralMedium,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                BookingStatusChip(booking.status)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                CardDateBlock(
                    label = "Check-in",
                    value = checkIn,
                    bg = AzureaColors.PurpleLighter,
                    labelColor = AzureaColors.Purple,
                    modifier = Modifier.weight(1f)
                )

                CardDateBlock(
                    label = "Check-out",
                    value = checkOut,
                    bg = AzureaColors.WarningLight,
                    labelColor = AzureaColors.Warning,
                    modifier = Modifier.weight(1f)
                )
            }


            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = AzureaColors.NeutralMedium,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        booking.payment_method?.replaceFirstChar { it.uppercase() } ?: "—",
                        color = AzureaColors.NeutralMedium,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    price,
                    color = AzureaColors.NeutralDark,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }


            Button(
                onClick = onViewClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AzureaColors.Purple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(6.dp))
                Text("View", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}



@Composable
fun VerifiedBadge() {
    Box(
        modifier = Modifier
            .background(AzureaColors.SuccessLight, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = AzureaColors.Success,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Verified",
                color = AzureaColors.Success,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



@Composable
fun CardDateBlock(
    label: String,
    value: String,
    bg: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                label,
                color = labelColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                color = AzureaColors.NeutralDark,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,          // ✔ prevent wrapping
                softWrap = false       // ✔ truly forces single line
            )
        }
    }
}




@Composable
fun StatusChip(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun PropertyChip(type: String) {
    val (bg, text) = when (type.lowercase()) {
        "room" -> AzureaColors.PurpleLighter to AzureaColors.Purple
        "area" -> Color(0xFFE6EDFF) to Color(0xFF2F48C9)
        else -> AzureaColors.NeutralLight to AzureaColors.NeutralDark
    }
    StatusChip(type, bg, text)
}

@Composable
fun BookingStatusChip(status: String) {
    val (displayText, bg, textColor) = when (val lowerStatus = status.lowercase()) {
        "checked_in" -> Triple("Check in", AzureaColors.PurpleLight, AzureaColors.Purple)
        "pending" -> Triple("Pending", AzureaColors.WarningLight, AzureaColors.Warning)
        "reserved" -> Triple("Reserved", AzureaColors.SuccessLight, AzureaColors.Success)
        "cancelled" -> Triple("Cancelled", AzureaColors.ErrorLight, AzureaColors.Error)
        "rejected" -> Triple("Rejected", AzureaColors.ErrorLight, AzureaColors.Error)
        "checked_out" -> Triple("Check out", AzureaColors.PurpleLight, AzureaColors.Purple)
        "no_show" -> Triple("No show", AzureaColors.ErrorLight, AzureaColors.Error)
        else -> Triple(
            lowerStatus.replace("_", " ").split(" ").joinToString(" ") { it.capitalize() },
            AzureaColors.NeutralLight,
            AzureaColors.NeutralDark
        )
    }
    StatusChip(displayText, bg, textColor)
}

private fun formatDateTime(input: String?): String {
    if (input.isNullOrBlank()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val date = inputFormat.parse(input)

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        date?.let {
            "${dateFormat.format(it)}\nat ${timeFormat.format(it)}"
        } ?: "N/A"
    } catch (e: Exception) {
        // Fallback for different format
        try {
            val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat2.parse(input)
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            date?.let {
                "${dateFormat.format(it)}\nat ${timeFormat.format(it)}"
            } ?: "N/A"
        } catch (e2: Exception) {
            "N/A"
        }
    }
}
