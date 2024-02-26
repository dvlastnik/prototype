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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import cz.mendelu.pef.xvlastni.prototype.constants.Elements
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

        var rPVMData: ClassName? = null
        rapidPrototypeViewModel.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            rPVMData = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

            return@forEach
        }

        var rPFData: ClassName? = null //TODO zmenit na ClassName
        rapidPrototypeFunction.forEach { symbol ->
            if (!symbol.validate()) return@forEach

            rPFData = ClassName(symbol.packageName.asString(), symbol.simpleName.asString())

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
            generateFile(
                content = Elements.RapidRow.content,
                packageName = "$packageName.elements",
                fileName = Elements.RapidRow.name
            )

            // variables for used jetpack compose classes
            val composableClass = ClassName("androidx.compose.runtime", "Composable")
            val columnClass = ClassName("androidx.compose.foundation.layout", "Column")
            val textClass = ClassName("androidx.compose.material3", "Text")
            val modifierClass = ClassName("androidx.compose.ui", "Modifier")
            val hiltViewModelClass = ClassName("androidx.hilt.navigation.compose", "hiltViewModel")
            val stringResourceClass = ClassName("androidx.compose.ui.res", "stringResource")

            //TODO: Remove only temporary solution
            val uiStateClass = ClassName("cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model", "UiState")
            val defaultErrorsClass = ClassName("cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture", "DefaultErrors")
            // TODO: ------

            // variables
            val viewModelVar = "viewModel"
            val uiStateVar = "uiState"

            val mutableStateClass = ClassName("androidx.compose.runtime", "MutableState")
            val rememberSaveableClass = ClassName("androidx.compose.runtime.saveable", "rememberSaveable")
            val mutableStateOfClass = ClassName("androidx.compose.runtime", "mutableStateOf")
            val launchedEffectClass = ClassName("androidx.compose.runtime", "LaunchedEffect")
            val alignmentClass = ClassName("androidx.compose.ui", "Alignment")
            val arrangementClass = ClassName("androidx.compose.foundation.layout", "Arrangement")
            val paddingClass = ClassName("androidx.compose.foundation.layout", "padding")
            val floatingActionButtonClass = ClassName("androidx.compose.material3", "FloatingActionButton")
            val iconClass = ClassName("androidx.compose.material3", "Icon")

            // making composable function
            val screenContentBuilder = FunSpec.builder(fileName + "Content")
                .addAnnotation(composableClass)
                .addParameter(uiStateVar, uiStateClass.parameterizedBy(ClassName(packageName, className), defaultErrorsClass))
                .addStatement("")
                .beginControlFlow("if ($uiStateVar.data == null) ")
                .addStatement("%T(text = %S)", textClass, "nothing in there")
                .endControlFlow()
                .beginControlFlow("else ")
                .beginControlFlow("""
                    %T(
                        modifier = %T
                            .fillMaxSize()
                            .%T(all = basicMargin()),
                        horizontalAlignment = %T.CenterHorizontally,
                        verticalArrangement = %T.Center
                    )
                """.trimIndent(), columnClass, modifierClass, paddingClass, alignmentClass, arrangementClass)

            properties.forEach { property ->
                val propertyName = property.simpleName.asString()
                screenContentBuilder
                    .addStatement("%T(trailing = %S, leading = $uiStateVar.data!!.%L.toString())", ClassName("$packageName.elements", "RapidRow"), propertyName, propertyName)
            }

            screenContentBuilder
                .endControlFlow()
                .endControlFlow()

            val screenBuilder = FunSpec.builder(fileName)
                .addAnnotation(composableClass)
                .addStatement("val $viewModelVar = %T<%T>()", hiltViewModelClass, rPVMData!!)
                .addStatement("")
                .beginControlFlow("$viewModelVar.let ")
                .beginControlFlow("%T(it) ", launchedEffectClass)
                .addStatement("$viewModelVar.${rPFData!!.simpleName}()")
                .endControlFlow()
                .endControlFlow()
                .addStatement("")
                .beginControlFlow("val $uiStateVar: %T<%T<$className, %T>> = %T ", //{
                    mutableStateClass,
                    uiStateClass,
                    defaultErrorsClass,
                    rememberSaveableClass
                )
                .addStatement("%T(%T())",
                    mutableStateOfClass,
                    uiStateClass
                )
                .endControlFlow() //}
                .addStatement("")
                .beginControlFlow("$viewModelVar.$uiStateVar.value.let ")
                .addStatement("$uiStateVar.value = it")
                .endControlFlow() //}
                .addStatement("")
                .addCode("""
                    ${Elements.BaseScreen.name}(
                        topBarText = %S,
                        drawFullScreenContent = false,
                        showLoading = $uiStateVar.value.loading,
                        placeholderScreenContent = if ($uiStateVar.value.errors != null) {
                            ${Elements.PlaceholderScreen.name}Content(null, %T(id = $uiStateVar.value.errors!!.communicationError))
                        }
                        else {
                            null
                        },
                        floatingActionButton = {
                            %T(onClick = { $viewModelVar.${rPFData!!.simpleName}() }) {
                                %T(imageVector = Icons.Default.Refresh, contentDescription = null)
                            }
                        },
                        actions = {},
                        bottomContent = {},
                        showBottomSheet = %T(false),
                        bottomSheetContent = {}
                    )   
                """.trimIndent(),
                    className,
                    stringResourceClass,
                    floatingActionButtonClass,
                    iconClass,
                    mutableStateOfClass
                )
                .beginControlFlow("")
                .addStatement("%T($uiStateVar = $uiStateVar.value)", ClassName(packageName, fileName + "Content"))
                .endControlFlow()

            val fileSpec = FileSpec.builder(packageName, fileName)
                .addImport("androidx.compose.foundation.layout.fillMaxSize", "")
                .addImport("$packageName.elements", Elements.RapidRow.name)
                .addImport("$packageName.elements", Elements.BaseScreen.name)
                .addImport("$packageName.elements", Elements.PlaceholderScreen.name)
                .addImport("$packageName.elements", "${Elements.PlaceholderScreen.name}Content")
                .addImport("$packageName.elements", Elements.LoadingScreen.name)
                .addImport("$packageName.elements", "basicMargin")
                .addImport("$packageName.elements", "halfMargin")
                .addImport("androidx.compose.material.icons.filled", "Refresh")
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