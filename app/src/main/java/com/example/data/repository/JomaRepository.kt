package com.example.data.repository

import com.example.data.local.JomaDao
import com.example.data.local.JomaRecordEntity
import com.example.data.local.MemberEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JomaRepository(private val dao: JomaDao) {

    val allMembers: Flow<List<MemberEntity>> = dao.getAllMembers()
    val allJomaRecords: Flow<List<JomaRecordEntity>> = dao.getAllJomaRecords()

    suspend fun checkAndSeedInitialData() {
        if (dao.getMemberCount() == 0) {
            val defaultMembers = listOf(
                MemberEntity(name = "রমজান", phone = "01774237994"),
                MemberEntity(name = "সরিফুল", phone = "01959160781"),
                MemberEntity(name = "সনন", phone = "+8801738217755"),
                MemberEntity(name = "ফিরোজ", phone = "01752063433"),
                MemberEntity(name = "সোহাগ", phone = "01986830725")
            )
            dao.insertMembers(defaultMembers)

            // Add initial deposit record as in the user's template
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())
            dao.insertJoma(
                JomaRecordEntity(
                    memberId = 4, // "ফিরোজ"
                    memberName = "ফিরোজ",
                    amount = 100.0,
                    dateString = todayStr,
                    note = "নিয়মিত দৈনিক জমা"
                )
            )
        }
    }

    suspend fun addMember(name: String, phone: String): Long {
        val member = MemberEntity(
            name = name.trim(),
            phone = phone.trim(),
            joinDate = System.currentTimeMillis()
        )
        return dao.insertMember(member)
    }

    suspend fun deleteMember(memberId: Long) {
        dao.deleteMemberById(memberId)
        dao.deleteJomaByMemberId(memberId)
    }

    suspend fun addJoma(
        memberId: Long,
        memberName: String,
        amount: Double,
        dateString: String,
        note: String = ""
    ): Long {
        val record = JomaRecordEntity(
            memberId = memberId,
            memberName = memberName,
            amount = amount,
            dateString = dateString,
            timestamp = System.currentTimeMillis(),
            note = note
        )
        return dao.insertJoma(record)
    }

    suspend fun deleteJoma(id: Long) {
        dao.deleteJomaById(id)
    }
}
