package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.LocalFileSystem
import org.jdom.Element
import java.io.File
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.icon.PassatIcons
import org.pcsoft.passat.plugin.sdk.ppu.FpcUnitIndex
import javax.swing.Icon

/**
 * Registers a Free Pascal Compiler installation as a first-class IntelliJ SDK, exactly like the JDK
 * for Java. A Pascal module selects an FPC SDK as its target compiler via [com.intellij.openapi.roots.ModuleRootManager],
 * and the target language version (dialect) is carried as [FpcSdkAdditionalData].
 */
@Suppress("OVERRIDE_DEPRECATION")
class FpcSdkType : SdkType("FPC") {

    override fun getPresentableName(): String = PassatBundle.message("sdk.fpc.type.name")

    override fun getIcon(): Icon = PassatIcons.FpcSdk

    override fun getIconForAddAction(): Icon = PassatIcons.FpcSdk

    override fun suggestHomePath(): String? = suggestHomePaths().firstOrNull()

    override fun suggestHomePaths(): Collection<String> {
        val candidates = listOf(
            "/usr/lib/fpc",
            "/usr/local/lib/fpc",
            "/opt/fpc",
            "C:\\FPC",
            "C:\\lazarus\\fpc",
        )
        // The Windows installer nests the actual home one level deeper in a version directory
        // (e.g. C:\FPC\3.2.2), so each base that is not itself a valid home is also expanded into
        // its immediate subdirectories.
        return candidates.flatMap { base -> FpcSdkUtil.expandToValidHomes(base) }.distinct()
    }

    /**
     * A home is only accepted when it carries both a compiler and the `ppudump` tool. `ppudump` is
     * mandatory because Passat reads each unit's real name and interface symbols from its `.ppu` via
     * `ppudump -Fjson`; a minimal FPC install that ships the compiler but not `ppudump` is therefore
     * rejected (see [getInvalidHomeMessage] for the user-facing guidance).
     */
    override fun isValidSdkHome(path: String): Boolean =
        FpcSdkUtil.isValidHome(path) && FpcSdkUtil.hasPpuDump(path)

    override fun getInvalidHomeMessage(path: String): String =
        // A compiler but no ppudump means the user picked a too-minimal FPC distribution: point them
        // at a full install instead of the generic "not an FPC home" message.
        if (FpcSdkUtil.isValidHome(path) && !FpcSdkUtil.hasPpuDump(path))
            PassatBundle.message("sdk.fpc.home.noPpudump")
        else
            PassatBundle.message("sdk.fpc.home.invalid")

    override fun getVersionString(sdkHome: String): String? = FpcSdkUtil.detectVersion(sdkHome)

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        val version = FpcSdkUtil.detectVersion(sdkHome)
        return if (version != null) "FPC $version" else "FPC"
    }

    /**
     * Attaches the FPC installation's compiled-unit directories as class roots and its library
     * sources as source roots. This puts the standard units (RTL + bundled packages) into every
     * Pascal module's resolve scope, so `uses SysUtils` resolves and navigates to source. Called by
     * the platform when an FPC SDK is created.
     */
    override fun setupSdkPaths(sdk: Sdk) {
        val home = sdk.homePath ?: return
        val unitDirs = FpcSdkUtil.findUnitDirectories(home)
        val sourceDirs = FpcSdkUtil.findSourceDirectories(home)
        if (unitDirs.isEmpty() && sourceDirs.isEmpty()) return

        val modificator = sdk.sdkModificator
        addRoots(modificator, unitDirs, OrderRootType.CLASSES)
        addRoots(modificator, sourceDirs, OrderRootType.SOURCES)
        // setupSdkPaths may be invoked off the EDT (ProjectSdksModel), but commitChanges() needs a
        // write action on the EDT — runAndWait switches threads and takes the write lock.
        WriteAction.runAndWait<RuntimeException> { modificator.commitChanges() }

        // Read each compiled unit's real name and interface symbols via ppudump and cache them, so
        // `uses` completion can offer correctly-cased unit names without re-running the tool. This
        // spawns one process per unit, so it runs on a background thread, never the EDT.
        ApplicationManager.getApplication().executeOnPooledThread {
            FpcUnitIndex.getInstance().ensureIndexed(home)
        }
    }

    private fun addRoots(modificator: SdkModificator, dirs: List<File>, rootType: OrderRootType) {
        val lfs = LocalFileSystem.getInstance()
        for (dir in dirs) {
            val vf = lfs.refreshAndFindFileByIoFile(dir) ?: continue
            modificator.addRoot(vf, rootType)
        }
    }

    override fun createAdditionalDataConfigurable(
        sdkModel: SdkModel,
        sdkModificator: SdkModificator,
    ): AdditionalDataConfigurable = FpcSdkAdditionalDataConfigurable(sdkModificator)

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
        (additionalData as? FpcSdkAdditionalData)?.save(additional)
    }

    override fun loadAdditionalData(currentSdk: Sdk, additional: Element): SdkAdditionalData =
        FpcSdkAdditionalData.load(additional)

    companion object {
        @JvmStatic
        fun getInstance(): FpcSdkType = findInstance(FpcSdkType::class.java)

        /** True if the given SDK type id is the FPC SDK. */
        fun isFpcSdk(sdkTypeId: SdkTypeId?): Boolean = sdkTypeId is FpcSdkType
    }
}
