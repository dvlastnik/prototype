package cz.mendelu.pef.xvlastni.prototype.constants

import com.squareup.kotlinpoet.ClassName

object ClassNames {
    val composableClass = ClassName("androidx.compose.runtime", "Composable")
    val columnClass = ClassName("androidx.compose.foundation.layout", "Column")
    val textClass = ClassName("androidx.compose.material3", "Text")
    val modifierClass = ClassName("androidx.compose.ui", "Modifier")
    val hiltViewModelClass = ClassName("androidx.hilt.navigation.compose", "hiltViewModel")
    val stringResourceClass = ClassName("androidx.compose.ui.res", "stringResource")
    val mutableStateClass = ClassName("androidx.compose.runtime", "MutableState")
    val rememberSaveableClass = ClassName("androidx.compose.runtime.saveable", "rememberSaveable")
    val mutableStateOfClass = ClassName("androidx.compose.runtime", "mutableStateOf")
    val launchedEffectClass = ClassName("androidx.compose.runtime", "LaunchedEffect")
    val alignmentClass = ClassName("androidx.compose.ui", "Alignment")
    val arrangementClass = ClassName("androidx.compose.foundation.layout", "Arrangement")
    val paddingClass = ClassName("androidx.compose.foundation.layout", "padding")
    val floatingActionButtonClass = ClassName("androidx.compose.material3", "FloatingActionButton")
    val iconClass = ClassName("androidx.compose.material3", "Icon")
    val paddingValuesClass = ClassName("androidx.compose.foundation.layout", "PaddingValues")
    val lazyColumnClass = ClassName("androidx.compose.foundation.lazy", "LazyColumn")
    val dividerClass = ClassName("androidx.compose.material3", "Divider")
    val heightClass = ClassName("androidx.compose.foundation.layout", "height")
    val dpClass = ClassName("androidx.compose.ui.unit", "dp")
}