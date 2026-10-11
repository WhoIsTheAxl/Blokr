/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loader.mod.manifest

import de.bixilon.minosoft.modding.loader.mod.manifest.load.LoadM
import de.bixilon.minosoft.modding.loader.mod.manifest.packages.PackagesM
import java.net.URL

data class ModManifest(
    val name: String,
    val description: String? = null,
    val authors: List<String>? = null,
    val website: URL? = null,
    val version: String,

    val main: String,
    val packages: PackagesM? = null,
    val load: LoadM? = null,
) {

    init {
        if (!NAME_REGEX.matches(name)) {
            throw IllegalStateException("Invalid mod name!")
        }
    }

    companion object {
        val NAME_REGEX = "[\\w| -_]{3,30}".toRegex()
    }
}
