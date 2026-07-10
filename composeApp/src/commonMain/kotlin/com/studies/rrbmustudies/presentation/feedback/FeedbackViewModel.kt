package com.studies.rrbmustudies.presentation.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studies.rrbmustudies.domain.model.Feedback
import com.studies.rrbmustudies.domain.usecase.SubmitFeedbackUseCase
import com.studies.rrbmustudies.ui.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedbackUiState(
    val rating: Int = 0,
    val comment: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
)

class FeedbackViewModel(
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    fun onRatingChange(rating: Int) {
        _state.update { it.copy(rating = rating, error = null) }
    }

    fun onCommentChange(comment: String) {
        _state.update { it.copy(comment = comment, error = null) }
    }

    fun onTagToggle(tag: String) {
        _state.update { current ->
            val tags = current.selectedTags.toMutableSet()
            if (tag in tags) tags.remove(tag) else tags.add(tag)
            current.copy(selectedTags = tags)
        }
    }

    fun submit() {
        val current = _state.value
        if (current.rating == 0) {
            _state.update { it.copy(error = "Please select a rating") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            val result = submitFeedbackUseCase(
                Feedback(
                    rating = current.rating,
                    comment = current.comment.trim(),
                    tags = current.selectedTags.toList(),
                ),
            )
            result.fold(
                onSuccess = { _state.value = FeedbackUiState(isSubmitted = true) },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.toUserMessage("Couldn't submit feedback. Try again."),
                        )
                    }
                },
            )
        }
    }
}
