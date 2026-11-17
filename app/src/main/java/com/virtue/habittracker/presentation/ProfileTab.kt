package com.virtue.habittracker.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.virtue.habittracker.presentation.account.AccountDeletionUiState

@Composable
fun ProfileTab(
    modifier: Modifier,
    visible: Boolean,
    onSignOut: () -> Unit,
    deletionState: AccountDeletionUiState,
    onDeleteAccountClick: () -> Unit,
    onDeleteConfirmationChanged: (String) -> Unit,
    onDeleteAccountConfirmed: () -> Unit,
    onDeleteAccountCancelled: () -> Unit
) {
    Column(
        modifier.offset(x = if (visible) 0.dp else 10_000.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Manage your account and personal data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your progress belongs to you.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Your habits and programs are stored locally and synced to your account when cloud sync is available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Delete account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Permanently remove your Firebase account and its cloud data, including habits, check-ins, and program progress. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onDeleteAccountClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !deletionState.isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete account")
                }
            }
        }
    }

    if (deletionState.showConfirmation) {
        AlertDialog(
            onDismissRequest = onDeleteAccountCancelled,
            title = { Text("Permanently delete your account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This permanently deletes your Firebase Authentication account and all data stored under your user record, including habits, daily check-ins, programs, and program activity. Your local app data and pending sync queue will also be cleared. A minimal temporary deletion marker is retained to prevent an old session from recreating your data."
                    )
                    Text(
                        "You will not be able to recover this information. For security, you may need to sign in again before deletion.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = deletionState.confirmationText,
                        onValueChange = onDeleteConfirmationChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Type DELETE to confirm") },
                        enabled = !deletionState.isDeleting
                    )
                    deletionState.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (deletionState.isDeleting) {
                        CircularProgressIndicator()
                        Text("Deleting your account and cloud data…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDeleteAccountConfirmed,
                    enabled = !deletionState.isDeleting && deletionState.confirmationText == "DELETE",
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (deletionState.isDeleting) "Deleting…" else "Delete permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteAccountCancelled, enabled = !deletionState.isDeleting) {
                    Text("Cancel")
                }
            }
        )
    }
}
