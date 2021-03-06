/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.foundation.layout

import androidx.compose.Composable
import androidx.compose.emptyContent
import androidx.test.filters.SmallTest
import androidx.ui.core.ConfigurationAmbient
import androidx.ui.core.Layout
import androidx.compose.ui.unit.LayoutDirection
import androidx.ui.core.Modifier
import androidx.ui.core.Ref
import androidx.ui.core.localeLayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SmallTest
@RunWith(JUnit4::class)
class LayoutDirectionModifierTest : LayoutTest() {

    @Test
    fun testModifiedLayoutDirection_inMeasureScope() {
        val latch = CountDownLatch(1)
        val resultLayoutDirection = Ref<LayoutDirection>()

        show {
            Layout(
                children = @Composable {},
                modifier = Modifier.rtl
            ) { _, _ ->
                resultLayoutDirection.value = layoutDirection
                latch.countDown()
                layout(0, 0) {}
            }
        }

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertTrue(LayoutDirection.Rtl == resultLayoutDirection.value)
    }

    @Test
    fun testModifiedLayoutDirection_inIntrinsicsMeasure() {
        val latch = CountDownLatch(1)
        var resultLayoutDirection: LayoutDirection? = null

        show {
            @OptIn(ExperimentalLayout::class)
            Layout(
                children = @Composable {},
                modifier = Modifier.preferredWidth(IntrinsicSize.Max).rtl,
                minIntrinsicWidthMeasureBlock = { _, _ -> 0 },
                minIntrinsicHeightMeasureBlock = { _, _ -> 0 },
                maxIntrinsicWidthMeasureBlock = { _, _ ->
                    resultLayoutDirection = this.layoutDirection
                    latch.countDown()
                    0
                },
                maxIntrinsicHeightMeasureBlock = { _, _ -> 0 }
            ) { _, _ ->
                layout(0, 0) {}
            }
        }

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertNotNull(resultLayoutDirection)
        assertTrue(LayoutDirection.Rtl == resultLayoutDirection)
    }

    @Test
    fun testRestoreLocaleLayoutDirection() {
        val latch = CountDownLatch(1)
        val resultLayoutDirection = Ref<LayoutDirection>()

        show {
            Stack(Modifier.rtl) {
                val restoreModifier = when (ConfigurationAmbient.current.localeLayoutDirection) {
                    LayoutDirection.Ltr -> Modifier.ltr
                    LayoutDirection.Rtl -> Modifier.rtl
                }
                Layout(emptyContent(), restoreModifier) { _, _ ->
                    resultLayoutDirection.value = layoutDirection
                    latch.countDown()
                    layout(0, 0) {}
                }
            }
        }

        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(LayoutDirection.Ltr, resultLayoutDirection.value)
    }
}
