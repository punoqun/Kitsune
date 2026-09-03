package io.github.drumber.kitsune.ui.profile.editprofile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.ui.component.compose.loading.ListLoadingSkeleton
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId

/**
 * Compose body for [SelectProfileLinkSiteBottomSheet].
 *
 * Displays a scrollable list of available profile link sites. Shows skeleton rows while the
 * list is being fetched from the network.
 */
@Composable
fun SelectProfileLinkSiteScreen(
    profileLinkSites: List<ProfileLinkSite>,
    isLoading: Boolean,
    onSiteSelected: (ProfileLinkSite) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Text(
                text = stringResource(R.string.action_select_profile_link_site),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(10.dp)
            )
        }

        if (isLoading) {
            item {
                ListLoadingSkeleton(
                    modifier = Modifier.fillMaxWidth(),
                    itemCount = 4
                )
            }
        }

        items(profileLinkSites, key = { it.id }) { site ->
            if (!site.name.isNullOrBlank()) {
                ListItem(
                    headlineContent = { Text(site.name) },
                    leadingContent = {
                        Icon(
                            painter = painterResource(getProfileSiteLogoResourceId(site.name)),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.clickable { onSiteSelected(site) }
                )
            }
        }
    }
}
