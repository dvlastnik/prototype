package cz.mendelu.pef.xvlastni.compose_rapid_prototyping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.ActivityScreen
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.ui.theme.ComposeRapidPrototypingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeRapidPrototypingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ActivityScreen(whatToInsert = Activity(key = "000", accessibility = 1.0, activity = "programovat", link = "", participants = 1, price = 1.0, type = "programming"))
                }
            }
        }
    }
}