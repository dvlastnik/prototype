package cz.mendelu.pef.xvlastni.prototype

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import cz.mendelu.pef.xvlastni.prototype.constants.Elements
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeViewModel
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

        val rapidPrototypeViewModel = resolver
            .getSymbolsWithAnnotation(RapidPrototypeViewModel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        val rapidPrototypeFunction = resolver
            .getSymbolsWithAnnotation(RapidPrototypeFunction::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()

        var rPVMData: ClassName? = null
        rapidPrototypeViewModel.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            rPVMData = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

            return@forEach
        }

        var rPF_Select: ClassName? = null
        var rPF_Insert: ClassName? = null
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
                        }
                        RapidPrototypeFunctionType.SELECT -> {
                            rPF_Select = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())
                        }
                        // Handle other cases as necessary
                        RapidPrototypeFunctionType.DELETE -> TODO()
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
            generateFile(
                content = Elements.RapidRow.content,
                packageName = "$packageName.elements",
                fileName = Elements.RapidRow.name
            )
            generateFile(
                content = Elements.RapidListRow.content,
                packageName = "$packageName.elements",
                fileName = Elements.RapidListRow.name
            )

            //TODO: Remove only temporary solution
            val uiStateClass =
                ClassName("cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model", "UiState")
            val defaultErrorsClass = ClassName(
                "cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture",
                "DefaultErrors"
            )
            val errorClass =
                ClassName("cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture", "Error")
            // TODO: ------
            
            val screenContentBuilder = FunSpec.builder(fileName + "Content")
            val screenBuilder = FunSpec.builder(fileName)

            if (isList == true && type == RapidPrototypeType.DATABASE) {

                // making composable function
                screenContentBuilder
                    .addAnnotation(ClassNames.composableClass)
                    .addParameter(
                        Variables.uiState,
                        uiStateClass.parameterizedBy(
                            List::class.asClassName()
                                .parameterizedBy(ClassName(packageName, className)), errorClass
                        )
                    )
                    .addParameter(Variables.paddingValues, ClassNames.paddingValuesClass)
                    .addParameter(
                        Variables.openBottomSheet,
                        ClassNames.mutableStateClass.parameterizedBy(Boolean::class.asClassName())
                    )
                    .addStatement("")
                    .beginControlFlow(
                        """
                    %T(
                        modifier = %T
                            .%T(${Variables.paddingValues})
                            .fillMaxSize()
                    )
                """.trimIndent(),
                        ClassNames.lazyColumnClass,
                        ClassNames.modifierClass,
                        ClassNames.paddingClass
                    )
                    .beginControlFlow("${Variables.uiState}.data?.let { data ->")
                    .beginControlFlow("data.forEachIndexed { index, it ->")
                    .beginControlFlow("item {")
                    .beginControlFlow("if (index != 0) {")
                    .addStatement(
                        """
                    %T(
                        modifier = %T
                            .%T(start = halfMargin(), end = halfMargin())
                            .%T(1.%T)
                    )
                """.trimIndent(),
                        ClassNames.dividerClass,
                        ClassNames.modifierClass,
                        ClassNames.paddingClass,
                        ClassNames.heightClass,
                        ClassNames.dpClass
                    )

                var titleIndex = 0
                properties.forEachIndexed { index, property ->
                    logger.warn("property: $property, $index")
                    if (property.simpleName.asString() == "id") {
                        titleIndex = index
                    }
                }
                val firstPropertyName = properties.elementAt(titleIndex).simpleName.asString()
                val secondPropertyName = properties.elementAt(titleIndex + 1).simpleName.asString()

                screenContentBuilder
                    .addStatement(
                        """
                    RapidListRow(
                        title = it.$firstPropertyName.toString(),
                        subtitle = it.$secondPropertyName.toString()
                    )
                """.trimIndent()
                    )
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow()

                screenBuilder
                    .addAnnotation(ClassNames.composableClass)
                    .addStatement(
                        "val ${Variables.viewModel} = %T<%T>()",
                        ClassNames.hiltViewModelClass,
                        rPVMData!!
                    )
                    .addStatement("")
                    .beginControlFlow(
                        "val ${Variables.uiState}: %T<%T<List<$className>, %T>> = %T ", //{
                        ClassNames.mutableStateClass,
                        uiStateClass,
                        errorClass,
                        ClassNames.rememberSaveableClass
                    )
                    .addStatement(
                        "%T(%T())",
                        ClassNames.mutableStateOfClass,
                        uiStateClass
                    )
                    .endControlFlow() //}
                    .addStatement("")
                    .beginControlFlow("%T(Unit)", ClassNames.launchedEffectClass)
                    .addStatement("${Variables.viewModel}.${rPF_Select!!.simpleName}()")
                    .endControlFlow()
                    .addStatement("")
                    .beginControlFlow("${Variables.viewModel}.${Variables.uiState}.value.let ")
                    .addStatement("${Variables.uiState}.value = it")
                    .endControlFlow() //}
                    .addStatement("")
                    .addStatement(
                        "val ${Variables.openBottomSheet} = %T { %T(false) }",
                        ClassNames.rememberSaveableClass,
                        ClassNames.mutableStateOfClass
                    )
                    .addCode(
                        """
                    ${Elements.BaseScreen.name}(
                        topBarText = %S,
                        drawFullScreenContent = true,
                        showLoading = ${Variables.uiState}.value.loading,
                        floatingActionButton = {
                            %T(onClick = { ${Variables.viewModel}.${rPF_Insert!!.simpleName}() }) {
                                %T(imageVector = Icons.Default.Add, contentDescription = null)
                            }
                        },
                        actions = {},
                        bottomContent = {},
                        showBottomSheet = ${Variables.openBottomSheet},
                        bottomSheetContent = {}
                    )   
                """.trimIndent(),
                        className,
                        ClassNames.floatingActionButtonClass,
                        ClassNames.iconClass,
                    )
                    .beginControlFlow("")
                    .addStatement(
                        "%T(${Variables.uiState} = ${Variables.uiState}.value, ${Variables.paddingValues} = it, ${Variables.openBottomSheet} = ${Variables.openBottomSheet})",
                        ClassName(packageName, fileName + "Content")
                    )
                    .endControlFlow()
            }

            val fileSpec = FileSpec.builder(packageName, fileName)
                .addImport("androidx.compose.foundation.layout.fillMaxSize", "")
                .addImport("$packageName.elements", Elements.RapidRow.name)
                .addImport("$packageName.elements", Elements.BaseScreen.name)
                .addImport("$packageName.elements", Elements.PlaceholderScreen.name)
                .addImport("$packageName.elements", "${Elements.PlaceholderScreen.name}Content")
                .addImport("$packageName.elements", Elements.LoadingScreen.name)
                .addImport("$packageName.elements", Elements.RapidListRow.name)
                .addImport("$packageName.elements", "basicMargin")
                .addImport("$packageName.elements", "halfMargin")
                .addImport("androidx.compose.material.icons.filled", "Add")
                .addImport("androidx.compose.material.icons", "Icons")
                .addFunction(screenBuilder.build())
                .addFunction(screenContentBuilder.build())
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