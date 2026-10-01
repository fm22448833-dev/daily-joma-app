package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JomaDao {

    // --- Members ---
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    // --- Joma Records ---
    @Query("SELECT * FROM joma_records ORDER BY timestamp DESC")
    fun getAllJomaRecords(): Flow<List<JomaRecordEntity>>

    @Query("SELECT * FROM joma_records WHERE dateString = :dateStr ORDER BY timestamp DESC")
    fun getJomaRecordsByDate(dateStr: String): Flow<List<JomaRecordEntity>>

    @Query("SELECT * FROM joma_records WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getJomaRecordsByMember(memberId: Long): Flow<List<JomaRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoma(record: JomaRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJomaList(records: List<JomaRecordEntity>)

    @Query("DELETE FROM joma_records WHERE id = :id")
    suspend fun deleteJomaById(id: Long)

    @Query("DELETE FROM joma_records WHERE memberId = :memberId")
    suspend fun deleteJomaByMemberId(memberId: Long)
}
