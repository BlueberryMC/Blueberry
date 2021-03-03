package net.blueberrymc.common.bml.event

import net.blueberrymc.common.Blueberry
import net.blueberrymc.common.bml.BlueberryMod

inline fun <reified T : Event> BlueberryMod.event(priority: EventPriority = EventPriority.NORMAL, noinline executor: (T) -> Unit) {
    Blueberry.getEventManager().registerEvent(T::class.java, this, priority, executor)
}
