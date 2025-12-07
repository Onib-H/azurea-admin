package com.harold.azureaadmin.ui.components.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.harold.azureaadmin.data.models.AreaDetail
import com.harold.azureaadmin.data.models.RoomDetail
import com.harold.azureaadmin.data.models.Amenity

// Sealed class to represent either Area or Room details


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShowItemDialog(
    itemDetail: ItemDetail?,
    onDismiss: () -> Unit
) {
    if (itemDetail == null) return

    // Extract common properties
    val (name, description, status, images, imageCount) = when (itemDetail) {
        is ItemDetail.AreaItem -> {
            val area = itemDetail.area
            ItemProperties(
                name = area.area_name,
                description = area.description,
                status = area.status,
                images = area.images.map { it.area_image },
                imageCount = area.images.size
            )
        }
        is ItemDetail.RoomItem -> {
            val room = itemDetail.room
            ItemProperties(
                name = room.room_name,
                description = room.description,
                status = room.status,
                images = room.images.map { it.room_image },
                imageCount = room.images.size
            )
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
            val pagerState = rememberPagerState { imageCount }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Image Slider (Fixed at top, not scrollable)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Image counter
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / $imageCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Content area - now with proper scrolling
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        color = Color(0xFFF8F9FA),
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp)
                        ) {
                            // Title + Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when {
                                        status.equals("available", true) -> Color(0xFFE8F5E8)
                                        status.equals("maintenance", true) -> Color(0xFFFFF3CD)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                ) {
                                    Text(
                                        text = status.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when {
                                            status.equals("available", true) -> Color(0xFF2E7D32)
                                            status.equals("maintenance", true) -> Color(0xFF8C6D1F)
                                            else -> Color(0xFFC62828)
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Description
                            Text(
                                text = if (description.isNullOrBlank()) "No description provided" else description,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF666666),
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(24.dp))

                            // Details cards - different for Area vs Room
                            when (itemDetail) {
                                is ItemDetail.AreaItem -> {
                                    AreaDetailsCards(itemDetail.area)
                                }
                                is ItemDetail.RoomItem -> {
                                    RoomDetailsCards(itemDetail.room)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Amenities section (Room only)
                            if (itemDetail is ItemDetail.RoomItem && itemDetail.room.amenities.isNotEmpty()) {
                                AmenitiesSection(itemDetail.room.amenities)
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // Booking info (Area only)
                            if (itemDetail is ItemDetail.AreaItem) {
                                BookingInfoSection(itemDetail.area)
                            }

                            // Add bottom padding for better scroll experience
                            Spacer(modifier = Modifier.height(24.dp))
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
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaDetailsCards(area: AreaDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.People,
            label = "Capacity",
            value = "${area.capacity} people",
            backgroundColor = Color(0xFFF8FFF9),
            borderColor = Color(0xFF43A047), // brighter green
        )

        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AttachMoney,
            label = "Price",
            value = area.price_per_hour,
            backgroundColor = Color(0xFFFFFEF2),
            borderColor = Color(0xFFFFC107), // bright amber
        )
    }
}

@Composable
private fun RoomDetailsCards(room: RoomDetail) {
    // First row - Room Type and Bed Type
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Hotel,
            label = "Room Type",
            value = room.room_type.replaceFirstChar { it.uppercase() },
            backgroundColor = Color(0xFFF4F9FF),
            borderColor = Color(0xFF1E88E5), // bright blue
        )

        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Bed,
            label = "Bed Type",
            value = room.bed_type.replaceFirstChar { it.uppercase() },
            backgroundColor = Color(0xFFFFF4E6),
            borderColor = Color(0xFFDC6C00), // bright amber
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Second row - Max Guests and Price per Night
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.People,
            label = "Max Guests",
            value = "${room.max_guests} guests",
            backgroundColor = Color(0xFFF8FFF9),
            borderColor = Color(0xFF43A047), // brighter green
        )

        DetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AttachMoney,
            label = "Price per Night",
            value = room.room_price,
            backgroundColor = Color(0xFFFFFEF2),
            borderColor = Color(0xFFFFC107), // bright amber
        )
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color = Color.Black
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = borderColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
            }
        }
    }
}

@Composable
private fun AmenitiesSection(amenities: List<Amenity>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3E5F5),
        border = BorderStroke(1.dp, Color(0xFFAB47BC))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "AMENITIES",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xCC6A1B9A),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            amenities.forEach { amenity ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        amenity.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingInfoSection(area: AreaDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE1F5FE),
        border = BorderStroke(1.dp, Color(0xFF039BE5))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "BOOKING INFORMATION",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xCC01579B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "This venue is available for fixed hours (8:00 AM - 5:00 PM) and can be booked for ${area.price_per_hour} per booking.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// Helper data class
private data class ItemProperties(
    val name: String,
    val description: String,
    val status: String,
    val images: List<String>,
    val imageCount: Int
)