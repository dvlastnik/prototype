package cz.mendelu.pef.xvlastni.prototype

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import cz.mendelu.pef.xvlastni.prototype.constants.Elements
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeRepository
import cz.mendelu.pef.xvlastni.prototype.constants.ClassNames
import cz.mendelu.pef.xvlastni.prototype.constants.Variables
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeType

class RapidPrototypeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {

        val rapidPrototypeSymbol = resolver
            .getSymbolsWithAnnotation(RapidPrototype::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val rapidPrototypeRepository = resolver
            .getSymbolsWithAnnotation(RapidPrototypeRepository::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val rapidPrototypeFunction = resolver
            .getSymbolsWithAnnotation(RapidPrototypeFunction::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()

        var repository: ClassName? = null
        rapidPrototypeRepository.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            repository = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

            return@forEach
        }

        var rPF_Select: ClassName? = null
        var rPF_Insert: ClassName? = null
        var rPF_Delete: ClassName? = null
        var insertParameter: ClassName? = null
        var deleteParameter: ClassName? = null
        var deleteParameterName: String = ""
        rapidPrototypeFunction.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            val annotation = symbol.annotations.firstOrNull {
                it.shortName.asString() == "RapidPrototypeFunction" &&
                        it.annotationType.resolve().declaration.qualifiedName?.asString() == RapidPrototypeFunction::class.qualifiedName
            }
            annotation?.arguments?.forEach { argument ->
                if (argument.name?.asString() == "type") {
                    val typeDeclaration = (argument.value as KSType).declaration

                    val functionTypeName = typeDeclaration.simpleName.asString()
                    val functionType = RapidPrototypeFunctionType.valueOf(functionTypeName)

                    when (functionType) {
                        RapidPrototypeFunctionType.INSERT -> {
                            rPF_Insert = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

                            symbol.parameters.forEach { parameter ->
                                val parameterType = parameter.type.resolve()
                                insertParameter = ClassName(parameterType.declaration.packageName.asString(), parameterType.declaration.simpleName.asString())
                            }
                        }
                        RapidPrototypeFunctionType.SELECT -> {
                            rPF_Select = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())
                        }
                        RapidPrototypeFunctionType.DELETE -> {
                            rPF_Delete = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

                            symbol.parameters.forEach { parameter ->
                                val parameterType = parameter.type.resolve()
                                deleteParameterName = parameter.name!!.asString()
                                deleteParameter = ClassName(parameterType.declaration.packageName.asString(), parameterType.declaration.simpleName.asString())
                            }
                        }
                    }
                }
            }

            return@forEach
        }

        rapidPrototypeSymbol.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            // accessing parameters of the annotation class
            val annotation =
                symbol.annotations.firstOrNull { it.shortName.asString() == "RapidPrototype" }
            val isList =
                annotation?.arguments?.find { it.name?.asString() == "isList" }?.value as? Boolean

            val typeDeclaration = (annotation?.arguments?.find { it.name?.asString() == "type" }?.value as KSType).declaration
            val type = RapidPrototypeType.valueOf(typeDeclaration.simpleName.asString())
            // variables of the data class
            val properties = symbol.getAllProperties()

            // variables for naming
            val packageName = symbol.packageName.asString()
            val className = symbol.simpleName.asString()
            val fileName = "${className}Screen"

            // building necessary components
            generateFile(
                content = Elements.Dimensions.content,
                packageName = "$packageName${Elements.Dimensions.packageName}",
                fileName = Elements.Dimensions.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.LoadingScreen.content,
                packageName = "$packageName${Elements.LoadingScreen.packageName}",
                fileName = Elements.LoadingScreen.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.PlaceholderScreen.content,
                packageName = "$packageName${Elements.PlaceholderScreen.packageName}",
                fileName = Elements.PlaceholderScreen.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.BaseScreen.content,
                packageName = "$packageName${Elements.BaseScreen.packageName}",
                fileName = Elements.BaseScreen.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.RapidRow.content,
                packageName = "$packageName${Elements.RapidRow.packageName}",
                fileName = Elements.RapidRow.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.RapidListRow.content,
                packageName = "$packageName${Elements.RapidListRow.packageName}",
                fileName = Elements.RapidListRow.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.BaseViewModel.content,
                packageName = "$packageName${Elements.BaseViewModel.packageName}",
                fileName = Elements.BaseViewModel.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.Error.content,
                packageName = "$packageName${Elements.Error.packageName}",
                fileName = Elements.Error.name,
                codeGenerator = codeGenerator
            )
            generateFile(
                content = Elements.UiState.content,
                packageName = "$packageName${Elements.UiState.packageName}",
                fileName = Elements.UiState.name,
                codeGenerator = codeGenerator
            )


