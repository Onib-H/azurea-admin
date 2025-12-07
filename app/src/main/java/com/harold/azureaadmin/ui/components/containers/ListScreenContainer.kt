package com.harold.azureaadmin.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.vector.ImageVector
import com.harold.azureaadmin.ui.components.states.EmptyState
import com.harold.azureaadmin.ui.components.states.ErrorState
import com.harold.azureaadmin.ui.components.filters.SearchFilterHeader

@Composable
fun <T> ListScreenContainer(
    title: String,
    searchPlaceholder: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    showFilter: Boolean,
    onFilterClick: () -> Unit,

    isRefreshing: Boolean,
    onRefresh: () -> Unit,

    showBlackout: Boolean,
    loading: Boolean,
    error: String?,
    items: List<T>,

    emptyIcon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String? = null,

    skeleton: @Composable () -> Unit,
    content: @Composable (List<T>) -> Unit
) {
    // ❗ Correct version – no parameters
    val pullState = rememberPullToRefreshState()

    Column(modifier = Modifier.fillMaxSize()) {

        SearchFilterHeader(
            title = title,
            searchPlaceholder = searchPlaceholder,
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            onFilterClick = onFilterClick,
            showFilter = showFilter
        )

        Box(modifier = Modifier.fillMaxSize()) {

            androidx.compose.animation.AnimatedVisibility(
                visible = !showBlackout,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                PullToRefreshBox(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        error != null ->
                            ErrorState(error = error, onRetry = onRefresh)

                        loading && items.isEmpty() -> skeleton()

                        items.isEmpty() ->
                            EmptyState(
                                icon = emptyIcon,
                                title = emptyTitle,
                                subtitle = emptySubtitle,
                                useScroll = true
                            )

                        else -> content(items)
                    }

                }
            }

            if (showBlackout) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                        .zIndex(10f)
                )
            }
        }
    }
}
