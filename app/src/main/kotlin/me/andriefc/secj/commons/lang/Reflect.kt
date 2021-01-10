@file:JvmName("Reflect")

package me.andriefc.secj.commons.lang

import me.andriefc.secj.commons.lang.Modifier.*
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Modifier as JModifier

enum class Modifier(internal val accepts: (modifiers: Int) -> Boolean) {
    STATIC(JModifier::isStatic),
    CONCRETE({ !JModifier.isAbstract(it) }),
    FINAL(JModifier::isFinal),
}

fun Member.typedAs(modifier: Modifier): Boolean = modifier.accepts(modifiers)

fun Class<*>.isKotlin(): Boolean = getAnnotation(Metadata::class.java) != null

fun <T> Class<*>.findField(
    name: String,
    returnClass: Class<T>,
): Field? {
    return try {
        getField(name).takeIf { field -> returnClass.isAssignableFrom(field.type) }
    } catch (_: NoSuchFieldException) {
        null
    }
}

fun <T> Class<T>.asKotlinObject(): T? {
    return when {
        !isKotlin() -> null
        else -> findField("INSTANCE", this)
            ?.takeIf {
                it.typedAs(CONCRETE)
                        && it.typedAs(STATIC)
                        && it.typedAs(FINAL)
            }?.let { cast(it.get(null)) as T }
    }
}
