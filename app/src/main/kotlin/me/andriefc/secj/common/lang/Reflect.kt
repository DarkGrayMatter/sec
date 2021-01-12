@file:JvmName("Reflect")

package me.andriefc.secj.common.lang

import java.lang.reflect.Modifier.*


fun Class<*>.isKotlin(): Boolean = getAnnotation(Metadata::class.java) != null

fun <T> Class<T>.tryAsKotlinSingleton(): T? {
    return when {
        !isKotlin() -> null
        else -> try {
            val field = getField("INSTANCE").takeIf {
                isAssignableFrom(it.type)
                        && isStatic(it.modifiers)
                        && isPublic(it.modifiers)
                        && !isAbstract(it.modifiers)
            }
            field?.get(null)?.let(this::cast)
        } catch (_: NoSuchFieldException) {
            null
        }
    }
}
