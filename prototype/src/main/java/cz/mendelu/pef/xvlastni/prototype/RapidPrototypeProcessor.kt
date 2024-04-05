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
            val errorClass = ClassName(packageName + Elements.Error.packageName, Elements.Error.name)

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
                //data class is not needed

                //UISTATE
                val uiStateTypeName = uiStateClass
                    .parameterizedBy(
                        ClassName(packageName, className), errorClass
                    )

                val mutableStateTypeName = ClassNames.mutableStateClass
                    .parameterizedBy(uiStateTypeName)

                val uiStateProperty = PropertySpec.builder(Variables.uiState, mutableStateTypeName)
                    .initializer("%T(UiState())", ClassNames.mutableStateOfClass)
                    .build()
                //UISTATE

                requireNotNull(repository) { "Repository is not annotated" }
                requireNotNull(rPF_Select) { "Get call is not annotated" }

                //SELECT - GET
                var selectFun: FunSpec? = null
                rPF_Select?.let {
                    selectFun = FunSpec.builder(it.simpleName)
                        .addCode("""
                            %T {
                                val result = %T(%T.IO) {
                                    ${Variables.repository}.${it.simpleName}()
                                }
                                
                                when(result) {
                                    is CommunicationResult.CommunicationError ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(code = 0 ,message = %S))
                                    is CommunicationResult.Error ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 1, message = %S))
                                    is CommunicationResult.Exception ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 2, message = %S))
                                    is CommunicationResult.Success ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = result.data, errors = null)
                                }
                            }
                        """.trimIndent(),
                            ClassNames.launchClass,
                            ClassNames.withContextClass,
                            ClassNames.dispatchersClass,
                            ClassName(packageName + Elements.Error.packageName, Elements.Error.name),
                            "No internet",
                            "Failed to load the list",
                            "Unknown error"
                        )
                        .build()
                }
                //SELECT - GET

                viewModelBuilder
                    .addAnnotation(ClassNames.daggerHiltViewModelClass)
                    .primaryConstructor(FunSpec.constructorBuilder()
                        .addAnnotation(ClassNames.injectClass)
                        .addParameter(Variables.repository, repository!!)
                        .build())
                    .superclass(ClassName(packageName + Elements.BaseViewModel.packageName, Elements.BaseViewModel.name))
                    .addProperty(PropertySpec.builder(Variables.repository, repository!!, KModifier.PRIVATE).initializer(Variables.repository).build())
                    .addProperty(uiStateProperty)

                screenContentBuilder
                    .addAnnotation(ClassNames.composableClass)
                    .addParameter(
                        Variables.uiState,
                        uiStateTypeName
                    )
                    .addParameter(Variables.paddingValues, ClassNames.paddingValuesClass)
                    .beginControlFlow("""
                        %T(
                            modifier = %T
                                .fillMaxSize()
                                .%T(all = basicMargin()),
                            horizontalAlignment = %T.CenterHorizontally,
                            verticalArrangement = %T.Center
                        )
                    """.trimIndent(),
                        ClassNames.columnClass,
                        ClassNames.modifierClass,
                        ClassNames.paddingClass,
                        ClassNames.alignmentClass,
                        ClassNames.arrangementClass
                    )
                    .beginControlFlow("if (${Variables.uiState}.value.data == null)")
                    .addStatement("%T(text = %S)", ClassNames.textClass, "nothing in there")
                    .endControlFlow()
                    .beginControlFlow("else")

                properties.forEach { property ->
                    val propertyName = property.simpleName.asString()
                    screenContentBuilder
                        .addStatement("%T(trailing = %S, leading = ${Variables.uiState}.data!!.%L.toString()", ClassName(Elements.RapidRow.packageName, Elements.RapidRow.name), propertyName, propertyName)
                }

                screenContentBuilder
                    .endControlFlow()
                    .endControlFlow()

                screenBuilder
                    .addAnnotation(ClassNames.composableClass)
                    .addStatement("val ${Variables.viewModel} = %T<${className}ViewModel>()", ClassNames.hiltViewModelClass)
                    .addStatement("")
                    .beginControlFlow(
                        "val ${Variables.uiState}: %T<%T<$className, %T>> = %T",
                        ClassNames.mutableStateClass,
                        uiStateClass,
                        errorClass,
                        ClassNames.rememberSaveableClass
                    )
                    .addStatement("%T(%T())", ClassNames.mutableStateOfClass, uiStateClass)
                    .endControlFlow()
                    .addStatement("")
                    .beginControlFlow("${Variables.viewModel}.${Variables.uiState}.value.let")
                    .addStatement("${Variables.uiState}.value = it")
                    .endControlFlow()
                    .addStatement("")
                    .addCode("""
                        ${Elements.BaseScreen.name}(
                            topBarText = %S,
                            drawFullScreenContent = false,
                            showLoading = ${Variables.uiState}.value.loading,
                            placeholderScreenContent = if (${Variables.uiState}.value.errors != null) {
                                ${Elements.PlaceholderScreen.name}Content(null, %T(text = ${Variables.uiState}.value.errors!!.message))
                            }
                            else {
                                null
                            },
                            floatingActionButton = {
                                %T(onClick = { ${Variables.viewModel}.${rPF_Select!!.simpleName}() }) {
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
                            ClassNames.textClass,
                            ClassNames.floatingActionButtonClass,
                            ClassNames.iconClass,
                            ClassNames.mutableStateOfClass
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