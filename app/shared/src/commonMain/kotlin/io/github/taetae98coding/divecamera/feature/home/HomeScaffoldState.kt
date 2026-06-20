package io.github.taetae98coding.divecamera.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.divecamera.core.model.CameraGesture
import divecamera.app.shared.generated.resources.Res
import divecamera.app.shared.generated.resources.home_housing_none_name
import divecamera.app.shared.generated.resources.home_housing_seafrogs_manufacturer
import divecamera.app.shared.generated.resources.home_housing_seafrogs_name
import divecamera.app.shared.generated.resources.sf_ph_08

@Composable
internal fun rememberHomeScaffoldState(): HomeScaffoldState = remember { HomeScaffoldState() }

@Stable
internal class HomeScaffoldState {
    val housings: List<DiveHousing> =
        listOf(
            DiveHousing(
                id = "seafrogs-sf-ph-08",
                nameRes = Res.string.home_housing_seafrogs_name,
                manufacturerRes = Res.string.home_housing_seafrogs_manufacturer,
                image = Res.drawable.sf_ph_08,
                gesture =
                    CameraGesture(
                        isTouchEnable = false,
                        isUpKeyEnable = true,
                        isSwipeEnable = true,
                    ),
            ),
            DiveHousing(
                id = "no-housing",
                nameRes = Res.string.home_housing_none_name,
                manufacturerRes = null,
                image = null,
                gesture =
                    CameraGesture(
                        isTouchEnable = true,
                        isUpKeyEnable = true,
                        isSwipeEnable = false,
                    ),
            ),
        )
}
