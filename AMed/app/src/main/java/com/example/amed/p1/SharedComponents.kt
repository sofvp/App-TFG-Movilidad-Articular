package com.example.amed.p1

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun AngleDisplay(
    title: String,
    angle: Float,
    modifier: Modifier = Modifier,
    comparisonAngle: Float? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val center = Offset(x = size.width / 2, y = size.height / 2)
            val radius = size.width / 2 - 20f

            drawLine(
                color = onSurfaceVariantColor,
                start = Offset(x = center.x - radius, y = center.y),
                end = Offset(x = center.x + radius, y = center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.LightGray,
                start = Offset(x = center.x, y = center.y - radius),
                end = Offset(x = center.x, y = center.y + radius),
                strokeWidth = 1f
            )

            val angleInRad = Math.toRadians(-angle.toDouble())
            val endPoint = Offset(
                x = center.x + (radius * cos(angleInRad)).toFloat(),
                y = center.y + (radius * sin(angleInRad)).toFloat()
            )
            drawLine(
                color = primaryColor,
                start = center,
                end = endPoint,
                strokeWidth = 5f
            )

            drawArc(
                color = primaryColor,
                startAngle = 0f,
                sweepAngle = -angle,
                useCenter = false,
                style = Stroke(width = 4f),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            if (comparisonAngle != null) {
                val comparisonAngleInRad = Math.toRadians(-comparisonAngle.toDouble())
                val comparisonEndPoint = Offset(
                    x = center.x + (radius * cos(comparisonAngleInRad)).toFloat(),
                    y = center.y + (radius * sin(comparisonAngleInRad)).toFloat()
                )
                drawLine(
                    color = Color.Red,
                    start = center,
                    end = comparisonEndPoint,
                    strokeWidth = 3f
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${"%.0f".format(angle)}°", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (comparisonAngle != null) {
            Text(
                text = "${"%.0f".format(comparisonAngle)}° (ant.)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }

        Text(text = title, fontSize = 16.sp)
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AngleTimeLineChart(
    modifier: Modifier = Modifier,
    title: String,
    timestamps: List<Float>,
    angles: List<Float>,
    lineColor: Color,
    comparisonTimestamps: List<Float>? = null,
    comparisonAngles: List<Float>? = null,
    comparisonLineColor: Color = Color.Red
) {
    if (timestamps.isEmpty() || angles.isEmpty()) {
        Text("No hay datos para el gráfico de $title.")
        return
    }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val xPadding = 65.dp
            val yPadding = 8.dp

            val allAngles = angles + (comparisonAngles ?: emptyList())
            val allTimestamps = timestamps + (comparisonTimestamps ?: emptyList())

            val xMax = allTimestamps.maxOrNull() ?: 1f
            val yRange = (allAngles.maxOfOrNull { abs(it) } ?: 0f).coerceAtLeast(45f)
            val yMax = yRange + 10

            val graphWidth = maxWidth - xPadding
            val graphHeight = maxHeight - (yPadding * 2)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val xPaddingPx = xPadding.toPx()
                val yPaddingPx = yPadding.toPx()
                val graphWidthPx = graphWidth.toPx()
                val graphHeightPx = graphHeight.toPx()

                //Dibuja la cuadrícula y los ejes de los gráficos ángulo-tiempo
                val gridColor = Color.LightGray
                val numYGridLines = 4
                for (i in -numYGridLines..numYGridLines) {
                    if(i==0) continue
                    val yPos = (yPaddingPx + graphHeightPx / 2) - ((yMax / numYGridLines * i) / yMax) * (graphHeightPx / 2)
                    drawLine(gridColor, start = Offset(xPaddingPx, yPos), end = Offset(xPaddingPx + graphWidthPx, yPos), strokeWidth = 1f)
                }
                val numXGridLines = 5
                for(i in 0..numXGridLines) {
                    val xPos = xPaddingPx + (graphWidthPx / numXGridLines) * i
                    drawLine(gridColor, start=Offset(xPos, yPaddingPx), end=Offset(xPos, yPaddingPx + graphHeightPx), strokeWidth = 1f)
                }
                val axisColor = Color.Black
                drawLine(axisColor, start = Offset(xPaddingPx, yPaddingPx), end = Offset(xPaddingPx, graphHeightPx + yPaddingPx), strokeWidth = 2f)
                drawLine(axisColor, start = Offset(xPaddingPx, graphHeightPx / 2 + yPaddingPx), end = Offset(graphWidthPx + xPaddingPx, graphHeightPx / 2 + yPaddingPx), strokeWidth = 2f)

                fun scalePoint(time: Float, angle: Float): Offset {
                    val x = xPaddingPx + (time / xMax) * graphWidthPx
                    val y = (yPaddingPx + graphHeightPx / 2) - (angle / yMax) * (graphHeightPx / 2)
                    return Offset(x, y)
                }

                //Dibuja la línea de la prueba actual
                val path = Path()
                timestamps.zip(angles).forEachIndexed { index, (time, angle) ->
                    val point = scalePoint(time, angle)
                    if (index == 0) {
                        path.moveTo(point.x, point.y)
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }
                drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))

                //Dibuja la línea de la prueba anterior, si existe
                if (comparisonTimestamps != null && comparisonAngles != null) {
                    val comparisonPath = Path()
                    comparisonTimestamps.zip(comparisonAngles).forEachIndexed { index, (time, angle) ->
                        val point = scalePoint(time, angle)
                        if (index == 0) {
                            comparisonPath.moveTo(point.x, point.y)
                        } else {
                            comparisonPath.lineTo(point.x, point.y)
                        }
                    }
                    drawPath(comparisonPath, color = comparisonLineColor, style = Stroke(width = 2.dp.toPx()))
                }
            }

            // Etiquetas de los ejes
            Text(
                text = "Ángulo (°)",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = -12.dp, y = 0.dp)
                    .rotate(-90f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                maxLines = 1
            )
            val numYLabels = 4
            for (i in -numYLabels..numYLabels) {
                val angle = (yMax / numYLabels) * i
                val yOffset = yPadding + (graphHeight / 2) - (graphHeight / 2 * (angle / yMax))
                Text(
                    text = angle.roundToInt().toString(),
                    modifier = Modifier
                        .width(xPadding - 8.dp)
                        .offset(y = yOffset - 8.dp)
                        .padding(start = 16.dp),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        ) {
            val xPadding = 65.dp
            val graphWidth = maxWidth - xPadding
            val xMax = timestamps.maxOrNull() ?: 1f

            Text(
                text = "Tiempo (s)",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = xPadding),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            val numXLabels = 5
            for (i in 0..numXLabels) {
                val time = (xMax / numXLabels) * i
                val xOffset = xPadding + (graphWidth * (time / xMax))
                Text(
                    text = time.roundToInt().toString(),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = xOffset - 10.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(
    deviceName: String,
    deviceAddress: String,
    connectionState: ConnectionState,
    onClick: () -> Unit
) {
    val backgroundColor = when (connectionState) {
        ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
        ConnectionState.CONNECTING -> Color(0xFFFFF9C4)
        ConnectionState.CONNECTED -> Color(0xFFC8E6C9)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deviceName, fontWeight = FontWeight.SemiBold)
                Text(text = deviceAddress, fontSize = 12.sp)
            }
            Text(text = connectionState.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusIndicator(deviceName: String, state: ConnectionState) {
    val statusColor = when (state) {
        ConnectionState.DISCONNECTED -> Color.Red
        ConnectionState.CONNECTING -> Color(0xFFFBC02D)
        ConnectionState.CONNECTED -> Color(0xFF388E3C)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$deviceName: ", fontWeight = FontWeight.Normal, fontSize = 14.sp)
        Text(state.name, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun WarningCard(title: String, text: String, buttonText: String, onButtonClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(text)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onButtonClick) { Text(buttonText) }
        }
    }
}