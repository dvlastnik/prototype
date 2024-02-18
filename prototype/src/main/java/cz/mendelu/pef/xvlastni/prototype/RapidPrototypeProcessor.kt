package cz.mendelu.pef.xvlastni.prototype

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec

class RapidPrototypeProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(RapidPrototype::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            val packageName = symbol.packageName.asString()
            val className = symbol.simpleName.asString()
            val fileName = "${className}Screen"

            val fileSpec = FileSpec.builder(packageName, fileName)
                //.addImport(ClassName("androidx.compose.material3", "Text"), "")
                .addFunction(
                    FunSpec.builder(fileName)
                        .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
                        .addStatement("%T(text = %S)", ClassName("androidx.compose.material3", "Text"), "Generated $className")
                        .build()
                )
                .build()

            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(true, symbol.containingFile!!),
                packageName = packageName,
                fileName = fileName
            )

            file.writer().use { writer ->
                fileSpec.writeTo(writer)
            }
        }

        return emptyList()
    }
}