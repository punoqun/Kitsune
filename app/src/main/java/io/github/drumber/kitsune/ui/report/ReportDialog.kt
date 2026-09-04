package io.github.drumber.kitsune.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.report.ReportReason
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReportDialog(
    itemId: String,
    target: ReportTarget,
    onDismiss: () -> Unit
) {
    val viewModel: ReportViewModel = koinViewModel(
        key = "report_${target.name}_$itemId",
        parameters = { parametersOf(itemId, target) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canSubmit by viewModel.canSubmit.collectAsStateWithLifecycle(initialValue = false)
    var submitted by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.submitEvent.collect { event ->
            when (event) {
                ReportViewModel.SubmitEvent.ReportSent -> submitted = true
                ReportViewModel.SubmitEvent.Error -> submitFailed = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(target.titleRes)) },
        text = {
            when {
                submitted -> Text(stringResource(R.string.report_success))
                uiState.state == ReportViewModel.ReportState.Loading ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                uiState.state == ReportViewModel.ReportState.AlreadyReported ->
                    Text(stringResource(R.string.report_already_reported))
                else -> ReportForm(
                    selectedReason = uiState.selectedReason,
                    explanation = uiState.explanation.orEmpty(),
                    submitFailed = submitFailed,
                    onReasonSelected = {
                        submitFailed = false
                        viewModel.selectReason(it)
                    },
                    onExplanationChanged = {
                        submitFailed = false
                        viewModel.setExplanation(it)
                    }
                )
            }
        },
        confirmButton = {
            if (submitted || uiState.state == ReportViewModel.ReportState.AlreadyReported) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_close))
                }
            } else if (uiState.state == ReportViewModel.ReportState.NotReported) {
                TextButton(
                    enabled = canSubmit,
                    onClick = viewModel::submitReport
                ) {
                    Text(stringResource(R.string.report_submit))
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun ReportForm(
    selectedReason: ReportReason?,
    explanation: String,
    submitFailed: Boolean,
    onReasonSelected: (ReportReason) -> Unit,
    onExplanationChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.report_reason))
        ReportReason.entries.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedReason == reason,
                        onClick = { onReasonSelected(reason) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedReason == reason,
                    onClick = null
                )
                Text(stringResource(reason.labelRes))
            }
        }
        OutlinedTextField(
            value = explanation,
            onValueChange = onExplanationChanged,
            label = { Text(stringResource(R.string.report_explanation_hint)) },
            supportingText = {
                if (selectedReason == ReportReason.OTHER && explanation.isBlank()) {
                    Text(stringResource(R.string.report_error_explanation_required))
                } else if (submitFailed) {
                    Text(stringResource(R.string.report_failure))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
    }
}

private val ReportTarget.titleRes: Int
    get() = when (this) {
        ReportTarget.POST -> R.string.report_post_title
        ReportTarget.COMMENT -> R.string.report_comment_title
        ReportTarget.MEDIA_REACTION -> R.string.report_media_reaction_title
    }

private val ReportReason.labelRes: Int
    get() = when (this) {
        ReportReason.NSFW -> R.string.report_reason_nsfw
        ReportReason.OFFENSIVE -> R.string.report_reason_offensive
        ReportReason.SPOILER -> R.string.report_reason_spoiler
        ReportReason.BULLYING -> R.string.report_reason_bullying
        ReportReason.OTHER -> R.string.report_reason_other
        ReportReason.SPAM -> R.string.report_reason_spam
    }
