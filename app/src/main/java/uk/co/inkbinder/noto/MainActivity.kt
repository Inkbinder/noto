package uk.co.inkbinder.noto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uk.co.inkbinder.noto.ui.NotoApp
import uk.co.inkbinder.noto.ui.theme.NotoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as NotoApplication).appContainer
        setContent {
            NotoTheme {
                NotoApp(appContainer = appContainer)
            }
        }
    }
}

