package dev.mslalith.focuslauncher.core.domain.iconpack

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.mslalith.focuslauncher.core.model.IconPackLoadEvent
import dev.mslalith.focuslauncher.core.model.IconPackType
import dev.mslalith.focuslauncher.core.testing.CoroutineTest
import dev.mslalith.focuslauncher.core.testing.launcherapps.TestIconPackManager
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
class LoadIconPackUseCaseTest : CoroutineTest() {

    private val testIconPackManager = TestIconPackManager()

    private lateinit var useCase: LoadIconPackUseCase

    @Before
    fun setup() {
        useCase = LoadIconPackUseCase(iconPackManager = testIconPackManager)
    }

    @Test
    fun `01 - load icon pack`() = runCoroutineTest {
        val iconPackType = IconPackType.System

        backgroundScope.launch {
            testIconPackManager.iconPackLoadEventFlow.test {
                assertThat(awaitItem()).isEqualTo(IconPackLoadEvent.Loading(iconPackType = iconPackType))
                assertThat(awaitItem()).isEqualTo(IconPackLoadEvent.Loaded(iconPackType = iconPackType))
            }
        }

        useCase(iconPackType = iconPackType)
    }
}
