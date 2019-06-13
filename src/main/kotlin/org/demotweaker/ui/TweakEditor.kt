package org.demotweaker.ui

import org.demotweaker.Tweaker
import tornadofx.App
import tornadofx.launch

class DemoTweakApp: App(TweakFrame::class)

/**
 * Manages the UI for editing tweaks.
 */
object TweakAppLauncher {

    var uiFrame: TweakFrame? = null

    private var launchCalled = false
    var tweakerToShow: Tweaker? = null
        private set

    fun showTweaker(tweaker: Tweaker) {
        tweakerToShow = tweaker
        openUI()
        uiFrame?.tweaker = tweaker
    }

    // TODO: Might be nice if this worked with another app as a separate scene / window?
    fun openUI() {
        if (!launchCalled) {
            launchCalled = true

            // Do not block this method
            val thread = Thread {
                launch<DemoTweakApp>()
            }
            thread.isDaemon = false // Wait until this thread is done before quitting program
            thread.start()
        }
    }



}