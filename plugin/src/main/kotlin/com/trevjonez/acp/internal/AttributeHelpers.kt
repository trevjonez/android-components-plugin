/*
 *    Copyright 2018 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trevjonez.acp.internal

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer


@Suppress("UNCHECKED_CAST")
internal fun AttributeContainer.addAll(
    attributes: AttributeContainer
): AttributeContainer = apply {
  attributes.keySet().forEach {
    it as Attribute<Any>
    attribute(it, attributes.getAttribute(it)!!)
  }
}

internal inline infix fun <reified T> Attribute<T>.from(container: AttributeContainer): T =
    T::class.java.cast(container.getAttribute(this))
