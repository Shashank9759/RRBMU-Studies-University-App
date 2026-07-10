package com.studies.rrbmustudies.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studies.rrbmustudies.domain.model.AppNotification
import com.studies.rrbmustudies.ui.components.AdBannerSlot
import com.studies.rrbmustudies.ui.components.EmptyNotificationState
import com.studies.rrbmustudies.ui.components.ErrorState
import com.studies.rrbmustudies.ui.components.FilterChip
import com.studies.rrbmustudies.ui.components.FilterChipRow
import com.studies.rrbmustudies.ui.components.LoadingShimmer
import com.studies.rrbmustudies.ui.components.NotificationItem
import com.studies.rrbmustudies.ui.components.SearchBar
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.theme.RrbmuDimens
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(
    onNotificationClick: (AppNotification) -> Unit,
    onOpenSettings: () -> Unit = {},
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing = state is UiState.Loading
    val pullRefreshState = rememberPullRefreshState(isRefreshing, viewModel::refresh)

    Scaffold { padding ->
        when (val uiState = state) {
            is UiState.Loading -> LoadingShimmer(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(
                message = uiState.message,
                onRetry = uiState.retry ?: viewModel::refresh,
                modifier = Modifier.padding(padding),
            )
            is UiState.Success -> {
                val data = uiState.data
                val filtered = viewModel.filteredNotifications(data)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                        .pullRefresh(pullRefreshState),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = RrbmuDimens.screenHorizontal,
                                vertical = RrbmuDimens.spacingSm,
                            ),
                        )

                        SearchBar(
                            query = data.query,
                            onQueryChange = viewModel::onQueryChange,
                            placeholder = "Search alerts...",
                            showFilterIcon = false,
                        )

                        FilterChipRow {
                            NotificationFilter.entries.forEach { filter ->
                                FilterChip(
                                    label = filter.label,
                                    selected = data.filter == filter,
                                    onClick = { viewModel.onFilterChange(filter) },
                                )
                            }
                        }

                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmptyNotificationState(onSettingsClick = onOpenSettings)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    horizontal = RrbmuDimens.screenHorizontal,
                                    vertical = RrbmuDimens.spacingSm,
                                ),
                                verticalArrangement = Arrangement.spacedBy(RrbmuDimens.listItemGap),
                            ) {
                                items(filtered, key = { it.id }) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        isUnread = notification.id !in data.readIds,
                                        timestamp = formatNotificationTime(notification.createdAt),
                                        onClick = {
                                            viewModel.markRead(notification.id)
                                            onNotificationClick(notification)
                                        },
                                    )
                                }
                            }
                            AdBannerSlot()
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}
