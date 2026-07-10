package com.studies.rrbmustudies.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.HomeCarouselSettings
import com.studies.rrbmustudies.domain.usecase.CreateHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.DeleteHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.GetHomeAdsUseCase
import com.studies.rrbmustudies.domain.usecase.ObserveHomeCarouselSettingsUseCase
import com.studies.rrbmustudies.domain.usecase.ReorderHomeAdsUseCase
import com.studies.rrbmustudies.domain.usecase.SaveHomeCarouselSettingsUseCase
import com.studies.rrbmustudies.domain.usecase.UpdateHomeAdUseCase
import com.studies.rrbmustudies.domain.usecase.UploadHomeAdImageUseCase
import com.studies.rrbmustudies.ui.state.UiState
import com.studies.rrbmustudies.ui.util.isValidHttpUrl
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ManageHomeAdsViewModel(
    private val getHomeAdsUseCase: GetHomeAdsUseCase,
    private val createHomeAdUseCase: CreateHomeAdUseCase,
    private val updateHomeAdUseCase: UpdateHomeAdUseCase,
    private val deleteHomeAdUseCase: DeleteHomeAdUseCase,
    private val uploadHomeAdImageUseCase: UploadHomeAdImageUseCase,
    private val observeHomeCarouselSettingsUseCase: ObserveHomeCarouselSettingsUseCase,
    private val saveHomeCarouselSettingsUseCase: SaveHomeCarouselSettingsUseCase,
    private val reorderHomeAdsUseCase: ReorderHomeAdsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<HomeAd>>>(UiState.Loading)
    val state: StateFlow<UiState<List<HomeAd>>> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    private val _carouselSettings = MutableStateFlow(HomeCarouselSettings())
    val carouselSettings: StateFlow<HomeCarouselSettings> = _carouselSettings.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            observeHomeCarouselSettingsUseCase()
                .catch { emit(HomeCarouselSettings()) }
                .collect { _carouselSettings.value = it }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            getHomeAdsUseCase(adminMode = true)
                .catch { e ->
                    _state.value = UiState.Error(
                        e.toUserMessage("Couldn't load ads. Pull to refresh."),
                    ) { refresh() }
                }
                .collect { ads -> _state.value = UiState.Success(ads.sortedBy { it.order }) }
        }
    }

    fun uploadImage(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            _isUploadingImage.value = true
            uploadHomeAdImageUseCase(bytes, fileName).fold(
                onSuccess = { url ->
                    _uploadedImageUrl.value = url
                    _actionMessage.value = "Image uploaded — preview updated"
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't upload image. Try again.")
                },
            )
            _isUploadingImage.value = false
        }
    }

    fun clearUploadedImageUrl() {
        _uploadedImageUrl.value = null
    }

    fun saveAd(ad: HomeAd) {
        viewModelScope.launch {
            val link = ad.linkUrl.trim()
            if (link.isNotBlank() && !isValidHttpUrl(link)) {
                _actionMessage.value = "Link URL must start with http:// or https://"
                return@launch
            }
            val pendingUpload = _uploadedImageUrl.value?.trim().orEmpty()
            val image = ad.imageUrl.trim().ifBlank { pendingUpload }
            if (image.isBlank()) {
                _actionMessage.value = "Please upload a banner image first."
                return@launch
            }
            if (ad.title.isBlank()) {
                _actionMessage.value = "Please enter a title."
                return@launch
            }
            val currentAds = (_state.value as? UiState.Success)?.data.orEmpty()
            val order = if (ad.id.isBlank()) currentAds.size else ad.order
            val normalized = ad.copy(
                imageUrl = image,
                linkUrl = link,
                title = ad.title.trim(),
                order = order,
            )
            val result = if (normalized.id.isBlank()) {
                createHomeAdUseCase(normalized)
            } else {
                updateHomeAdUseCase(normalized)
            }
            result.fold(
                onSuccess = {
                    _actionMessage.value = "Ad saved"
                    refresh()
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't save ad. Try again.")
                },
            )
        }
    }

    fun deleteAd(adId: String) {
        viewModelScope.launch {
            deleteHomeAdUseCase(adId).fold(
                onSuccess = {
                    _actionMessage.value = "Ad deleted"
                    refresh()
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't delete ad. Try again.")
                },
            )
        }
    }

    fun moveAdToTop(adId: String) {
        val ads = (_state.value as? UiState.Success)?.data ?: return
        val target = ads.firstOrNull { it.id == adId } ?: return
        val reordered = listOf(target) + ads.filterNot { it.id == adId }
        applyReorder(reordered)
    }

    fun moveAdUp(adId: String) {
        val ads = (_state.value as? UiState.Success)?.data?.toMutableList() ?: return
        val index = ads.indexOfFirst { it.id == adId }
        if (index <= 0) return
        ads[index - 1] = ads[index].also { ads[index] = ads[index - 1] }
        applyReorder(ads)
    }

    fun moveAdDown(adId: String) {
        val ads = (_state.value as? UiState.Success)?.data?.toMutableList() ?: return
        val index = ads.indexOfFirst { it.id == adId }
        if (index < 0 || index >= ads.lastIndex) return
        ads[index + 1] = ads[index].also { ads[index] = ads[index + 1] }
        applyReorder(ads)
    }

    private fun applyReorder(ordered: List<HomeAd>) {
        viewModelScope.launch {
            val withOrder = ordered.mapIndexed { index, ad -> ad.copy(order = index) }
            _state.value = UiState.Success(withOrder)
            reorderHomeAdsUseCase(withOrder).fold(
                onSuccess = { _actionMessage.value = "Order updated" },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't update order. Try again.")
                    refresh()
                },
            )
        }
    }

    fun saveCarouselInterval(seconds: Int) {
        viewModelScope.launch {
            val settings = HomeCarouselSettings(seconds.coerceIn(2, 60))
            saveHomeCarouselSettingsUseCase(settings).fold(
                onSuccess = {
                    _carouselSettings.value = settings
                    _actionMessage.value = "Slide duration saved"
                },
                onFailure = { e ->
                    _actionMessage.value = e.toUserMessage("Couldn't save slide duration.")
                },
            )
        }
    }

    fun clearMessage() { _actionMessage.value = null }
}
