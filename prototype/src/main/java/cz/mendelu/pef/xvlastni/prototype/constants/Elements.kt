package cz.mendelu.pef.xvlastni.prototype.constants

import com.squareup.kotlinpoet.ClassName

object Elements {
    object RandomList {
        val name = "randomList"
        val content = """
            return if (typeName.contains("String")) {
                "listOf(${RandomString.name}(), ${RandomString.name}(), ${RandomString.name}())"
            }
            else if (typeName.contains("Int") || typeName.contains("Long")) {
                "listOf((0..100).random(), (0..100).random(), (0..100).random())"
            }
            else if (typeName.contains("Float") || typeName.contains("Double")) {
                "listOf(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())"
            }
            else {
                "listOf()"
            }
        """.trimIndent()
    }
    object RandomString {
        val name = "randomString"
        val content = """
                val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
                return (1..6)
                    .map {
                        chars.random()
                    }
                    .joinToString("")
        """.trimIndent()
    }
    object UiState {
        val name = "UiState"
        val packageName = ".model"
        val content = """
            import java.io.Serializable

            open class UiState<T, E>
                (
                var loading: Boolean = true,
                var data: T? = null,
                var errors: E? = null) : Serializable {
            }
        """.trimIndent()
    }
    object Error {
        val name = "Error"
        val packageName = ".architecture"
        val content = """
            data class Error(
                val code: Int,
                val message: String?
            )
        """.trimIndent()
    }
    object BaseViewModel {
        val name = "BaseViewModel"
        val packageName = ".architecture"
        val content = """
            import androidx.lifecycle.ViewModel
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.Job
            import kotlin.coroutines.CoroutineContext

            abstract class BaseViewModel : ViewModel(), CoroutineScope {
                private val job = Job()
                override val coroutineContext: CoroutineContext = Dispatchers.Main + job
            }
        """.trimIndent()
    }
    object RapidListRow {
        val name = "RapidListRow"
        val packageName = ".elements"
        val content = """
            import androidx.compose.foundation.clickable
            import androidx.compose.foundation.layout.Arrangement
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.Row
            import androidx.compose.foundation.layout.fillMaxWidth
            import androidx.compose.foundation.layout.padding
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.ArrowForward
            import androidx.compose.material3.Icon
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.MutableState
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.res.painterResource
            import androidx.compose.ui.text.style.TextOverflow
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.text.TextStyle
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.unit.sp
            
            @Composable
            fun RapidListRow(
                title: String,
                subtitle: String,
                modifier: Modifier = Modifier
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = title, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis, 
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        
                        Text(
                            text = subtitle, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis, 
                            style = TextStyle(
                                fontSize = 16.sp
                            )
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        """.trimIndent()
    }
    object RapidRow {
        val name = "RapidRow"
        val packageName = ".elements"
        val content = """
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.Row
            import androidx.compose.foundation.layout.Spacer
            import androidx.compose.foundation.layout.fillMaxWidth
            import androidx.compose.foundation.layout.height
            import androidx.compose.foundation.layout.padding
            import androidx.compose.foundation.text.ClickableText
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.platform.LocalUriHandler
            import androidx.compose.ui.platform.testTag
            import androidx.compose.ui.res.stringResource
            import androidx.compose.ui.text.SpanStyle
            import androidx.compose.ui.text.TextStyle
            import androidx.compose.ui.text.buildAnnotatedString
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.text.style.TextDecoration
            import androidx.compose.ui.unit.dp
            
            @Composable
            fun RapidRow(
                trailing: String,
                leading: String
            ) {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Text(
                            text = trailing, 
                            style = TextStyle(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                         Text(
                            text = leading,
                            style = TextStyle(
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        """.trimIndent()
    }
    object BaseScreen {
        val name = "BaseScreen"
        val packageName = ".elements"
        val content = """
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.ArrowBack
            import androidx.compose.material3.*
            import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.MutableState
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.unit.dp

            @OptIn(ExperimentalMaterial3Api::class)
            @Composable
            fun BaseScreen(
                topBarText: String?,
                onBackClick: (() -> Unit)? = null,
                showSidePadding: Boolean = true,
                drawFullScreenContent: Boolean = false,
                placeholderScreenContent: PlaceholderScreenContent? = null,
                showLoading: Boolean = false,
                floatingActionButton: @Composable () -> Unit = {},
                bottomContent: @Composable (paddingValues: PaddingValues) -> Unit = {},
                bottomSheetContent: (@Composable () -> Unit)? = null,
                showBottomSheet: MutableState<Boolean> = mutableStateOf(false),
                actions: @Composable RowScope.() -> Unit = {},
                content: @Composable (paddingValues: PaddingValues) -> Unit
            ) {
                val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Scaffold(
                    contentColor = Color.Black,
                    containerColor = Color.White,
                    floatingActionButton = floatingActionButton,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                                ) {
                                    if(topBarText != null) {
                                        Text(
                                            text = topBarText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .padding(start = 0.dp)
                                                .weight(1.5f)
                                        )
                                    }
                                }
                            },
                            actions = actions,
                            navigationIcon = {
                                if (onBackClick != null) {
                                    IconButton(
                                        onClick = onBackClick,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                    }
                                }
                            },
                            colors = topAppBarColors(
                                containerColor = Color.White,
                                navigationIconContentColor = Color.Black,
                                titleContentColor = Color.Black,
                                actionIconContentColor = Color.Black
                            )
                        )
                    }
                ) {
                    if (placeholderScreenContent != null) {
                        PlaceholderScreen(
                            modifier = Modifier.padding(it),
                            content = placeholderScreenContent)
                    }
                    else if (showLoading) {
                        LoadingScreen(modifier = Modifier.padding(it))
                    }
                    else {
                        if (!drawFullScreenContent) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(it)
                                ) {
                                    item {
                                        Column(
                                            verticalArrangement = Arrangement.Top,
                                            modifier = Modifier
                                                .padding(if (!showSidePadding) basicMargin() else 0.dp)
                                        ) {
                                            content(it)
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .padding(basicMargin())
                                        .weight(1f, false),
                                ) {
                                    bottomContent(it)
                                }
                            }
                        }
                        else {
                            content(it)
                        }
                    }
                    if (bottomSheetContent != null) {
                        if  (showBottomSheet.value) {
                            ModalBottomSheet(
                                onDismissRequest = { showBottomSheet.value = false },
                                sheetState = bottomSheetState,
                            ) {
                                bottomSheetContent()
                                Spacer(modifier = Modifier.height(50.dp))
                            }
                        }
                    }
                }

            }
        """.trimIndent()
    }

