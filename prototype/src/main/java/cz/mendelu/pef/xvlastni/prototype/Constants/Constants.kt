package cz.mendelu.pef.xvlastni.prototype.Constants

object Constants {
    val fileBaseScreen = """
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
        fun PlaceHolderScreen(
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

    val fileLoadingScreen = """
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

    //TODO pridat import na import dimensions
    val filePlaceholderScreen = """
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
        fun PlaceHolderScreen(
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

    val fileDimensions = """
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