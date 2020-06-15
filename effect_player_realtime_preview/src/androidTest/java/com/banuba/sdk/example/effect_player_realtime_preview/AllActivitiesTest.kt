package com.banuba.sdk.example.effect_player_realtime_preview

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.banuba.sdk.example.effect_player_realtime_preview.screenshot.EspressoScreenshot
import com.banuba.sdk.example.effect_player_realtime_preview.screenshot.ScreenshotTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class AllActivitiesTest {
    @Rule
    @JvmField
    val testName = TestName()

    @Rule
    @JvmField
    val screenshotTestRule = ScreenshotTestRule()

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
        )

    @Test
    fun cameraPreviewTest() {
        onView(withId(R.id.openCameraButton)).perform(click())
        Thread.sleep(2000)

//        Negative case to make test fail
//        onView(withId(R.id.openCameraButton)).check(matches(withText("Hello!")))
        pressBack()
    }

    @Test
    fun applyMaskTest() {
        onView(withId(R.id.applyMaskButton)).perform(click())
        onView(withId(R.id.showMaskButton)).check(matches(isDisplayed())).perform(click())
//        Wait until effect appeared to make a screenshot
        Thread.sleep(2000)
        EspressoScreenshot.takeScreenshot(testName.methodName)

//        Enable all 3 effects by tapping on the screen

        onView(withId(R.id.surfaceView)).perform(click())
        Thread.sleep(2000)
        EspressoScreenshot.takeScreenshot(testName.methodName)

        onView(withId(R.id.surfaceView)).perform(click())
        Thread.sleep(2000)
        EspressoScreenshot.takeScreenshot(testName.methodName)

        onView(withId(R.id.surfaceView)).perform(click())
        Thread.sleep(2000)
        EspressoScreenshot.takeScreenshot(testName.methodName)

        onView(withId(R.id.showMaskButton)).perform(click())

        pressBack()
    }

    @Test
    fun RecordVideoTest() {
        onView(withId(R.id.recordVideoButton)).perform(click())
        onView(withId(R.id.recordAudioSwitch)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(500)
        onView(withId(R.id.recordActionButton)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.recordActionButton)).perform(click())

        pressBack()
    }
}
