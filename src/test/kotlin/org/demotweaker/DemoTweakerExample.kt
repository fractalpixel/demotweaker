package org.demotweaker


fun main() {
    val tweaker = Tweaker()
//    val size = tweaker.getVariable("size")
 //   val hue = tweaker.getVariable("hue")
/*
    size.set(0, 1.0)
    size.set(5, 2.0)
    size.set(10, 10.0)
    size.set(20, 100.0)

    hue.set(2, 0.2)
    hue.set(10, 0.3)
    hue.set(15, 0.5)
*/
    tweaker.openEditor()

    while(true)  {
        tweaker.update()
        Thread.sleep(30)

    }

}