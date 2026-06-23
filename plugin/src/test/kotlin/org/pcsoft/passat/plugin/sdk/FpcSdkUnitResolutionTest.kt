package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.pcsoft.passat.language.psi.PascalUnits
import java.io.File

/**
 * End-to-end check of the FPC SDK wiring that powers `uses` completion: a real FPC home (the
 * fixture under `testData/fpc-home` ships actual `.ppu` files copied from an FPC 3.2.2 install) is
 * registered as an [FpcSdkType] SDK, its unit directories are attached as class roots via
 * [FpcSdkType.setupSdkPaths], and the SDK is bound to the test module. The standard units must then
 * surface through [PascalUnits], exactly as the completion contributor consumes them.
 */
class FpcSdkUnitResolutionTest : BasePlatformTestCase() {

    private lateinit var sdk: Sdk

    private fun fixtureHome(): File {
        val url = javaClass.classLoader.getResource("testData/fpc-home")
            ?: error("fpc-home fixture not found on test classpath")
        return File(url.toURI())
    }

    override fun setUp() {
        super.setUp()
        val home = fixtureHome().absolutePath
        // Sanity: the fixture must look like a real FPC home to the pure detection logic.
        assertTrue("fixture should be a valid FPC home", FpcSdkUtil.isValidHome(home))

        sdk = ProjectJdkImpl("Test FPC", FpcSdkType.getInstance(), home, null)
        FpcSdkType.getInstance().setupSdkPaths(sdk)
        WriteAction.runAndWait<RuntimeException> {
            ProjectJdkTable.getInstance().addJdk(sdk, testRootDisposable)
        }
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
    }

    fun testStandardUnitsResolveThroughSdkRoots() {
        myFixture.configureByText("Main.pas", "program P;\nuses ;\nbegin\nend.\n")
        val file = myFixture.file
        val names = PascalUnits.availableUnitNames(file.project, file.resolveScope)

        assertContainsElements(names, "cp437", "cp646", "dbf_wtil")
    }

    fun testUsesCompletionOffersStandardUnits() {
        myFixture.configureByText("Main.pas", "program P;\nuses <caret>;\nbegin\nend.\n")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "cp437", "cp646", "dbf_wtil")
    }
}
