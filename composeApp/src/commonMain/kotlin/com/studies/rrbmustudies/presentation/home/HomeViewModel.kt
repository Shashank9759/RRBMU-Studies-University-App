package com.studies.rrbmustudies.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.HomeCarouselSettings
import com.studies.rrbmustudies.domain.model.HomeDefaults
import com.studies.rrbmustudies.domain.model.Paper
import com.studies.rrbmustudies.domain.usecase.GetCoursesUseCase
import com.studies.rrbmustudies.domain.usecase.GetHomeAdsUseCase
import com.studies.rrbmustudies.domain.usecase.GetRecentPapersUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveAdminStateUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveHomeCarouselSettingsUseCase
import com.studies.rrbmustudies.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val ads: List<HomeAd> = HomeDefaults.promoAds,
    val courses: List<Course> = emptyList(),
    val recentPapers: List<Paper> = emptyList(),
    val carouselSettings: HomeCarouselSettings = HomeCarouselSettings(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getHomeAdsUseCase: GetHomeAdsUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getRecentPapersUseCase: GetRecentPapersUseCase,
    private val observeAdminStateUseCase: ObserveAdminStateUseCase,
    private val observeHomeCarouselSettingsUseCase: ObserveHomeCarouselSettingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeUiState>>(
        UiState.Loading,
    )
    val state: StateFlow<UiState<HomeUiState>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAdminStateUseCase.isAdmin.flatMapLatest { isAdmin ->
                combine(
                    getHomeAdsUseCase(adminMode = isAdmin)
                        .catch { emit(HomeDefaults.promoAds) },
                    getCoursesUseCase()
                        .catch { emit(emptyList()) },
                    getRecentPapersUseCase(limit = 6)
                        .catch { emit(emptyList()) },
                    observeHomeCarouselSettingsUseCase()
                        .catch { emit(HomeCarouselSettings()) },
                ) { ads, courses, recentPapers, carouselSettings ->
                    HomeUiState(
                        ads = ads.ifEmpty { HomeDefaults.promoAds },
                        courses = courses.take(8),
                        recentPapers = recentPapers.take(6),
                        carouselSettings = carouselSettings,
                    )
                }
            }
                .catch {
                    _state.value = UiState.Error("Couldn't load home content. Pull to refresh.")
                }
                .collect { data ->
                    _state.value = UiState.Success(data)
                }
        }
    }

    fun refresh() {
        if (_state.value is UiState.Error) _state.value = UiState.Loading
    }
}
