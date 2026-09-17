package com.example.skilllinkapp.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.skilllinkapp.ui.theme.SkillLinkBackground
import com.example.skilllinkapp.ui.theme.SkillLinkLightPurple
import com.example.skilllinkapp.ui.theme.SkillLinkNavy
import com.example.skilllinkapp.ui.theme.SkillLinkPurple

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillLinkBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 26.dp,
                top = 28.dp,
                end = 26.dp,
                bottom = 30.dp
            )
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "‹",
                style = MaterialTheme.typography.headlineLarge,
                color = SkillLinkNavy,
                modifier = Modifier
                    .size(35.dp)
                    .clickable {
                        onBack()
                    }
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SkillLinkNavy
                )
            }

            Spacer(
                modifier = Modifier.size(35.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(42.dp)
        )

        // =====================================================
        // ACCOUNT
        // =====================================================

        SettingsSectionTitle(
            text = "ACCOUNT"
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        SettingsItem(
            icon = "○",
            title = "Edit profile",
            subtitle = "Update your personal details",
            onClick = {
                // Future feature
            }
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

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // =====================================================
        // PREFERENCES
        // =====================================================

        SettingsSectionTitle(
            text = "PREFERENCES"
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

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

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // =====================================================
        // APP
        // =====================================================

        SettingsSectionTitle(
            text = "APP"
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        SettingsItem(
            icon = "?",
            title = "Help & support",
            subtitle = "We're here to help",
            onClick = {
                // Future feature
            }
        )

        SettingsDivider()

        SettingsItem(
            icon = "↪",
            title = "Log out",
            subtitle = "",
            titleColor = Color(0xFFFF5A67),
            onClick = {
                onLogout()
            }
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
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF85849A),
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            value = 1.5f,
            type = androidx.compose.ui.unit.TextUnitType.Sp
        )
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
    titleColor: Color = SkillLinkNavy,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -----------------------------------------------------
        // ICON BOX
        // -----------------------------------------------------

        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = SkillLinkLightPurple
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = icon,
                    color = SkillLinkPurple,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(
            modifier = Modifier.size(16.dp)
        )

        // -----------------------------------------------------
        // TEXT
        // -----------------------------------------------------

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )

            if (subtitle.isNotBlank()) {

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF85849A),
                    modifier = Modifier.padding(
                        top = 4.dp
                    )
                )
            }
        }

        // -----------------------------------------------------
        // CHEVRON
        // -----------------------------------------------------

        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF9996A8)
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
            .height(1.dp)
            .padding(
                start = 0.dp
            ),
        color = Color(0xFFE8E6EF)
    ) {}
}