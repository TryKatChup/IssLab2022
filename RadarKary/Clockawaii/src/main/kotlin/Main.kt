import Friends.coroutineScope
import Friends.punti
import Friends.radius
import Friends.radiussave
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        app()
    }
}


@Composable
fun app() {
    punti = remember { mutableStateMapOf() }
    radius = remember { mutableStateOf(0f) }
    radiussave = remember { mutableStateOf(0f) }
    coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        draw()
    }
}


fun requestpointy(): Float {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://192.168.1.100:9999"))
        .build();

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    //println(response.body())
    return response.body().toFloat()
}

fun generaterandomtheta(): Pair<Double, Double> {
    val t = (2) * Math.PI * Math.random()
    val r = sqrt(Math.random())
    return Pair(r, t)
}

fun generaterandomy(radius: Float): Double {
    val a = generaterandomtheta()
    return (radius * a.first * sin(a.second))
}

fun generaterandomx(radius: Float): Double {
    val a = generaterandomtheta()
    return (radius * a.first * cos(a.second))
}

@Composable
fun draw() {


    val canvasWidth = remember { mutableStateOf(0f) }
    val canvasHeight = remember { mutableStateOf(0f) }

    coroutineScope.launch {
        withContext(Dispatchers.IO) {
            while (true) {
                //punti.clear()
                val y = requestpointy()
                val randomx = Random.nextInt(-15, 15)
                punti[Offset(randomx.toFloat(), -y)] = Punto()
                delay(1000)

            }
        }

    }


    val infiniteTransition = rememberInfiniteTransition()
    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                180f at 1000
            }
        )
    )



    Canvas(modifier = Modifier.fillMaxSize()) {
        canvasWidth.value = size.width
        canvasHeight.value = size.height
        radius.value = size.minDimension / 4
        val centerx = size.center.x
        val centery = size.center.y

        val horizontalGradientBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Green,
                Color(("ff" + "#bbfcd2".removePrefix("#").lowercase()).toLong(16)),
                Color.Transparent
            )
        )



        drawCircle(
            color = Color.Green,
            center = Offset(x = centerx, y = centery),
            radius = radius.value + 3,
            style = Fill
        )
        drawCircle(
            color = Color.Black,
            center = Offset(x = centerx, y = centery),
            radius = radius.value,
            style = Fill
        )
        drawCircle(
            color = Color.Green,
            center = Offset(x = centerx, y = centery),
            radius = (radius.value / 1.5).toFloat(),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.Green,
            center = Offset(x = centerx, y = centery),
            radius = radius.value / 4,
            style = Stroke(width = 1.5f)
        )

        drawLine(
            start = Offset(x = centerx - radius.value, y = centery),
            end = Offset(
                x = centerx + radius.value,
                y = centery
            ),
            color = Color.Green,
            alpha = 0.5f
        )

        drawLine(
            start = Offset(x = centerx, y = centery - radius.value),
            end = Offset(
                x = centerx,
                y = centery + radius.value
            ),
            color = Color.Green,
            alpha = 0.5f
        )

        rotate(45f) {
            drawLine(
                start = Offset(x = centerx, y = centery - radius.value),
                end = Offset(
                    x = centerx,
                    y = centery + radius.value
                ),
                color = Color.Green,
                alpha = 0.5f
            )
        }

        rotate(-45f) {
            drawLine(
                start = Offset(x = centerx, y = centery - radius.value),
                end = Offset(
                    x = centerx,
                    y = centery + radius.value
                ),
                color = Color.Green,
                alpha = 0.5f
            )
        }



        for (punto in punti.entries) {

            radiussave.value = radius.value
            if (punto.value.visible && !punto.value.started) {
                punto.value.time = LocalDateTime.now()
                punto.value.started = true
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {

                        delay(5000) //Todo: calculate this proportional to the rotation animation speed
                        punto.value.visible = false
                    }
                }

            }

            drawCircle(
                color = Color(("ff" + "#00c20d".removePrefix("#").lowercase()).toLong(16)),
                center = Offset(
                    x = (punto.key.x * (radius.value / radiussave.value)) + centerx,
                    y = (punto.key.y * (radius.value / radiussave.value)) + centery
                ),
                radius = 5f,
                style = Fill,
                alpha = if (punto.value.visible && punto.value.started) (1 - (LocalDateTime.now().second - punto.value.time.second).toFloat() / 5).coerceIn(
                    0f,
                    1f
                ) else 0f
            )
        }



        rotate(-rotationAnimation) {
            drawArc(
                horizontalGradientBrush,
                0f,
                60f,
                useCenter = true,
                size = Size(radius.value * 2, radius.value * 2),
                topLeft = Offset((centerx) - radius.value, (centery) - radius.value),
                alpha = 0.3f,
                blendMode = BlendMode.SrcAtop
            )

            drawLine(
                start = Offset(x = centerx, y = centery),
                end = Offset(
                    x = centerx + radius.value,
                    y = centery
                ),
                color = Color.Green,
                strokeWidth = 5F,

                )
        }

    }

    Column {
        Button({
            radiussave.value = radius.value
            var generare = 1000
            while (generare != 0) {
                val x1 = generaterandomx(radius.value).toFloat()
                val y1 = generaterandomy(radius.value).toFloat()
                if ((x1 * x1) + (y1 * y1) < radius.value.pow(2)) {

                    punti[Offset(
                        x1,
                        y1
                    )] = Punto()
                    generare -= 1
                }
            }

        }) {
            Text("Ciao")
        }

        Button({
            punti.clear()

        }) {
            Text("Ciao Ciao")
        }
    }

}


