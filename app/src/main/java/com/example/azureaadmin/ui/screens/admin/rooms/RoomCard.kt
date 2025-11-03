package com.example.azureaadmin.ui.screens.admin.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.azureaadmin.data.models.Room
import com.example.azureaadmin.ui.theme.BlueSecondaryDark
import com.example.azureaadmin.ui.theme.GreenAvailable
import com.example.azureaadmin.ui.theme.NeutralOnSurface
import com.example.azureaadmin.utils.FormatPrice

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomCard(
    room: Room,
    onViewClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.White // ✅ force pure white
        )

    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = room.images.firstOrNull()?.room_image ?: "",
                    contentDescription = room.room_name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                if (room.discount_percent > 0) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.large
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "${room.discount_percent}% OFF",
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 🔹 Status Badge (Available / Maintenance)
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(
                            color = when (room.status.lowercase()) {
                                "available" -> GreenAvailable
                                "maintenance" -> Color(0xFFFFC107) // amber yellow for maintenance
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = room.status.uppercase(),
                        color = when (room.status.lowercase()) {
                            "available" -> MaterialTheme.colorScheme.onPrimary
                            "maintenance" -> Color.Black
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

            }

            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = room.room_name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 🔹 Room Type Chip
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.HomeWork, // 🏠 room type icon
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF1E63D6)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFEAF3FF), // pastel blue background
                            labelColor = Color(0xFF1E63D6)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF1E63D6)),
                        label = {
                            Text(
                                text = room.room_type.uppercase(),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E63D6),
                                fontSize = 9.sp, // ✅ smaller font
                                maxLines = 1
                            )
                        }
                    )

                    // 🔹 Bed Type Chip
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Bed, // 🛏 bed type icon
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFDC6C00)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFFF4E6), // pastel orange background
                            labelColor = Color(0xFFDC6C00)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFDC6C00)),
                        label = {
                            Text(
                                text = room.bed_type.uppercase(),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDC6C00),
                                fontSize = 9.sp, // ✅ smaller font
                                maxLines = 1
                            )
                        }
                    )

                    // 🔹 Max Guests Chip
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.People,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF137A2D) // green icon
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFEFFAEF), // pastel green background
                            labelColor = Color(0xFF137A2D)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF137A2D)),
                        label = {
                            Text(
                                text = "Max Guests: ${room.max_guests}",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF137A2D),
                                fontSize = 9.sp, // ✅ smaller font
                                maxLines = 1
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (room.description.isNullOrBlank()) "No description provided" else room.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (room.discount_percent > 0) {
                    Text(
                        text = FormatPrice.formatPrice(room.price_per_night),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = FormatPrice.formatPrice(room.discounted_price_numeric ?: room.price_per_night),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = onViewClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(NeutralOnSurface, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Outlined.Visibility, contentDescription = "View", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(BlueSecondaryDark, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onPrimary)
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}
