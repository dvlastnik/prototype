package cz.mendelu.pef.xvlastni.prototype

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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
import cz.mendelu.pef.xvlastni.prototype.constants.ClassNames
import cz.mendelu.pef.xvlastni.prototype.constants.Elements
import cz.mendelu.pef.xvlastni.prototype.constants.Variables

fun generateRapidPrototypeApi(
    packageName: String,
    className: String,
    fileName: String,
    uiStateClass: ClassName,
    errorClass: ClassName,
    repository: ClassName?,
    rPF_Select: ClassName?,
    properties: Sequence<KSPropertyDeclaration>,
    viewModelBuilder: TypeSpec.Builder,
    screenContentBuilder: FunSpec.Builder,
    screenBuilder: FunSpec.Builder
) {
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
                errorClass,
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
        .addFunction(selectFun!!)


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
        .beginControlFlow("if (${Variables.uiState}.data == null)")
        .addStatement("%T(text = %S)", ClassNames.textClass, "nothing in there")
        .endControlFlow()
        .beginControlFlow("else")

    properties.forEach { property ->
        val propertyName = property.simpleName.asString()
        screenContentBuilder
            .addStatement("RapidRow(trailing = %S, leading = ${Variables.uiState}.data!!.%L.toString())", propertyName, propertyName)
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
                                ${Elements.PlaceholderScreen.name}Content(null, ${Variables.uiState}.value.errors!!.message!!)
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
            ClassNames.floatingActionButtonClass,
            ClassNames.iconClass,
            ClassNames.mutableStateOfClass
        )
        .beginControlFlow("")
        .addStatement(
            "%T(${Variables.uiState} = ${Variables.uiState}.value, ${Variables.paddingValues} = it)",
            ClassName(packageName, fileName + "Content")
        )
        .endControlFlow()
}