            val uiStateClass = ClassName(packageName + Elements.UiState.packageName, Elements.UiState.name)
            val errorClass = ClassName("cz.mendelu.pef.xvlastni.prototype.classes", "Error")

            val viewModelBuilder = TypeSpec.classBuilder(className + "ViewModel")
            val screenContentBuilder = FunSpec.builder(fileName + "Content")
            val screenBuilder = FunSpec.builder(fileName)
            val bottomSheetContentBuilder = FunSpec.builder(className + "BottomSheetContent")

            if (isList == true && type == RapidPrototypeType.DATABASE) {
                generateRapidPrototypeDatabase(
                    packageName,
                    className,
                    fileName,
                    uiStateClass,
                    errorClass,
                    rPF_Select,
                    rPF_Insert,
                    rPF_Delete,
                    properties,
                    insertParameter,
                    repository,
                    viewModelBuilder,
                    screenContentBuilder,
                    screenBuilder,
                    bottomSheetContentBuilder,
                    codeGenerator
                )
            }
            else if (isList == true && type == RapidPrototypeType.API) {
                generateRapidPrototypeApiList(
                    packageName,
                    className,
                    fileName,
                    uiStateClass,
                    errorClass,
                    repository,
                    rPF_Select,
                    rPF_Insert,
                    rPF_Delete,
                    properties,
                    insertParameter,
                    deleteParameter,
                    deleteParameterName,
                    viewModelBuilder,
                    screenContentBuilder,
                    screenBuilder,
                    bottomSheetContentBuilder,
                    codeGenerator
                )
            }
            else if (isList == false && type == RapidPrototypeType.API) {
                generateRapidPrototypeApi(
                    packageName,
                    className,
                    fileName,
                    uiStateClass,
                    errorClass,
                    repository,
                    rPF_Select,
                    properties,
                    viewModelBuilder,
                    screenContentBuilder,
                    screenBuilder
                )
            }

            val fileSpec = FileSpec.builder(packageName, fileName)
                .addImport(ClassNames.textClass.packageName, ClassNames.textClass.simpleName)
                .addImport(ClassNames.floatingActionButtonClass.packageName, ClassNames.floatingActionButtonClass.simpleName)
                .addImport(ClassNames.iconButtonClass.packageName, ClassNames.floatingActionButtonClass.simpleName)
                .addImport(ClassNames.iconClass.packageName, ClassNames.iconClass.simpleName)
                .addImport("android.util", "Log")
                .addImport("androidx.compose.foundation.layout", "fillMaxWidth")
                .addImport("androidx.compose.foundation.layout", "Arrangement")
                .addImport("androidx.compose.foundation.layout", "fillMaxSize")
                .addImport("$packageName.elements", Elements.RapidRow.name)
                .addImport("$packageName.elements", Elements.BaseScreen.name)
                .addImport("$packageName.elements", Elements.PlaceholderScreen.name)
                .addImport("$packageName.elements", "${Elements.PlaceholderScreen.name}Content")
                .addImport("$packageName.elements", Elements.LoadingScreen.name)
                .addImport("$packageName.elements", Elements.RapidListRow.name)
                .addImport("$packageName.elements", "basicMargin")
                .addImport("$packageName.elements", "halfMargin")
                .addImport("androidx.compose.material.icons.filled", "Add")
                .addImport("androidx.compose.material.icons.filled", "Refresh")
                .addImport("androidx.compose.material.icons.filled", "Delete")
                .addImport("androidx.compose.material.icons", "Icons")
                .addImport("androidx.compose.material3", "IconButton")
                .addImport("cz.mendelu.pef.xvlastni.prototype.classes", "Error")
                .addFunction(screenBuilder.build())
                .addFunction(screenContentBuilder.build())
                .addFunction(bottomSheetContentBuilder.build())
                .build()

            generateFileWithFileSpec(packageName, fileName, fileSpec, codeGenerator)

            val viewModelFileSpec = FileSpec.builder(packageName, "${className}ViewModel")
                .addImport("android.util", "Log")
                .addImport("cz.mendelu.pef.xvlastni.prototype.classes", "CommunicationResult")
                .addType(viewModelBuilder.build())
                .build()

            generateFileWithFileSpec(packageName, "${className}ViewModel", viewModelFileSpec, codeGenerator)
        }

        return emptyList()
    }
}