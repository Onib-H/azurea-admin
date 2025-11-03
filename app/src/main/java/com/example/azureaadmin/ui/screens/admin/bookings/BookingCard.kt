package com.example.azureaadmin.ui.screens.admin.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.azureaadmin.data.models.BookingData
import com.example.azureaadmin.utils.FormatDate

@Composable
fun BookingCard(
    booking: BookingData,
    onViewClick: () -> Unit = {}
) {


    val propertyName = booking.room_details?.room_name ?: booking.area_details?.area_name ?: "Unknown"
    val totalAmount = booking.room_details?.discounted_price ?: booking.area_details?.discounted_price
    val propertyType = if (booking.is_venue_booking) "AREA" else "ROOM"
    val propertyImage = booking.room_details?.images?.firstOrNull()?.room_image
        ?: booking.area_details?.images?.firstOrNull()?.area_image
    val guestName = "${booking.user.first_name} ${booking.user.last_name}"
    val bookingDate = FormatDate.format(booking.created_at)
    val checkInDate = FormatDate.format(booking.check_in_date)
    val checkOutDate = FormatDate.format(booking.check_out_date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Top Row: image + property info
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = propertyImage ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = propertyName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PropertyChip(propertyType)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guest + booking date + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = guestName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bookingDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BookingStatusChip(booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Check-in / Check-out
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Check-in", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = checkInDate,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Check-out", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = checkOutDate,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Bottom row: price and view button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (totalAmount.isNullOrEmpty())
                        "₱${"%,.2f".format(booking.total_price)}"
                    else totalAmount,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )


                IconButton(
                    onClick = onViewClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "View Booking",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun PropertyChip(type: String) {
    val (bgColor, textColor) = when (type.lowercase()) {
        "room" -> Color(0xFFE3F6E8) to Color(0xFF0E7A34)   // soft mint green
        "area" -> Color(0xFFE4EBFF) to Color(0xFF204ECF)   // cool blue
        else -> Color(0xFFF2F2F2) to Color(0xFF333333)
    }
    StatusChip(text = type, backgroundColor = bgColor, textColor = textColor)
}

@Composable
fun BookingStatusChip(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "checked_in" -> Color(0xFFDCEBFF) to Color(0xFF0C56B0)   // deeper blue
        "pending" -> Color(0xFFFFF3CD) to Color(0xFF946200)      // warm yellow
        "reserved" -> Color(0xFFE4F9F2) to Color(0xFF008A5C)     // teal-green
        "cancelled" -> Color(0xFFFFE6E6) to Color(0xFFD32F2F)
        else -> Color(0xFFF2F2F2) to Color(0xFF333333)
    }
    StatusChip(text = status, backgroundColor = bgColor, textColor = textColor)
}
