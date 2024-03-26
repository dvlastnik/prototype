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
                fileName = Elements.Dimensions.name
            )
            generateFile(
                content = Elements.LoadingScreen.content,
                packageName = "$packageName${Elements.LoadingScreen.packageName}",
                fileName = Elements.LoadingScreen.name
            )
            generateFile(
                content = Elements.PlaceholderScreen.content,
                packageName = "$packageName${Elements.PlaceholderScreen.packageName}",
                fileName = Elements.PlaceholderScreen.name
            )
            generateFile(
                content = Elements.BaseScreen.content,
                packageName = "$packageName${Elements.BaseScreen.packageName}",
                fileName = Elements.BaseScreen.name
            )
            generateFile(
                content = Elements.RapidRow.content,
                packageName = "$packageName${Elements.RapidRow.packageName}",
                fileName = Elements.RapidRow.name
            )
            generateFile(
                content = Elements.RapidListRow.content,
                packageName = "$packageName${Elements.RapidListRow.packageName}",
                fileName = Elements.RapidListRow.name
            )
            generateFile(
                content = Elements.BaseViewModel.content,
                packageName = "$packageName${Elements.BaseViewModel.packageName}",
                fileName = Elements.BaseViewModel.name
            )
            generateFile(
                content = Elements.Error.content,
                packageName = "$packageName${Elements.Error.packageName}",
                fileName = Elements.Error.name
            )
            generateFile(
                content = Elements.UiState.content,
                packageName = "$packageName${Elements.UiState.packageName}",
                fileName = Elements.UiState.name
            )


            val uiStateClass = ClassName(packageName + Elements.UiState.packageName, Elements.UiState.name)
            val errorClass = ClassName(packageName + Elements.Error.packageName, Elements.Error.name)

            val viewModelBuilder = TypeSpec.classBuilder(className + "ViewModel")
            val screenContentBuilder = FunSpec.builder(fileName + "Content")
            val screenBuilder = FunSpec.builder(fileName)
            val bottomSheetContentBuilder = FunSpec.builder(className + "BottomSheetContent")

            if (isList == true && type == RapidPrototypeType.DATABASE) {
                generateRapidPrototypeDatabase(
                    packageName, className, fileName, uiStateClass, errorClass, rPF_Select, rPF_Insert, rPF_Delete, properties, repository, viewModelBuilder, screenContentBuilder, screenBuilder, bottomSheetContentBuilder
                )
            }
            else if (isList == true && type == RapidPrototypeType.API) {
                // data class
                val dataClass = generateDataClass(packageName, className)
                // viewModel
                // UISTATE
                val uiStateTypeName = uiStateClass
                    .parameterizedBy(
                        dataClass, errorClass
                    )

                val mutableStateTypeName = ClassNames.mutableStateClass
                    .parameterizedBy(uiStateTypeName)

                val uiStateProperty = PropertySpec.builder(Variables.uiState, mutableStateTypeName)
                    .initializer("%T(UiState())", ClassNames.mutableStateOfClass)
                    .build()
                //UISTATE

                requireNotNull(repository) { "Repository is not annotated" }

                //SELECT - GET
                var selectFun: FunSpec? = null
                rPF_Select?.let {
                    selectFun = FunSpec.builder(it.simpleName)
                        .addModifiers(KModifier.OPEN)
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
                                    ${Variables.uiState}.value = UiState(loading = false, data = %T(list = result.data), errors = null)
                            }
                        }
                    """.trimIndent(),
                            ClassNames.launchClass,
                            ClassNames.withContextClass,
                            ClassNames.dispatchersClass,
                            ClassName(packageName + Elements.Error.packageName, Elements.Error.name),
                            "No internet",
                            "Failed to load the list",
                            "Unknown error",
                            dataClass
                        )
                        .build()
                }
                //SELECT - GET

                //INSERT - POST
                var insertFun: FunSpec? = null
                rPF_Insert?.let {
                    if (insertParameter != null) {
                        insertFun = FunSpec.builder(it.simpleName)
                            .addParameter(className.lowercase(), insertParameter!!)
                            .addModifiers(KModifier.OPEN)
                            .addCode(
                                """
                                %T {
                                    val result = %T(%T.IO) {
                                        ${Variables.repository}.${it.simpleName}(${className.lowercase()})
                                    }
                                    
                                    when (result) {
                                        is CommunicationResult.CommunicationError ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(code = 0 ,message = %S))
                                    is CommunicationResult.Error ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 1, message = %S))
                                    is CommunicationResult.Exception ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 2, message = %S))
                                    is CommunicationResult.Success ->
                                        Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                        ${selectFun!!.name}()
                                    }
                                }
                                """.trimIndent(),
                                    ClassNames.launchClass,
                                    ClassNames.withContextClass,
                                    ClassNames.dispatchersClass,
                                    ClassName(
                                        packageName + Elements.Error.packageName,
                                        Elements.Error.name
                                    ),
                                    "No internet",
                                    "Failed to load the list",
                                    "Unknown error"
                                )
                            .build()
                    }
                    else {
                        insertFun = FunSpec.builder(it.simpleName)
                            .addModifiers(KModifier.OPEN)
                            .addCode(
                                """
                                %T {
                                    val result = %T(%T.IO) {
                                        ${Variables.repository}.${it.simpleName}()
                                    }
                                    
                                    when (result) {
                                        is CommunicationResult.CommunicationError ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(code = 0 ,message = %S))
                                    is CommunicationResult.Error ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 1, message = %S))
                                    is CommunicationResult.Exception ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 2, message = %S))
                                    is CommunicationResult.Success ->
                                        Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                        ${selectFun!!.name}()
                                    }
                                }
                                """.trimIndent(),
                                        ClassNames.launchClass,
                                        ClassNames.withContextClass,
                                        ClassNames.dispatchersClass,
                                        ClassName(
                                            packageName + Elements.Error.packageName,
                                            Elements.Error.name
                                        ),
                                        "No internet",
                                        "Failed to load the list",
                                        "Unknown error",
                                        dataClass
                                    )
                            .build()
                    }
                }
                //INSERT - POST

                //DELETE - DELETE
                var deleteFun: FunSpec? = null
                rPF_Delete?.let {
                    if (deleteParameter != null) {
                        deleteFun = FunSpec.builder(it.simpleName)
                            .addParameter(className.lowercase(), deleteParameter!!)
                            .addModifiers(KModifier.OPEN)
                            .addCode(
                                """
                                %T {
                                    val result = %T(%T.IO) {
                                        ${Variables.repository}.${it.simpleName}(${className.lowercase()})
                                    }
                                    
                                    when (result) {
                                        is CommunicationResult.CommunicationError ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(code = 0 ,message = %S))
                                    is CommunicationResult.Error ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 1, message = %S))
                                    is CommunicationResult.Exception ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 2, message = %S))
                                    is CommunicationResult.Success ->
                                        Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                        ${selectFun!!.name}()
                                    }
                                }
                                """.trimIndent(),
                                ClassNames.launchClass,
                                ClassNames.withContextClass,
                                ClassNames.dispatchersClass,
                                ClassName(
                                    packageName + Elements.Error.packageName,
                                    Elements.Error.name
                                ),
                                "No internet",
                                "Failed to load the list",
                                "Unknown error"
                            )
                            .build()
                    }
                    else {
                        deleteFun = FunSpec.builder(it.simpleName)
                            .addModifiers(KModifier.OPEN)
                            .addCode(
                            """
                                %T {
                                    val result = %T(%T.IO) {
                                        ${Variables.repository}.${it.simpleName}()
                                    }
                                    
                                    when (result) {
                                        is CommunicationResult.CommunicationError ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(code = 0 ,message = %S))
                                    is CommunicationResult.Error ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 1, message = %S))
                                    is CommunicationResult.Exception ->
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(code = 2, message = %S))
                                    is CommunicationResult.Success ->
                                        Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                        ${selectFun!!.name}()
                                    }
                                }
                                """.trimIndent(),
                                ClassNames.launchClass,
                                ClassNames.withContextClass,
                                ClassNames.dispatchersClass,
                                ClassName(
                                    packageName + Elements.Error.packageName,
                                    Elements.Error.name
                                ),
                                "No internet",
                                "Failed to load the list",
                                "Unknown error",
                                dataClass
                            )
                            .build()
                    }
                }
                //DELETE - DELETE

                //SET DETAIL
                val setDetailFun = createSetDetailFunSpec(className, packageName)
                //SET DETAIL

                viewModelBuilder
                    .addAnnotation(ClassNames.daggerHiltViewModelClass)
                    .primaryConstructor(FunSpec.constructorBuilder()
                        .addAnnotation(ClassNames.injectClass)
                        .addParameter(Variables.repository, repository!!)
                        .build())
                    .superclass(ClassName(packageName + Elements.BaseViewModel.packageName, Elements.BaseViewModel.name))
                    .addProperty(PropertySpec.builder(Variables.repository, repository!!, KModifier.PRIVATE).initializer(Variables.repository).build())
                    .addProperty(uiStateProperty)
                    .addFunction(setDetailFun)

                selectFun?.let {
                    viewModelBuilder
                        .addFunction(it)
                }

                insertFun?.let {
                    viewModelBuilder
                        .addFunction(it)
                }

                var titleIndex = 0
                properties.forEachIndexed { index, property ->
                    logger.warn("property: $property, $index")
                    if (property.simpleName.asString() == "id") {
                        titleIndex = index
                    }
                }
                val firstPropertyName = properties.elementAt(titleIndex).simpleName.asString()
                val secondPropertyName = properties.elementAt(titleIndex + 1).simpleName.asString()

                val propertiesCode = properties.joinToString(separator = "\n") {
                    val propertyName = it.simpleName.asString()
                    """RapidRow(trailing = "$propertyName", leading = it.$propertyName.toString())"""
                }

                //create content
                createScreenContentForList(
                    screenContentBuilder, uiStateTypeName, packageName, className, firstPropertyName, secondPropertyName
                )

                //create bottom sheet content
                createBottomSheetContent(
                    bottomSheetContentBuilder = bottomSheetContentBuilder,
                    packageName = packageName,
                    className = className,
                    propertiesCode = propertiesCode
                )

                //create screen
                screenBuilder
                    .addAnnotation(ClassNames.composableClass)
                    .addStatement(
                        "val ${Variables.viewModel} = %T<${className}ViewModel>()",
                        ClassNames.hiltViewModelClass
                    )
                    .addStatement("")
                    .beginControlFlow(
                        "val ${Variables.uiState}: %T<%T<${dataClass.simpleName}, %T>> = %T ", //{
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
                            drawFullScreenContent = false,
                            showLoading = ${Variables.uiState}.value.loading,
                            placeholderScreenContent = if (uiState.value.errors != null) {
                                PlaceholderScreenContent(null, ${Variables.uiState}.value.errors!!.message)
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
                            showBottomSheet = ${Variables.openBottomSheet},
                            bottomSheetContent = {
                                %T(
                                    modifier = %T
                                        .padding(basicMargin())
                                ) {
                                    ${Variables.uiState}.value.data!!.detail?.let {
                                        %T(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            %T(
                                                onClick = {
                                                    //tady jsem vymazal delete xd
                                                    ${Variables.openBottomSheet}.value = false
                                                }
                                            ) {
                                                %T(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        ${className}BottomSheetContent(it)
                                    }
                                }
                            }
                        )   
                    """.trimIndent(),
                        className,
                        ClassNames.floatingActionButtonClass,
                        ClassNames.iconClass,
                        ClassNames.columnClass,
                        ClassNames.modifierClass,
                        ClassNames.rowClass,
                        ClassNames.iconButtonClass,
                        ClassNames.iconClass
                    )
                    .beginControlFlow("")
                    .addStatement(
                        "%T(${Variables.uiState} = ${Variables.uiState}.value, ${Variables.paddingValues} = it, ${Variables.openBottomSheet} = ${Variables.openBottomSheet}, ${Variables.viewModel} = ${Variables.viewModel})",
                        ClassName(packageName, fileName + "Content")
                    )
                    .endControlFlow()


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
                .addFunction(screenBuilder.build())
                .addFunction(screenContentBuilder.build())
                .addFunction(bottomSheetContentBuilder.build())
                .build()

            generateFileWithFileSpec(packageName, fileName, fileSpec)

            val viewModelFileSpec = FileSpec.builder(packageName, "${className}ViewModel")
                .addImport("android.util", "Log")
                .addImport("cz.mendelu.pef.xvlastni.prototype.classes", "CommunicationResult")
                .addType(viewModelBuilder.build())
                .build()

            generateFileWithFileSpec(packageName, "${className}ViewModel", viewModelFileSpec)
        }

        return emptyList()
    }

    private fun generateRapidPrototypeDatabase(
        packageName: String,
        className: String,
        fileName: String,
        uiStateClass: ClassName,
        errorClass: ClassName,
        rPF_Select: ClassName?,
        rPF_Insert: ClassName?,
        rPF_Delete: ClassName?,
        properties: Sequence<KSPropertyDeclaration>,
        repository: ClassName?,
        viewModelBuilder: TypeSpec.Builder,
        screenContentBuilder: FunSpec.Builder,
        screenBuilder: FunSpec.Builder,
        bottomSheetContentBuilder: FunSpec.Builder
    ) {
        // data class
        val dataClass = generateDataClass(packageName, className)
        // viewModel
        // UISTATE
        val uiStateTypeName = uiStateClass
            .parameterizedBy(
                dataClass, errorClass
            )

        val mutableStateTypeName = ClassNames.mutableStateClass
            .parameterizedBy(uiStateTypeName)

        val uiStateProperty = PropertySpec.builder(Variables.uiState, mutableStateTypeName)
            .initializer("%T(UiState())", ClassNames.mutableStateOfClass)
            .build()
        //UISTATE

        //SELECT
        var selectFun: FunSpec? = null
        rPF_Select?.let {
            selectFun = FunSpec.builder(it.simpleName)
                .addModifiers(KModifier.OPEN)
                .addCode(
                    """
                            %T {
                                ${Variables.repository}.${it.simpleName}()
                                    .%T {
                                        ${Variables.uiState}.value = UiState(loading = true, data = null, errors = null)
                                    }
                                    .%T {
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = %T(0, it.localizedMessage))
                                    }
                                    .collect {
                                        ${Variables.uiState}.value = UiState(loading = false, data = ${dataClass.simpleName}(list = it, detail = null), errors = null)
                                    }
                            }
                        """.trimIndent(),
                    ClassNames.launchClass,
                    ClassNames.flowOnStartClass,
                    ClassNames.flowCatchClass,
                    ClassName("cz.mendelu.pef.xvlastni.prototype.classes", "Error")
                )
                .build()
        }
        //SELECT

        //INSERT
        var insertFun: FunSpec? = null
        rPF_Insert?.let {
            insertFun = FunSpec.builder(it.simpleName)
                .addModifiers(KModifier.OPEN)
                .addCode(
                    """
                            launch {
                                val ${className.lowercase()} = init$className()
                
                                val id = ${Variables.repository}.${it.simpleName}(user)
                
                                if (id > 0) {
                                    Log.d("Rapid Prototype", "Saved")
                                } else {
                                    Log.d("Rapid Prototype", "Not saved")
                                }
                            }
                        """.trimIndent()
                )
                .build()
        }
        //INSERT

        //DELETE
        var deleteFun: FunSpec? = null
        rPF_Delete?.let {
            deleteFun = FunSpec.builder(it.simpleName)
                .addModifiers(KModifier.OPEN)
                .addParameter(className.lowercase(), ClassName(packageName, className))
                .addCode(
                    """
                            launch {
                                ${Variables.repository}.${it.simpleName}(${className.lowercase()})
                            }
                        """.trimIndent()
                )
                .build()
        }
        //DELETE

        //SET DETAIL
        val setDetailFun = createSetDetailFunSpec(className, packageName)
        //SET DETAIL

        requireNotNull(repository) { "Repository is not annotated" }

        viewModelBuilder
            .addAnnotation(ClassNames.daggerHiltViewModelClass)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addAnnotation(ClassNames.injectClass)
                    .addParameter(Variables.repository, repository)
                    .build()
            )
            .superclass(
                ClassName(
                    packageName + Elements.BaseViewModel.packageName,
                    Elements.BaseViewModel.name
                )
            )
            .addProperty(
                PropertySpec.builder(Variables.repository, repository, KModifier.PRIVATE)
                    .initializer(Variables.repository).build()
            )
            .addProperty(uiStateProperty)

        selectFun?.let {
            viewModelBuilder
                .addFunction(it)
        }

        var fabInsert = "{},"
        insertFun?.let {
            viewModelBuilder
                .addFunction(it)

            fabInsert = """
                {
                    FloatingActionButton(onClick = { ${Variables.viewModel}.${rPF_Insert!!.simpleName}() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                },
            """.trimIndent()
        }

        var iconDelete = ""
        deleteFun?.let {
            viewModelBuilder
                .addFunction(it)

            iconDelete = """
                IconButton(
                    onClick = {
                        ${Variables.viewModel}.${rPF_Delete!!.simpleName}(${Variables.uiState}.value.data!!.detail!!)
                        ${Variables.openBottomSheet}.value = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            """.trimIndent()
        }

        viewModelBuilder
            .addFunction(setDetailFun)
            .addFunction(generateModelInitFunction(properties, ClassName(packageName, className)))
            .addFunction(
                FunSpec.builder(Elements.RandomString.name)
                    .returns(String::class)
                    .addCode(Elements.RandomString.content)
                    .build()
            )
            .addFunction(
                FunSpec.builder(Elements.RandomList.name)
                    .addParameter("typeName", String::class)
                    .returns(String::class)
                    .addCode(Elements.RandomList.content)
                    .build()
            )
            .build()

        var titleIndex = 0
        properties.forEachIndexed { index, property ->
            logger.warn("property: $property, $index")
            if (property.simpleName.asString() == "id") {
                titleIndex = index
            }
        }
        val firstPropertyName = properties.elementAt(titleIndex).simpleName.asString()
        val secondPropertyName = properties.elementAt(titleIndex + 1).simpleName.asString()

        val propertiesCode = properties.joinToString(separator = "\n") {
            val propertyName = it.simpleName.asString()
            """RapidRow(trailing = "$propertyName", leading = it.$propertyName.toString())"""
        }

        //creating content
        createScreenContentForList(
            screenContentBuilder = screenContentBuilder,
            uiStateTypeName = uiStateTypeName,
            packageName = packageName,
            className = className,
            firstPropertyName = firstPropertyName,
            secondPropertyName = secondPropertyName
        )

        //creating bottom content
        createBottomSheetContent(
            bottomSheetContentBuilder = bottomSheetContentBuilder,
            packageName = packageName,
            className = className,
            propertiesCode = propertiesCode
        )

        screenBuilder
            .addAnnotation(ClassNames.composableClass)
            .addStatement(
                "val ${Variables.viewModel} = %T<${className}ViewModel>()",
                ClassNames.hiltViewModelClass
            )
            .addStatement("")
            .beginControlFlow(
                "val ${Variables.uiState}: %T<%T<${dataClass.simpleName}, %T>> = %T ", //{
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

        rPF_Select?.let {
            screenBuilder
                .addStatement("")
                .beginControlFlow("%T(Unit)", ClassNames.launchedEffectClass)
                .addStatement("${Variables.viewModel}.${it.simpleName}()")
                .endControlFlow()
        }

        screenBuilder
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
                            drawFullScreenContent = false,
                            showLoading = ${Variables.uiState}.value.loading,
                            placeholderScreenContent = if (uiState.value.errors != null) {
                                PlaceholderScreenContent(null, ${Variables.uiState}.value.errors!!.message)
                            }
                            else {
                                null
                            },
                            floatingActionButton = $fabInsert
                            actions = {},
                            bottomContent = {},
                            showBottomSheet = ${Variables.openBottomSheet},
                            bottomSheetContent = {
                                %T(
                                    modifier = %T
                                        .padding(basicMargin())
                                ) {
                                    ${Variables.uiState}.value.data!!.detail?.let {
                                        %T(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            $iconDelete
                                        }
                                        ${className}BottomSheetContent(it)
                                    }
                                }
                            }
                        )   
                    """.trimIndent(),
                className,
                ClassNames.columnClass,
                ClassNames.modifierClass,
                ClassNames.rowClass
            )
            .beginControlFlow("")
            .addStatement(
                "%T(${Variables.uiState} = ${Variables.uiState}.value, ${Variables.paddingValues} = it, ${Variables.openBottomSheet} = ${Variables.openBottomSheet}, ${Variables.viewModel} = ${Variables.viewModel})",
                ClassName(packageName, fileName + "Content")
            )
            .endControlFlow()
    }

    private fun createScreenContentForList(
        screenContentBuilder: FunSpec.Builder,
        uiStateTypeName: ParameterizedTypeName,
        packageName: String,
        className: String,
        firstPropertyName: String,
        secondPropertyName: String
    ) {
        screenContentBuilder
            .addAnnotation(ClassNames.composableClass)
            .addParameter(
                Variables.uiState,
                uiStateTypeName
            )
            .addParameter(Variables.paddingValues, ClassNames.paddingValuesClass)
            .addParameter(
                Variables.openBottomSheet,
                ClassNames.mutableStateClass.parameterizedBy(Boolean::class.asClassName())
            )
            .addParameter(Variables.viewModel, ClassName(packageName, "${className}ViewModel"))
            .beginControlFlow(
                """
                        %T(
                            modifier = %T
                                .fillMaxSize()           
                        )
                        """.trimIndent(),
                ClassNames.columnClass,
                ClassNames.modifierClass
            )
            .beginControlFlow("${Variables.uiState}.data?.let { data ->")
            .beginControlFlow("data.list.forEachIndexed { index, it ->")
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
            .endControlFlow()
            .addStatement(
                """
                        RapidListRow(
                            title = it.$firstPropertyName.toString(),
                            subtitle = it.$secondPropertyName.toString(),
                            modifier = Modifier.%T {
                                ${Variables.viewModel}.set${className}Detail(it)
                                ${Variables.openBottomSheet}.value = true
                            }
                        )
                    """.trimIndent(),
                ClassNames.clickableClass
            )
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
    }

    private fun createBottomSheetContent(
        bottomSheetContentBuilder: FunSpec.Builder,
        packageName: String,
        className: String,
        propertiesCode: String
    ) {
        bottomSheetContentBuilder
            .addAnnotation(ClassNames.composableClass)
            .addParameter("it", ClassName(packageName, className))
            .addCode(propertiesCode)
    }

    private fun createSetDetailFunSpec(className: String, packageName: String): FunSpec {
        return FunSpec.builder("set${className}Detail")
            .addModifiers(KModifier.OPEN)
            .addParameter(className.lowercase(), ClassName(packageName, className))
            .addCode(
                """
                            ${Variables.uiState}.value.data!!.detail = ${className.lowercase()}
                        """.trimIndent()
            )
            .build()
    }


    private fun generateModelInitFunction(properties: Sequence<KSPropertyDeclaration>, dataClass: ClassName): FunSpec {
        val codeBlockBuilder = CodeBlock.builder()
            .add("return %T(\n", dataClass)

        properties.forEachIndexed { index, property ->
            if (property.simpleName.asString() != "id") {
                val typeName = property.type.resolve().declaration.qualifiedName?.asString()
                val isLastProperty = index == properties.count() - 1

                val defaultValue = when {
                    typeName == "kotlin.String" -> "randomString()"
                    typeName == "kotlin.Int" || typeName == "kotlin.Long" -> "(1..100).random()"
                    typeName == "kotlin.Float" || typeName == "kotlin.Double" -> "Random.nextFloat()"
                    typeName?.startsWith("kotlin.collections.List") == true -> "${Elements.RandomList.name}(typeName)"
                    else -> "null"
                }

                codeBlockBuilder.add("    %L = %L", property.simpleName.asString(), defaultValue)

                if (!isLastProperty) {
                    codeBlockBuilder.add(",\n")
                }
            }
        }

        codeBlockBuilder.add(")")

        return FunSpec.builder("init${dataClass.simpleName}")
            .returns(dataClass)
            .addCode(codeBlockBuilder.build())
            .build()
    }
    private fun generateDataClass(packageName: String, className: String): ClassName {
        val dataClass = ClassName(packageName, "${className}Data")
        val dataClassString = """
                import $packageName.$className
                
                data class ${dataClass.simpleName}(
                    val list: List<$className>,
                    var detail: $className? = null
                )
            """.trimIndent()
        generateFile(
            content = dataClassString,
            packageName = dataClass.packageName,
            fileName = dataClass.simpleName
        )

        return dataClass
    }
    private fun generateElements(content: String, packageName: String, fileName: String) {
        val contentWithImport = buildString {
            appendLine("import $packageName.elements.${Elements.Dimensions.name}")
            append(content)
        }

        generateFile(contentWithImport, packageName, fileName)
    }

    private fun generateFileWithFileSpec(packageName: String, fileName: String, fileSpec: FileSpec) {
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = packageName,
            fileName = fileName
        )

        file.writer().use { writer ->
            fileSpec.writeTo(writer)
        }
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