fun generateRapidPrototypeApiList(
    packageName: String,
    className: String,
    fileName: String,
    uiStateClass: ClassName,
    errorClass: ClassName,
    repository: ClassName?,
    rPF_Select: ClassName?,
    rPF_Insert: ClassName?,
    rPF_Delete: ClassName?,
    properties: Sequence<KSPropertyDeclaration>,
    insertParameter: ClassName?,
    deleteParameter: ClassName?,
    deleteParameterName: String,
    viewModelBuilder: TypeSpec.Builder,
    screenContentBuilder: FunSpec.Builder,
    screenBuilder: FunSpec.Builder,
    bottomSheetContentBuilder: FunSpec.Builder,
    codeGenerator: CodeGenerator
) {
    // data class
    val dataClass = generateDataClass(packageName, className, codeGenerator)
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

    requireNotNull(repository) { "Repository is not annotated!" }
    requireNotNull(rPF_Select) { "At least GET function must be annotated!" }

    //SELECT - GET
    var fabOnClick = ""
    var fabIcon = "Icons.Default.Refresh"
    var selectFun: FunSpec? = null
    rPF_Select?.let {
        fabOnClick = "${Variables.viewModel}.${it.simpleName}()"

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
                                    ${Variables.uiState}.value = UiState(loading = false, data = %T(list = result.data), errors = null)
                            }
                        }
                    """.trimIndent(),
                ClassNames.launchClass,
                ClassNames.withContextClass,
                ClassNames.dispatchersClass,
                errorClass,
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
        fabIcon = "Icons.Default.Add"
        if (insertParameter != null) {
            fabOnClick = "${Variables.viewModel}.${it.simpleName}(${Variables.whatToInsert})"
            insertFun = FunSpec.builder(it.simpleName)
                .addParameter(className.lowercase(), insertParameter!!)
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
                                    is CommunicationResult.Success -> {
                                            Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                            ${Variables.repository}.${selectFun!!.name}()
                                        }
                                    }
                                }
                                """.trimIndent(),
                    ClassNames.launchClass,
                    ClassNames.withContextClass,
                    ClassNames.dispatchersClass,
                    errorClass,
                    "No internet",
                    "Failed to load the list",
                    "Unknown error"
                )
                .build()
        }
        else {
            fabOnClick = "${Variables.viewModel}.${it.simpleName}()"

            insertFun = FunSpec.builder(it.simpleName)
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
                                    is CommunicationResult.Success -> {
                                            Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                            ${Variables.repository}.${selectFun!!.name}()
                                        }
                                    }
                                }
                                """.trimIndent(),
                    ClassNames.launchClass,
                    ClassNames.withContextClass,
                    ClassNames.dispatchersClass,
                    errorClass,
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
                                    is CommunicationResult.Success -> {
                                            Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                            ${Variables.repository}.${selectFun!!.name}()
                                        }
                                    }
                                }
                                """.trimIndent(),
                    ClassNames.launchClass,
                    ClassNames.withContextClass,
                    ClassNames.dispatchersClass,
                    errorClass,
                    "No internet",
                    "Failed to load the list",
                    "Unknown error"
                )
                .build()
        }
        else {
            deleteFun = FunSpec.builder(it.simpleName)
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
                                    is CommunicationResult.Success -> {
                                            Log.d("Saved", ${Variables.uiState}.value.data.toString())
                                            ${Variables.repository}.${selectFun!!.name}()
                                        }
                                    }
                                }
                                """.trimIndent(),
                    ClassNames.launchClass,
                    ClassNames.withContextClass,
                    ClassNames.dispatchersClass,
                    errorClass,
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

    var iconDelete = ""
    deleteFun?.let {
        viewModelBuilder
            .addFunction(it)

        iconDelete = """
                        IconButton(
                            onClick = {
                                ${Variables.viewModel}.${rPF_Delete!!.simpleName}(it.${deleteParameterName}!!)
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

    var titleIndex = 0
    properties.forEachIndexed { index, property ->
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

    if (insertParameter != null) {
        screenBuilder
            .addParameter(Variables.whatToInsert, insertParameter)
    }

    screenBuilder
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
                                %T(onClick = { $fabOnClick }) {
                                    %T(imageVector = $fabIcon, contentDescription = null)
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
                                            $iconDelete
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
            ClassNames.rowClass
        )
        .beginControlFlow("")
        .addStatement(
            "%T(${Variables.uiState} = ${Variables.uiState}.value, ${Variables.paddingValues} = it, ${Variables.openBottomSheet} = ${Variables.openBottomSheet}, ${Variables.viewModel} = ${Variables.viewModel})",
            ClassName(packageName, fileName + "Content")
        )
        .endControlFlow()
}
fun generateRapidPrototypeDatabase(
    packageName: String,
    className: String,
    fileName: String,
    uiStateClass: ClassName,
    errorClass: ClassName,
    rPF_Select: ClassName?,
    rPF_Insert: ClassName?,
    rPF_Delete: ClassName?,
    properties: Sequence<KSPropertyDeclaration>,
    insertParameter: ClassName?,
    repository: ClassName?,
    viewModelBuilder: TypeSpec.Builder,
    screenContentBuilder: FunSpec.Builder,
    screenBuilder: FunSpec.Builder,
    bottomSheetContentBuilder: FunSpec.Builder,
    codeGenerator: CodeGenerator
) {
    requireNotNull(repository) { "Repository is not annotated" }
    requireNotNull(rPF_Select) { "At least SELECT function must be annotated!" }

    // data class
    val dataClass = generateDataClass(packageName, className, codeGenerator)
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
            .addCode(
                """
                            %T {
                                ${Variables.repository}.${it.simpleName}()
                                    .%T {
                                        ${Variables.uiState}.value = UiState(loading = true, data = null, errors = null)
                                    }
                                    .%T {
                                        ${Variables.uiState}.value = UiState(loading = false, data = null, errors = Error(0, it.localizedMessage))
                                    }
                                    .collect {
                                        ${Variables.uiState}.value = UiState(loading = false, data = ${dataClass.simpleName}(list = it, detail = null), errors = null)
                                    }
                            }
                        """.trimIndent(),
                ClassNames.launchClass,
                ClassNames.flowOnStartClass,
                ClassNames.flowCatchClass
            )
            .build()
    }
    //SELECT

    //INSERT
    var insertFun: FunSpec? = null
    rPF_Insert?.let {
        if (insertParameter != null) {
            insertFun = FunSpec.builder(it.simpleName)
                .addParameter(className.lowercase(), insertParameter!!)
                .addCode(
                    """
                            launch {
                                val id = ${Variables.repository}.${it.simpleName}(${className.lowercase()})
                
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
        else {
            insertFun = FunSpec.builder(it.simpleName)
                .addCode(
                    """
                            launch {
                                val ${className.lowercase()} = init$className()
                
                                val id = ${Variables.repository}.${it.simpleName}()
                
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
    }
    //INSERT

    //DELETE
    var deleteFun: FunSpec? = null
    rPF_Delete?.let {
        deleteFun = FunSpec.builder(it.simpleName)
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

        if (insertParameter != null) {
            fabInsert = """
                {
                    FloatingActionButton(onClick = { ${Variables.viewModel}.${rPF_Insert!!.simpleName}(${Variables.whatToInsert}) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                },
            """.trimIndent()
        }
        else {
            fabInsert = """
                {
                    FloatingActionButton(onClick = { ${Variables.viewModel}.${rPF_Insert!!.simpleName}() }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                },
            """.trimIndent()
        }
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
        .build()

    var titleIndex = 0
    properties.forEachIndexed { index, property ->
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

    if (insertParameter != null) {
        screenBuilder
            .addParameter(Variables.whatToInsert, insertParameter)
    }

    screenBuilder
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

fun createScreenContentForList(
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

fun createBottomSheetContent(
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

fun createSetDetailFunSpec(className: String, packageName: String): FunSpec {
    return FunSpec.builder("set${className}Detail")
        .addParameter(className.lowercase(), ClassName(packageName, className))
        .addCode(
            """
                            ${Variables.uiState}.value.data!!.detail = ${className.lowercase()}
                        """.trimIndent()
        )
        .build()
}

fun generateDataClass(packageName: String, className: String, codeGenerator: CodeGenerator): ClassName {
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
        fileName = dataClass.simpleName,
        codeGenerator = codeGenerator
    )

    return dataClass
}

fun generateFileWithFileSpec(packageName: String, fileName: String, fileSpec: FileSpec, codeGenerator: CodeGenerator) {
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies.ALL_FILES,
        packageName = packageName,
        fileName = fileName
    )

    file.writer().use { writer ->
        fileSpec.writeTo(writer)
    }
}

fun generateFile(content: String, packageName: String, fileName: String, codeGenerator: CodeGenerator) {
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