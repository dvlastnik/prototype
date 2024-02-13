package cz.mendelu.pef.xvlastni.annotationsprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStream

class ComposableGenerateProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerateComposable::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            val packageName = symbol.packageName.asString()
            val className = symbol.simpleName.asString()
            val fileName = "${className}Generated"

            codeGenerator.createNewFile(
                dependencies = Dependencies(true, symbol.containingFile!!),
                packageName = packageName,
                fileName = fileName
            ).use { outputStream ->
                generateComposable(outputStream, packageName, className)
            }
        }

        return emptyList()
    }

    private fun generateComposable(outputStream: OutputStream, packageName: String, className: String) {
        outputStream.writer().use { writer ->
            writer.write(
                """
                package $packageName
                
                import androidx.compose.runtime.Composable
                import androidx.compose.material3.Text
                
                @Composable
                fun ${className}Composable() {
                    Text("Hello from $className!")
                }
                """.trimIndent()
            )
        }
    }
}