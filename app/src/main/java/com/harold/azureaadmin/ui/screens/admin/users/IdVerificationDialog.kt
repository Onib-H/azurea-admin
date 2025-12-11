// Redesigned ID Verification Screen with card-based layout matching your Booking Details UI
// Buttons placed inside content, improved spacing, consistent colors, no bottom navbar look

package com.harold.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.harold.azureaadmin.ui.components.topbar.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdVerificationDialog(
    idType: String?,
    frontIdUrl: String?,
    backIdUrl: String?,
    applyDiscountDefault: Boolean = false,
    onDismiss: () -> Unit,
    onApprove: (Boolean) -> Unit,
    onReject: (String) -> Unit
) {
    val rejectionReasons = listOf(
        "Blurry or unclear image",
        "Incomplete ID",
        "ID expired",
        "Mismatched information",
        "Invalid ID type",
        "Edited or tampered image",
        "Glare or shadow",
        "Low resolution",
        "Duplicate submission",
        "Wrong document uploaded",
        "Other"
    )

    var applyDiscount by remember { mutableStateOf(applyDiscountDefault) }
    var showRejectDropdown by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var customReason by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                AppTopBar(
                    title = "ID Verification",
                    onBack = onDismiss
                )

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ID TYPE CARD - PROMINENT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Label
                            Text(
                                text = "ID Type: ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(Modifier.width(12.dp))

                            // Value Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = idType ?: "N/A",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }



                    Spacer(Modifier.height(20.dp))

                    // FRONT ID CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Front Side",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            AsyncImage(
                                model = frontIdUrl,
                                contentDescription = "Front ID",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // BACK ID CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Back Side",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            AsyncImage(
                                model = backIdUrl,
                                contentDescription = "Back ID",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // DISCOUNT OPTION CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = applyDiscount,
                                onCheckedChange = { applyDiscount = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Apply PWD / Senior Discount",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "20% off total price",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (showRejectDropdown) {
                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Rejection Reason",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedReason,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Select Rejection Reason") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        containerColor = Color.White // makes it white
                                    ) {
                                        rejectionReasons.forEach { reason ->
                                            DropdownMenuItem(
                                                text = { Text(reason) },
                                                onClick = {
                                                    selectedReason = reason
                                                    expanded = false
                                                },
                                                colors = MenuDefaults.itemColors(
                                                    textColor = MaterialTheme.colorScheme.onSurface
                                                )


                                            )
                                        }
                                    }
                                }

                                if (selectedReason == "Other") {
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = customReason,
                                        onValueChange = { customReason = it },
                                        label = { Text("Enter custom reason") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val reason = if (selectedReason == "Other") customReason else selectedReason
                                        if (reason.isNotEmpty()) {
                                            onReject(reason)
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    Text(
                                        "Submit Rejection",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ACTION BUTTONS (INSIDE CONTENT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDropdown = !showRejectDropdown },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Reject",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Button(
                            onClick = {
                                onApprove(applyDiscount)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Approve",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}