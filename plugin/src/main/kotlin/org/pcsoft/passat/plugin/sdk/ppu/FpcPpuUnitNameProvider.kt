package org.pcsoft.passat.plugin.sdk.ppu

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.vfs.VirtualFile
import org.pcsoft.passat.language.psi.PascalUnitNameProvider
import org.pcsoft.passat.plugin.sdk.FpcSdkType
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Supplies the real internal name of a compiled FPC unit (`.ppu`) to the `:language` core's
 * [PascalUnitNameProvider] extension point, so `uses` completion shows `System` rather than the
 * lower-cased file name `system`. Names come from [FpcUnitIndex]; on a cache miss the owning FPC
 * home is indexed once in the background (off the EDT, since `ppudump` is spawned per unit) and the
 * correct name appears on the next completion. Non-`.ppu` files defer to the default file-name rule.
 */
class FpcPpuUnitNameProvider : PascalUnitNameProvider {

    // FPC homes already scheduled/finished indexing, to avoid re-spawning the background task.
    private val indexedHomes = ConcurrentHashMap.newKeySet<String>()

    override fun displayName(project: Project, file: VirtualFile): String? {
        if (!file.extension.equals(PPU_EXT, ignoreCase = true)) return null
        val io = File(file.path)
        FpcUnitIndex.getInstance().internalUnitName(io)?.let { return it }
        // Not indexed yet: kick off a one-time background index of the FPC home that owns this file.
        scheduleIndexing(project, io)
        return null
    }

    private fun scheduleIndexing(project: Project, ppu: File) {
        val home = owningFpcHome(ppu) ?: return
        if (!indexedHomes.add(home)) return
        val task = object : Task.Backgroundable(project, "Indexing FPC units", true) {
            override fun run(indicator: ProgressIndicator) {
                FpcUnitIndex.getInstance().ensureIndexed(home)
            }
        }
        ApplicationManager.getApplication().invokeLater {
            ProgressManager.getInstance().run(task)
        }
    }

    /** The registered FPC SDK home that contains [ppu], or `null` if none does. */
    private fun owningFpcHome(ppu: File): String? {
        val path = ppu.canonicalPath
        return ProjectJdkTable.getInstance().allJdks
            .filter { FpcSdkType.isFpcSdk(it.sdkType) }
            .mapNotNull { it.homePath }
            .firstOrNull { home -> runCatching { path.startsWith(File(home).canonicalPath) }.getOrDefault(false) }
    }

    private companion object {
        const val PPU_EXT = "ppu"
    }
}
