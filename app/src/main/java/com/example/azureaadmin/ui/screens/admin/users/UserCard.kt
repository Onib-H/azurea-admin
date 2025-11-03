package com.example.azureaadmin.ui.screens.admin.users


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.azureaadmin.data.models.User
import com.example.azureaadmin.ui.components.modals.DeleteItemDialog

@Composable
fun UserCard(
    user: User,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image with Verification Indicator
            Box(
                modifier = Modifier.size(64.dp)
            ) {
                AsyncImage(
                    model = user.profile_image,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = when (user.is_verified) {
                                "verified" -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                "pending" -> Color(0xFFFFA726).copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                // Verification Badge Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp),
                    shape = CircleShape,
                    color = when (user.is_verified) {
                        "verified" -> Color(0xFF4CAF50)
                        "pending" -> Color(0xFFFFA726)
                        else -> Color(0xFF9E9E9E)
                    },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (user.is_verified) {
                                "verified" -> Icons.Outlined.CheckCircle
                                "pending" -> Icons.Outlined.Schedule
                                else -> Icons.Outlined.Cancel
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = "${user.first_name} ${user.last_name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Email with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status Tags Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Verification Status
                    StatusChip(
                        text = when (user.is_verified) {
                            "verified" -> "Verified"
                            "pending" -> "Pending"
                            else -> "Unverified"
                        },
                        color = when (user.is_verified) {
                            "verified" -> Color(0xFF4CAF50)
                            "pending" -> Color(0xFFFFA726)
                            else -> Color(0xFF9E9E9E)
                        }
                    )

                    // Senior/PWD Badge
                    if (user.is_senior_or_pwd) {
                        StatusChip(
                            text = "SENIOR/PWD",
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    DeleteItemDialog(
        show = showDeleteDialog,
        itemLabel = "User",
        onDismiss = { showDeleteDialog = false },
        onDelete = {
            showDeleteDialog = false
            onDeleteClick()
        },
        icon = Icons.Filled.Warning,
        title = "Archive User",
        description = "Are you sure you want to archive this user?",
        confirmButtonText = "Archive"
    )
}

@Composable
private fun StatusChip(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            letterSpacing = 0.3.sp
        )
    }
}