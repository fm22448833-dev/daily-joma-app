package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JomaRecordEntity
import com.example.data.local.MemberEntity
import com.example.ui.components.BengaliNumberUtils
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.GreenDeposit
import com.example.ui.theme.RedDanger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun JomaLogScreen(
    members: List<MemberEntity>,
    records: List<JomaRecordEntity>,
    onAddDeposit: (memberId: Long, memberName: String, amount: Double, dateStr: String, note: String) -> Unit,
    onDeleteDeposit: (JomaRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayDateStr = remember { BengaliNumberUtils.getTodayDateString() }

    var selectedMember by remember(members) { mutableStateOf(members.firstOrNull()) }
    var selectedAmount by remember { mutableStateOf("100") }
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var noteInput by remember { mutableStateOf("") }
    var isMemberDropdownOpen by remember { mutableStateOf(false) }

    // History filter
    var filterMemberId by remember { mutableStateOf<Long?>(null) }
    var recordToDelete by remember { mutableStateOf<JomaRecordEntity?>(null) }

    val quickAmounts = listOf("100", "200", "500", "50")

    val filteredRecords = remember(records, filterMemberId) {
        if (filterMemberId == null) {
            records
        } else {
            records.filter { it.memberId == filterMemberId }
        }
    }

    val filteredTotal = remember(filteredRecords) {
        filteredRecords.sumOf { it.amount }
    }

    // DatePicker setup
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDateStr = sdf.format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("joma_log_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: দৈনিক ১০০ টাকা জমা (Matching HTML card)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(GreenDeposit.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GreenDeposit,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "দৈনিক ১০০ টাকা জমা",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সদস্য নির্বাচন করে জমার পরিমাণ নিশ্চিত করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Member Dropdown Selection (Matching <select id="jomaMember">)
                    Text(
                        text = "সদস্য নির্বাচন করুন *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { isMemberDropdownOpen = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_joma_member_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedMember?.name ?: "কোনো সদস্য নেই",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedMember != null) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "তালিকা দেখুন"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isMemberDropdownOpen,
                            onDismissRequest = { isMemberDropdownOpen = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(m.name, fontWeight = FontWeight.Medium)
                                            if (m.phone.isNotBlank()) {
                                                Text(
                                                    m.phone,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedMember = m
                                        isMemberDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Amount Selection Chips
                    Text(
                        text = "টাকার পরিমাণ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            val isSelected = selectedAmount == amt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAmount = amt },
                                label = {
                                    Text(
                                        text = "${BengaliNumberUtils.toBangla(amt.toInt())} ৳",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Date Selection & Note Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedCard(
                            onClick = { datePickerDialog.show() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "তারিখ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = BengaliNumberUtils.formatDateToDisplay(selectedDateStr),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("মন্তব্য (ঐচ্ছিক)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Submit Button: ১০০ টাকা জমা দিন (Matches HTML .btn)
                    Button(
                        onClick = {
                            val member = selectedMember
                            val amount = selectedAmount.toDoubleOrNull() ?: 100.0
                            if (member != null && amount > 0) {
                                onAddDeposit(
                                    member.id,
                                    member.name,
                                    amount,
                                    selectedDateStr,
                                    noteInput
                                )
                                noteInput = ""
                            }
                        },
                        enabled = selectedMember != null,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_joma_deposit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${BengaliNumberUtils.toBangla(selectedAmount.toIntOrNull() ?: 100)} টাকা জমা দিন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Instruction line from HTML: প্রতিদিন একবার করে চাপ দিলে ১০০ টাকা যোগ হবে
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 প্রতিদিন একবার করে চাপ দিলে ১০০ টাকা যোগ হবে",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Section: জমার বিবরণী তালিকা (Joma History Log)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "জমার পূর্ববর্তী রেকর্ড",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GreenDeposit.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "মোট: " + BengaliNumberUtils.formatTaka(filteredTotal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDeposit,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Member filter horizontal chip bar
                if (members.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = filterMemberId == null,
                                onClick = { filterMemberId = null },
                                label = { Text("সকল সদস্য") }
                            )
                        }
                        items(members, key = { it.id }) { m ->
                            FilterChip(
                                selected = filterMemberId == m.id,
                                onClick = {
                                    filterMemberId = if (filterMemberId == m.id) null else m.id
                                },
                                label = { Text(m.name) }
                            )
                        }
                    }
                }
            }
        }

        // History Table / Items (Matches HTML table: নাম, তারিখ, টাকা, ডিলিট)
        if (filteredRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "এখনও কোনো জমার হিসাব নেই",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("joma_record_item_${record.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Member Name + Date + Note
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GreenDeposit.copy(alpha = 0.12f))
                            ) {
                                Text(
                                    text = record.memberName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GreenDeposit
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = record.memberName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )

                                Text(
                                    text = BengaliNumberUtils.formatDateToDisplay(record.dateString),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (record.note.isNotBlank()) {
                                    Text(
                                        text = record.note,
                                        fontSize = 11.sp,
                                        color = BluePrimary
                                    )
                                }
                            }
                        }

                        // Right: Amount Pill + Delete Button (.btn-red as in HTML)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GreenDeposit.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "+ " + BengaliNumberUtils.formatTaka(record.amount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDeposit,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete button (Red button as in HTML)
                            IconButton(
                                onClick = { recordToDelete = record },
                                modifier = Modifier
                                    .testTag("delete_joma_btn_${record.id}")
                                    .size(34.dp)
                                    .background(
                                        RedDanger.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "ডিলিট",
                                    tint = RedDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete Record Confirmation Dialog
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("জমা হিসাব ডিলিট") },
            text = {
                Text("${record.memberName} এর ${BengaliNumberUtils.formatTaka(record.amount)} জমার রেকর্ড কি মুছে ফেলতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDeposit(record)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                    modifier = Modifier.testTag("confirm_delete_joma_btn")
                ) {
                    Text("ডিলিট")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
