package io.github.taetae98coding.divecamera.feature.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.divecamera.compose.DiveCameraTheme
import io.github.taetae98coding.divecamera.core.model.Housing

@Composable
internal fun HousingScreen(
    navigateToCamera: (Housing) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar() },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = listOf(
                    Housing.SEAFROGS_PH08,
                    Housing.DIVEROID,
                    Housing.NONE,
                ),
            ) { housing ->
                HousingItem(
                    housing = housing,
                    onClick = { navigateToCamera(housing) },
                    modifier = Modifier.fillParentMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(text = "Housing") },
        modifier = modifier,
    )
}

@Composable
private fun HousingItem(
    housing: Housing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Text(
                text = housing.name,
                style = DiveCameraTheme.typography.titleMediumEmphasized,
            )
        }
    }
}
