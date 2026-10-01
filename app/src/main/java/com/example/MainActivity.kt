package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.JomaViewModel
import com.example.ui.components.AdminHeader
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JomaLogScreen
import com.example.ui.screens.MemberDetailDialog
import com.example.ui.screens.MembersScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DailyJomaApp()
            }
        }
    }
}

enum class AppTab(val title: String) {
    DASHBOARD("ড্যাশবোর্ড"),
    MEMBERS("সদস্য"),
    JOMA("জমা")
}

@Composable
fun DailyJomaApp(
    viewModel: JomaViewModel = viewModel(
        factory = JomaViewModel.provideFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val adminProfile by viewModel.adminProfile.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val jomaRecords by viewModel.jomaRecords.collectAsStateWithLifecycle()
    val selectedMemberForDetail by viewModel.selectedMemberForDetail.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Intercept back button when not on dashboard
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = "ড্যাশবোর্ড")
                    },
                    label = { Text("ড্যাশবোর্ড", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = "সদস্য")
                    },
                    label = { Text("সদস্য", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_members")
                )

                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = {
                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "জমা")
                    },
                    label = { Text("জমা", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BluePrimary,
                        indicatorColor = BluePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_joma")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Section: Admin Header (with avatar, name, subtitle, live date)
            AdminHeader(
                adminProfile = adminProfile,
                todayDateString = dashboardState.todayDateString,
                onUpdateProfile = { name, title, phone ->
                    viewModel.updateAdminProfile(name, title, phone)
                },
                onShareReport = {
                    val reportText = viewModel.generateShareableSummary()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "জমা রিপোর্ট শেয়ার করুন")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.statusBarsPadding()
            )

            // Top Tab Row matching HTML tabs (<div class="tabs">)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BluePrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = BluePrimary
                    )
                }
            ) {
                AppTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("tab_item_${tab.name.lowercase()}")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            // Screen Body with smooth animated transitions
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_content_transition",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> DashboardScreen(
                        state = dashboardState,
                        onQuickDeposit = { member ->
                            viewModel.quickDeposit100(member)
                        },
                        onDepositAllRemaining = { unpaidMembers ->
                            viewModel.depositAllRemaining100(unpaidMembers)
                        },
                        onMemberClick = { member ->
                            viewModel.selectMemberForDetail(member)
                        },
                        onNavigateToJomaTab = {
                            selectedTabIndex = 2
                        }
                    )
                    1 -> MembersScreen(
                        members = members,
                        memberTotals = dashboardState.memberTotals,
                        memberRecordCounts = dashboardState.memberRecordCounts,
                        onAddMember = { name, phone ->
                            viewModel.addMember(name, phone)
                        },
                        onDeleteMember = { member ->
                            viewModel.deleteMember(member)
                        },
                        onMemberClick = { member ->
                            viewModel.selectMemberForDetail(member)
                        }
                    )
                    2 -> JomaLogScreen(
                        members = members,
                        records = jomaRecords,
                        onAddDeposit = { memberId, memberName, amount, dateStr, note ->
                            viewModel.addDeposit(memberId, memberName, amount, dateStr, note)
                        },
                        onDeleteDeposit = { record ->
                            viewModel.deleteDeposit(record)
                        }
                    )
                }
            }
        }
    }

    // Member Detail Dialog
    selectedMemberForDetail?.let { member ->
        MemberDetailDialog(
            member = member,
            records = jomaRecords,
            onDismiss = { viewModel.selectMemberForDetail(null) },
            onQuickDeposit = { m ->
                viewModel.quickDeposit100(m)
            },
            onDeleteMember = { m ->
                viewModel.deleteMember(m)
            }
        )
    }
}
