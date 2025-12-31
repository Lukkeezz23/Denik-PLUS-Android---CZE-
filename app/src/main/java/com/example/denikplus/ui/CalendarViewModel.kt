package com.example.denikplus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.denikplus.data.EntriesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(
    private val uid: String,
    private val repo: EntriesRepository
) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
    val year: StateFlow<Int> = _year

    private val _counts = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val counts: StateFlow<Map<LocalDate, Int>> = _counts

    private var job: Job? = null

    init {
        startObserve(_year.value)
    }

    fun prevYear() = setYear(_year.value - 1)
    fun nextYear() = setYear(_year.value + 1)

    fun setYear(newYear: Int) {
        if (newYear == _year.value) return
        _year.value = newYear
        startObserve(newYear)
    }

    private fun startObserve(y: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            repo.observeYearCounts(uid, y).collect { map ->
                _counts.value = map
            }
        }
    }

    fun addEntry(date: LocalDate, moodLabel: String) {
        repo.addEntry(uid, date, moodLabel)
    }
}

class CalendarViewModelFactory(
    private val uid: String,
    private val repo: EntriesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(uid, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
