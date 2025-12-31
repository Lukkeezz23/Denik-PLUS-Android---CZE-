package com.example.denikplus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.denikplus.data.EntriesRepository
import com.example.denikplus.data.EntryItem
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

    private var yearJob: Job? = null

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _dayEntries = MutableStateFlow<List<EntryItem>>(emptyList())
    val dayEntries: StateFlow<List<EntryItem>> = _dayEntries

    private var dayJob: Job? = null

    init {
        startObserveYear(_year.value)
    }

    fun prevYear() = setYear(_year.value - 1)
    fun nextYear() = setYear(_year.value + 1)

    fun setYear(newYear: Int) {
        if (newYear == _year.value) return
        _year.value = newYear
        startObserveYear(newYear)

        // když přepneš rok, zavři detail dne
        closeDay()
    }

    private fun startObserveYear(year: Int) {
        yearJob?.cancel()
        yearJob = viewModelScope.launch {
            repo.observeYearCounts(uid, year).collect { map ->
                _counts.value = map
            }
        }
    }

    fun openDay(date: LocalDate) {
        _selectedDate.value = date

        dayJob?.cancel()
        dayJob = viewModelScope.launch {
            repo.observeDayEntries(uid, date).collect { list ->
                _dayEntries.value = list
            }
        }
    }

    fun closeDay() {
        _selectedDate.value = null
        _dayEntries.value = emptyList()

        dayJob?.cancel()
        dayJob = null
    }

    // ✅ vytvoření plnohodnotného zápisu
    fun addEntry(date: LocalDate, moodLabel: String, text: String) {
        repo.addEntry(uid, date, moodLabel, text)
    }

    // ✅ editace
    fun updateEntry(entryId: String, moodLabel: String, text: String) {
        repo.updateEntry(uid, entryId, moodLabel, text)
    }

    // ✅ mazání
    fun deleteEntry(entryId: String) {
        repo.deleteEntry(uid, entryId)
    }

    override fun onCleared() {
        super.onCleared()
        yearJob?.cancel()
        dayJob?.cancel()
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
