package net.blueberrymc.common.bml.event

import net.blueberrymc.common.Blueberry
import net.blueberrymc.common.bml.BlueberryMod
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

inline fun <reified T : Event> BlueberryMod.event(priority: EventPriority = EventPriority.NORMAL, noinline executor: (T) -> Unit) {
    Blueberry.getEventManager().registerEvent(T::class.java, this, priority, executor)
}

suspend inline fun <R> BlueberryMod.async(crossinline block: () -> R): R =
    suspendCoroutine {
        Blueberry.getUtil().serverScheduler.runTaskAsynchronously(this) {
            try {
                it.resume(block())
            } catch (e: Throwable) {
                it.resumeWithException(e)
            }
        }
    }