    object PlaceholderScreen {
        val name = "PlaceholderScreen"
        val packageName = ".elements"
        val content = """
            import androidx.compose.foundation.Image
            import androidx.compose.foundation.layout.*
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.layout.ContentScale
            import androidx.compose.ui.res.painterResource
            import androidx.compose.ui.text.style.TextAlign
            import androidx.compose.ui.unit.dp
            
    
            data class PlaceholderScreenContent(val image: Int?, val text: String?)
    
            @Composable
            fun PlaceholderScreen(
                modifier: Modifier,
                content: PlaceholderScreenContent){
                Box(modifier = modifier.fillMaxSize()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(basicMargin())
                    ) {
                        if (content.image != null) {
                            Image(
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.width(300.dp),
                                painter = painterResource(id = content.image),
                                contentDescription = null)
                        }
    
                        Spacer(modifier = Modifier.height(basicMargin()))
    
                        if (content.text != null) {
                            Text(text = content.text,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        """.trimIndent()
    }

    object LoadingScreen {
        val name = "LoadingScreen"
        val packageName = ".elements"
        val content = """
            import androidx.compose.foundation.background
            import androidx.compose.foundation.layout.Arrangement
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.size
            import androidx.compose.material3.CircularProgressIndicator
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.unit.dp
    
            @Composable
            fun LoadingScreen(
                modifier: Modifier,
            ){
                Column(modifier = modifier.fillMaxSize()
                    .background(Color.White),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
    
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5.dp)
                }
            }
        """.trimIndent()
    }

    object Dimensions {
        val name = "Dimensions"
        val packageName = ".elements"
        val content = """
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.unit.Dp
            import androidx.compose.ui.unit.dp
    
            @Composable
            fun basicMargin(): Dp = 16.dp
    
            @Composable
            fun halfMargin(): Dp = 8.dp
    
            @Composable
            fun smallMargin(): Dp = 4.dp
        """.trimIndent()
    }
}