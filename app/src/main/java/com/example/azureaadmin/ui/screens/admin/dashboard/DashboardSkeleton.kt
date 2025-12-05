package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.azureaadmin.ui.components.skeletons.SkeletonBox

@Composable
fun DashboardSkeleton() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header skeleton
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(0.5f)
                )
                SkeletonBox(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth(0.7f)
                )
            }
        }

        // Stats cards skeleton
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                repeat(2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            SkeletonBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(150.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(500.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(800.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(350.dp)
            )
        }
    }
}