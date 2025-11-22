package com.example.azureaadmin.ui.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.azureaadmin.ui.components.button.DropdownField


@Composable
fun GuestIdVerificationDialog(
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // dimmed background
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Guest ID Verification",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = idType ?: "N/A",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // ID Images
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Front Side", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = frontIdUrl,
                                contentDescription = "Front ID",
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color(0xFFECECEC), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Back Side", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = backIdUrl,
                                contentDescription = "Back ID",
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color(0xFFECECEC), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Discount Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = applyDiscount,
                            onCheckedChange = { applyDiscount = it }
                        )
                        Text("Apply PWD / Senior Discount (20% off all bookings)")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDropdown = !showRejectDropdown },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Reject")
                        }

                        Button(
                            onClick = {
                                onApprove(applyDiscount)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Approve")
                        }
                    }

                    // Rejection Dropdown
                    if (showRejectDropdown) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DropdownField(
                            label = "Select Rejection Reason",
                            value = selectedReason,
                            options = rejectionReasons,
                            onSelect = { selectedReason = it }
                        )

                        if (selectedReason == "Other") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customReason,
                                onValueChange = { customReason = it },
                                label = { Text("Enter custom reason") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val reasonToSend =
                                    if (selectedReason == "Other") customReason else selectedReason
                                onReject(reasonToSend)
                                showRejectDropdown = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Rejection")
                        }
                    }
                }
            }
        }
    }
}


