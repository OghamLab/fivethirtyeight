package com.ola.fivethirtyeight.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ola.fivethirtyeight.include.TabSimpleLightTopAppBar
import com.ola.fivethirtyeight.notification.NotificationFrequency
import com.ola.fivethirtyeight.routes.Routes
import com.ola.fivethirtyeight.viewmodel.SettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -----------------------------
// Dropdown Item
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdownItem(
    title: String,
    description: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(2.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// -----------------------------
// Toggle Item
// -----------------------------
@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// -----------------------------
// Main Settings Screen
// -----------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? Activity
    val state by viewModel.settingsState.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    val isDarkTheme = when (state.themePref) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val statusBarColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
    }

    LaunchedEffect(isDarkTheme, statusBarColor) {
        activity?.window?.let { window ->
            window.statusBarColor = statusBarColor.toArgb()
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    Scaffold(
        topBar = {
            TabSimpleLightTopAppBar(title = "Settings")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 🔔 Notifications Section
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
            }
            SettingsToggleItem(
                title = "Enable Notifications",
                description = "Receive alerts for new articles",
                checked = state.notifications,
                onCheckedChange = viewModel::toggleNotifications

            )

            Divider(Modifier.padding(vertical = 16.dp))

            SettingsDropdownItem(
                title = "Notification Frequency",
                description = "How often you want to receive alerts",
                options = NotificationFrequency.entries.map { it.label },   // ✅ List<String>
                selectedOption = state.notificationFrequency.label,          // ✅ String
                onOptionSelected = { selectedLabel ->
                    NotificationFrequency.entries
                        .firstOrNull { it.label == selectedLabel }
                        ?.let(viewModel::setNotificationFrequency)
                }
            )

            // 🎨 Theme Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Theme", style = MaterialTheme.typography.titleMedium)
            }
            SettingsDropdownItem(
                title = "Theme Preference",
                description = "Choose your theme",
                options = listOf("System Default", "Light", "Dark"),
                selectedOption = state.themePref,
                onOptionSelected = viewModel::setThemePref
            )

            Divider(Modifier.padding(vertical = 16.dp))

            // 📞 Contact Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContactMail, contentDescription = "Contact", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Contact Us", style = MaterialTheme.typography.titleMedium)
            }
            SettingsItemCard(
                title = "Email",
                description = "info.oghamlab@gmail.com",
                icon = Icons.Default.Email
            ) {
                launchEmailIntent(
                    context,
                    "info.oghamlab@gmail.com",
                    "App Support",
                    "Hi, Ogham Lab"
                )
            }

            SettingsItemCard(
                title = "Privacy Policy",
                description = "Read how we handle your data",
                icon = Icons.Default.Lock
            ) {
                navController.navigate(Routes.Main.PRIVACY) // replace with your route
            }

            Divider(Modifier.padding(vertical = 16.dp))

            // ℹ️ About Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("About", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "App Version: v3.0",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            SettingsItemCard(
                title = "Developed By",
                description = "Ogham Lab",
                icon = Icons.Default.Info
            ) {
                showAboutDialog = true
            }
        }
    }

    // 🔲 About Dialog with clickable links
    if (showAboutDialog) {
        val annotatedText = buildAnnotatedString {
            append("All rights to the news content, including information, names, images, pictures, logos, and icons are retained by the original publisher, ")

            // Website link
            pushStringAnnotation(tag = "URL", annotation = "https://abcnews.go.com")
            withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                append("https://abcnews.go.com")
            }
            pop()

            append(".\n\nContact us at ")

            // Email link
            pushStringAnnotation(tag = "EMAIL", annotation = "mailto:info.oghamlab@gmail.com")
            withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                append("info.oghamlab@gmail.com")
            }
            pop()

            append(" for any inquiry.")
        }

        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About This App") },
            text = {
                ClickableText(
                    text = annotatedText,
                    style = LocalTextStyle.current.copy(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                            .firstOrNull()?.let { ann ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                                context.startActivity(intent)
                            }
                        annotatedText.getStringAnnotations(tag = "EMAIL", start = offset, end = offset)
                            .firstOrNull()?.let { ann ->
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse(ann.item) // mailto:
                                }
                                context.startActivity(intent)
                            }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// -----------------------------
// Email intent helper
// -----------------------------
fun launchEmailIntent(
    context: Context,
    to: String,
    subject: String,
    body: String
) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, "Send email using:"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening email app", Toast.LENGTH_SHORT).show()
    }
}
