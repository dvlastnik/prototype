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
import com.squareup.kotlinpoet.Import

class RapidPrototypeProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(RapidPrototype::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            // variables of the data class
            val properties = symbol.getAllProperties()

            // variables for naming
            val packageName = symbol.packageName.asString()
            val className = symbol.simpleName.asString()
            val fileName = "${className}Screen"

            // variables for used jetpack compose classes
            val composableClass = ClassName("androidx.compose.runtime", "Composable")
            val columnClass = ClassName("androidx.compose.foundation.layout", "Column")
            val textClass = ClassName("androidx.compose.material3", "Text")
            val modifierClass = ClassName("androidx.compose.ui", "Modifier")

            // making composable function
            val funSpecBuilder = FunSpec.builder(fileName)
                .addAnnotation(composableClass)
                .addParameter("user", ClassName(packageName, className))
                .beginControlFlow("%T(modifier = %T.fillMaxSize())", columnClass, modifierClass)

            properties.forEach { property ->
                val propertyName = property.simpleName.asString()
                funSpecBuilder
                    .addStatement("%T(text = %S + user.%L)", textClass, "$propertyName: ", propertyName)
            }

            // end of the composable function
            funSpecBuilder.endControlFlow()

            val fileSpec = FileSpec.builder(packageName, fileName)
                .addImport("androidx.compose.foundation.layout.fillMaxSize", "")
                .addFunction(funSpecBuilder.build())
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