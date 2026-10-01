package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.JomaRecordEntity
import com.example.data.local.MemberEntity
import com.example.data.repository.JomaRepository
import com.example.ui.components.BengaliNumberUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalMembers: Int = 0,
    val totalJoma: Double = 0.0,
    val todayJoma: Double = 0.0,
    val todayDateString: String = BengaliNumberUtils.getTodayDateString(),
    val members: List<MemberEntity> = emptyList(),
    val todayDepositedMemberIds: Set<Long> = emptySet(),
    val memberTotals: Map<Long, Double> = emptyMap(),
    val memberRecordCounts: Map<Long, Int> = emptyMap(),
    val recentRecords: List<JomaRecordEntity> = emptyList()
)

data class AdminProfile(
    val name: String = "Md Firoz Islam",
    val title: String = "Admin: ফিরোজ ভাই",
    val phone: String = "01752063433"
)

class JomaViewModel(
    application: Application,
    private val repository: JomaRepository
) : AndroidViewModel(application) {

    private val _adminProfile = MutableStateFlow(AdminProfile())
    val adminProfile: StateFlow<AdminProfile> = _adminProfile.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _selectedMemberForDetail = MutableStateFlow<MemberEntity?>(null)
    val selectedMemberForDetail: StateFlow<MemberEntity?> = _selectedMemberForDetail.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val jomaRecords: StateFlow<List<JomaRecordEntity>> = repository.allJomaRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dashboardState: StateFlow<DashboardUiState> = combine(
        members,
        jomaRecords
    ) { memberList, records ->
        val todayStr = BengaliNumberUtils.getTodayDateString()
        val totalMembers = memberList.size
        val totalJoma = records.sumOf { it.amount }
        val todayRecords = records.filter { it.dateString == todayStr }
        val todayJoma = todayRecords.sumOf { it.amount }
        val todayMemberIds = todayRecords.map { it.memberId }.toSet()

        val memberTotals = mutableMapOf<Long, Double>()
        val memberRecordCounts = mutableMapOf<Long, Int>()

        for (record in records) {
            memberTotals[record.memberId] = (memberTotals[record.memberId] ?: 0.0) + record.amount
            memberRecordCounts[record.memberId] = (memberRecordCounts[record.memberId] ?: 0) + 1
        }

        DashboardUiState(
            totalMembers = totalMembers,
            totalJoma = totalJoma,
            todayJoma = todayJoma,
            todayDateString = todayStr,
            members = memberList,
            todayDepositedMemberIds = todayMemberIds,
            memberTotals = memberTotals,
            memberRecordCounts = memberRecordCounts,
            recentRecords = records.take(10)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun addMember(name: String, phone: String, onComplete: () -> Unit = {}) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            viewModelScope.launch {
                _snackbarEvent.emit("অনুগ্রহ করে সদস্যের নাম লিখুন!")
            }
            return
        }

        viewModelScope.launch {
            repository.addMember(trimmedName, phone.trim())
            _snackbarEvent.emit("সদস্য '$trimmedName' সফলভাবে যোগ করা হয়েছে")
            onComplete()
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member.id)
            _snackbarEvent.emit("সদস্য '${member.name}' এবং জমার হিসাব মুছে ফেলা হয়েছে")
        }
    }

    fun addDeposit(
        memberId: Long,
        memberName: String,
        amount: Double,
        dateString: String = BengaliNumberUtils.getTodayDateString(),
        note: String = ""
    ) {
        if (amount <= 0) {
            viewModelScope.launch {
                _snackbarEvent.emit("টাকার পরিমাণ সঠিক নয়!")
            }
            return
        }

        viewModelScope.launch {
            repository.addJoma(
                memberId = memberId,
                memberName = memberName,
                amount = amount,
                dateString = dateString,
                note = note
            )
            val takaBangla = BengaliNumberUtils.toBangla(amount)
            _snackbarEvent.emit("$memberName এর জন্য $takaBangla টাকা জমা যোগ হয়েছে ✓")
        }
    }

    fun quickDeposit100(member: MemberEntity) {
        addDeposit(
            memberId = member.id,
            memberName = member.name,
            amount = 100.0,
            dateString = BengaliNumberUtils.getTodayDateString(),
            note = "দৈনিক নিয়মিত জমা"
        )
    }

    fun depositAllRemaining100(membersToDeposit: List<MemberEntity>) {
        if (membersToDeposit.isEmpty()) return
        viewModelScope.launch {
            val todayStr = BengaliNumberUtils.getTodayDateString()
            for (member in membersToDeposit) {
                repository.addJoma(
                    memberId = member.id,
                    memberName = member.name,
                    amount = 100.0,
                    dateString = todayStr,
                    note = "সকলের দৈনিক জমা"
                )
            }
            _snackbarEvent.emit("এক ক্লিকে ${BengaliNumberUtils.toBangla(membersToDeposit.size)} জন সদস্যের ১০০ টাকা জমা সফল!")
        }
    }

    fun deleteDeposit(record: JomaRecordEntity) {
        viewModelScope.launch {
            repository.deleteJoma(record.id)
            _snackbarEvent.emit("${record.memberName} এর ${BengaliNumberUtils.toBangla(record.amount)} টাকার জমা বাতিল করা হয়েছে")
        }
    }

    fun updateAdminProfile(name: String, title: String, phone: String) {
        _adminProfile.value = AdminProfile(
            name = name.ifBlank { "Md Firoz Islam" },
            title = title.ifBlank { "Admin: ফিরোজ ভাই" },
            phone = phone.ifBlank { "01752063433" }
        )
        viewModelScope.launch {
            _snackbarEvent.emit("অ্যাডমিন প্রোফাইল আপডেট হয়েছে")
        }
    }

    fun selectMemberForDetail(member: MemberEntity?) {
        _selectedMemberForDetail.value = member
    }

    fun generateShareableSummary(): String {
        val state = dashboardState.value
        val admin = adminProfile.value
        val todayBnDate = BengaliNumberUtils.formatDateToDisplay(state.todayDateString)
        val todayJomaBn = BengaliNumberUtils.formatTaka(state.todayJoma)
        val totalJomaBn = BengaliNumberUtils.formatTaka(state.totalJoma)
        val membersCountBn = BengaliNumberUtils.toBangla(state.totalMembers)
        val paidCount = state.todayDepositedMemberIds.size
        val paidCountBn = BengaliNumberUtils.toBangla(paidCount)

        val sb = StringBuilder()
        sb.append("📋 *দৈনিক জমা হিসাব রিপোর্ট*\n")
        sb.append("📅 তারিখ: $todayBnDate\n")
        sb.append("👤 ${admin.title} (${admin.name})\n")
        sb.append("----------------------------\n")
        sb.append("👥 মোট সদস্য: $membersCountBn জন\n")
        sb.append("✅ আজকের জমা দিয়েছে: $paidCountBn জন\n")
        sb.append("💰 আজকের মোট জমা: $todayJomaBn\n")
        sb.append("🏦 সর্বমোট জমা তহবিল: $totalJomaBn\n")
        sb.append("----------------------------\n")
        sb.append("📝 সদস্যওয়ারী আজকের স্ট্যাটাস:\n")

        for (member in state.members) {
            val isPaid = state.todayDepositedMemberIds.contains(member.id)
            val icon = if (isPaid) "✅ জমা হয়েছে (১০০৳)" else "⏳ বাকি আছে"
            sb.append("• ${member.name}: $icon\n")
        }

        sb.append("\nধন্যবাদ,\n${admin.name}")
        return sb.toString()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = JomaRepository(db.jomaDao())
                    return JomaViewModel(application, repo) as T
                }
            }
    }
}
