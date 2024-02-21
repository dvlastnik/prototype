package cz.mendelu.pef.xvlastni.prototype

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import cz.mendelu.pef.xvlastni.prototype.Constants.Elements
import cz.mendelu.pef.xvlastni.prototype.annotations.model.AnnotationModel
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeViewModel

class RapidPrototypeProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val rapidPrototypeSymbol = resolver
            .getSymbolsWithAnnotation(RapidPrototype::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val rapidPrototypeViewModel = resolver
            .getSymbolsWithAnnotation(RapidPrototypeViewModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val rapidPrototypeFunction = resolver
            .getSymbolsWithAnnotation(RapidPrototypeFunction::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()

        val rPVMData = AnnotationModel()
        rapidPrototypeViewModel.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            rPVMData.packageName = symbol.packageName.asString()
            rPVMData.name = symbol.simpleName.asString()

            return@forEach
        }

        val rPFData = AnnotationModel() //TODO zmenit na ClassName
        rapidPrototypeFunction.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            rPFData.packageName = symbol.packageName.asString()
            rPFData.name = symbol.simpleName.asString()

            return@forEach
        }

        rapidPrototypeSymbol.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            // variables of the data class
            val properties = symbol.getAllProperties()

            // variables for naming
            val packageName = symbol.packageName.asString()
            val className = symbol.simpleName.asString()
            val fileName = "${className}Screen"

            // building necessary components
            generateFile(
                content = Elements.Dimensions.content,
                packageName = "$packageName.elements",
                fileName = Elements.Dimensions.name
            )
            generateFile(
                content = Elements.LoadingScreen.content,
                packageName = "$packageName.elements",
                fileName = Elements.LoadingScreen.name
            )
            generateFile(
                content = Elements.PlaceholderScreen.content,
                packageName = "$packageName.elements",
                fileName = Elements.PlaceholderScreen.name
            )
            generateFile(
                content = Elements.BaseScreen.content,
                packageName = "$packageName.elements",
                fileName = Elements.BaseScreen.name
            )

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

            funSpecBuilder
                .addStatement("%T(text = %S + %S)", textClass, "ViewModel:", rPVMData.name)
                .addStatement("%T(text = %S + %S)", textClass, "Function:", rPFData.name)

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

    private fun generateElements(content: String, packageName: String, fileName: String) {
        val contentWithImport = buildString {
            appendLine("import $packageName.elements.${Elements.Dimensions.name}")
            append(content)
        }

        generateFile(contentWithImport, packageName, fileName)
    }

    private fun generateFile(content: String, packageName: String, fileName: String) {
        val contentWithPackage = buildString {
            appendLine("package $packageName")
            appendLine()
            append(content)
        }
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = packageName,
            fileName = fileName
        )

        file.writer().use { writer ->
            writer.write(contentWithPackage)
        }
    }
}