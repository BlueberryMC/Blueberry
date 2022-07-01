package net.blueberrymc.common.scheduler

import net.blueberrymc.common.Blueberry
import net.blueberrymc.common.bml.BlueberryMod
import kotlin.coroutines.*

inline fun AbstractBlueberryScheduler.run(mod: BlueberryMod, crossinline block: () -> Unit) =
    runTask(mod) { block() }

inline fun AbstractBlueberryScheduler.runAsync(mod: BlueberryMod, crossinline block: () -> Unit) =
    runTaskAsynchronously(mod) { block() }

suspend inline fun <R> AbstractBlueberryScheduler.get(mod: BlueberryMod, crossinline block: () -> R): R =
    suspendCoroutine {
        runTask(mod) {
            try {
                it.resume(block())
            } catch (e: Exception) {
                it.resumeWithException(e)
            }
        }
    }

suspend inline fun <R> AbstractBlueberryScheduler.getAsync(mod: BlueberryMod, crossinline block: () -> R): R =
    suspendCoroutine {
        runTaskAsynchronously(mod) {
            try {
                it.resume(block())
            } catch (e: Exception) {
                it.resumeWithException(e)
            }
        }
    }

fun (() -> Unit).runOnClient(mod: BlueberryMod) =
    Blueberry.getUtil().clientScheduler.run(mod, this)

fun (() -> Unit).runOnServer(mod: BlueberryMod) =
    Blueberry.getUtil().serverScheduler.run(mod, this)

fun (() -> Unit).runOnClientAsync(mod: BlueberryMod) =
    Blueberry.getUtil().clientScheduler.runAsync(mod, this)

fun (() -> Unit).runOnServerAsync(mod: BlueberryMod) =
    Blueberry.getUtil().serverScheduler.runAsync(mod, this)

suspend inline fun <R> (() -> R).getOnClient(mod: BlueberryMod): R =
    Blueberry.getUtil().clientScheduler.get(mod, this)

suspend inline fun <R> (() -> R).getOnServer(mod: BlueberryMod): R =
    Blueberry.getUtil().serverScheduler.get(mod, this)

suspend inline fun <R> (() -> R).getOnClientAsync(mod: BlueberryMod): R =
    Blueberry.getUtil().clientScheduler.getAsync(mod, this)

suspend inline fun <R> (() -> R).getOnServerAsync(mod: BlueberryMod): R =
    Blueberry.getUtil().serverScheduler.getAsync(mod, this)
