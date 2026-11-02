package com.tiagohenriques.project.timermasterlab.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.R
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    @StringRes currentScreenTitle: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcons: @Composable RowScope.() -> Unit = {},
) {
    val metrics = LocalUiMetrics.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_playstore_removebg),
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(metrics.logo)
                        .padding(4.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = metrics.title.copy(color = MaterialTheme.colorScheme.surface),
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(id = currentScreenTitle),
                        style = metrics.body.copy(color = MaterialTheme.colorScheme.surfaceContainer),
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
        actions = actionIcons,
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ),
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        scrollBehavior = scrollBehavior,
    )
}

@Preview(showBackground = true)
@Composable
fun TopAppBarPreview() {
    TimerMasterLabTheme {
        TopAppBar(
            currentScreenTitle = R.string.app_name,
            canNavigateBack = true,
            navigateUp = {},
        )
    }
}
