package com.example.skilllinkapp.screens


import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import com.example.skilllinkapp.ui.theme.SkillLinkLightPurple
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 22.dp,
                vertical = 24.dp
            )
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onBack() },
                shape = RoundedCornerShape(13.dp),
                color = Color.White
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        color = SkillLinkNavy,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(
                modifier = Modifier.size(14.dp)
            )

            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SkillLinkNavy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Manage your SkillLink experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF85849A)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // ACCOUNT CARD
        // =====================================================

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEditProfile() },
            shape = RoundedCornerShape(22.dp),
            color = SkillLinkPurple
        ) {

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = SkillLinkPurple,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.size(16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Your account",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Update your profile information",
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "›",
                    color = Color.White,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // =====================================================
        // ACCOUNT
        // =====================================================

        SettingsSectionTitle(
            text = "ACCOUNT"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsGroup {

            SettingsItem(
                icon = "○",
                title = "Edit profile",
                subtitle = "Update your personal details",
                onClick = onEditProfile
            )

            SettingsDivider()

            SettingsItem(
                icon = "⌁",
                title = "Change password",
                subtitle = "Keep your account secure",
                onClick = {
                    // Future feature
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // PREFERENCES
        // =====================================================

        SettingsSectionTitle(
            text = "PREFERENCES"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsGroup {

            SettingsItem(
                icon = "文",
                title = "Language",
                subtitle = "English",
                onClick = {
                    // Future feature
                }
            )

            SettingsDivider()

            SettingsItem(
                icon = "♧",
                title = "Notifications",
                subtitle = "Manage learning alerts",
                onClick = {
                    // Future feature
                }
            )

            SettingsDivider()

            SettingsItem(
                icon = "◐",
                title = "Dark mode",
                subtitle = "Coming soon",
                onClick = {
                    // Future feature
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // SUPPORT
        // =====================================================

        SettingsSectionTitle(
            text = "SUPPORT"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsGroup {

            SettingsItem(
                icon = "?",
                title = "Help & support",
                subtitle = "Get help with SkillLink",
                onClick = {
                    // Future feature
                }
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =====================================================
        // LOGOUT
        // =====================================================

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onLogout()
                },
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFFFF1F2)
        ) {

            Row(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 17.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFDDE1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↪",
                        color = Color(0xFFE54858),
                        fontSize = 21.sp
                    )
                }

                Spacer(
                    modifier = Modifier.size(14.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Log out",
                        color = Color(0xFFE54858),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "Sign out of your SkillLink account",
                        color = Color(0xFFB66A72),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = "›",
                    color = Color(0xFFE54858),
                    fontSize = 25.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // =====================================================
        // VERSION
        // =====================================================

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "SkillLink",
                color = Color(0xFFAAA7B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Teach. Learn. Connect.",
                color = Color(0xFFC0BDCA),
                fontSize = 11.sp
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )
    }
}


// =============================================================
// SETTINGS GROUP
// =============================================================

@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 6.dp
            ),
            content = content
        )
    }
}


// =============================================================
// SECTION TITLE
// =============================================================

@Composable
private fun SettingsSectionTitle(
    text: String
) {

    Text(
        text = text,
        color = Color(0xFF85849A),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )
}


// =============================================================
// SETTINGS ITEM
// =============================================================

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -----------------------------------------------------
        // ICON
        // -----------------------------------------------------

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SkillLinkLightPurple),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = icon,
                color = SkillLinkPurple,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(
            modifier = Modifier.size(14.dp)
        )

        // -----------------------------------------------------
        // TEXT
        // -----------------------------------------------------

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = SkillLinkNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = subtitle,
                color = Color(0xFF85849A),
                fontSize = 12.sp
            )
        }

        // -----------------------------------------------------
        // CHEVRON
        // -----------------------------------------------------

        Text(
            text = "›",
            color = Color(0xFFAAA7B8),
            fontSize = 23.sp
        )
    }
}


// =============================================================
// DIVIDER
// =============================================================

@Composable
private fun SettingsDivider() {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp),
        color = Color(0xFFF0EEF5)
    ) {}
}