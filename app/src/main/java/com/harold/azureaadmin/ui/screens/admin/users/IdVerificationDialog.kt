package com.harold.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ID Verification",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Text(
                                text = idType ?: "N/A",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                        .padding(bottom = 80.dp)
                ) {
                    // Front ID - Full width stacked
                    Text(
                        text = "Front Side",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        AsyncImage(
                            model = frontIdUrl,
                            contentDescription = "Front ID",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Back ID - Full width stacked
                    Text(
                        text = "Back Side",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        AsyncImage(
                            model = backIdUrl,
                            contentDescription = "Back ID",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Discount Checkbox - Minimal with subtle background
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = applyDiscount,
                                onCheckedChange = { applyDiscount = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apply PWD / Senior Discount (20% off all bookings)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Rejection Section - Minimal design
                    if (showRejectDropdown) {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedReason,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Rejection Reason") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
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

                        // Custom reason
                        if (selectedReason == "Other") {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = customReason,
                                onValueChange = { customReason = it },
                                label = { Text("Enter custom reason") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Rejection - Minimal
                        Button(
                            onClick = {
                                val reasonToSend = if (selectedReason == "Other")
                                    customReason else selectedReason
                                if (reasonToSend.isNotEmpty()) {
                                    onReject(reasonToSend)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            ),
                            enabled = selectedReason.isNotEmpty() &&
                                    (selectedReason != "Other" || customReason.isNotEmpty()),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Submit Rejection",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Fixed Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDropdown = !showRejectDropdown },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Reject",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                onApprove(applyDiscount)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Approve",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}