package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FarmifyTopAppBar(
    title: String,
    subtitle: String? = null,
    onLanguageToggle: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val langState = LocalAppLanguage.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("farmify_top_app_bar"),
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Farmer Vector Avatar + Assalam-o-Alaikum Greeting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    FarmerUserVectorAvatar(
                        size = 46.dp,
                        showTickMark = true,
                        onClick = onProfileClick
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (langState.isUrdu) "السلام علیکم" else "ASSALAM-O-ALAIKUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (subtitle != null && subtitle.isNotBlank()) subtitle else "Muhammad!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Sleek Action Buttons: Language Pill & Notification Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sleek Language Pill
                    Surface(
                        onClick = onLanguageToggle,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate),
                        shadowElevation = 1.dp,
                        modifier = Modifier.testTag("language_toggle_btn")
                    ) {
                        Text(
                            text = if (langState.isUrdu) "English" else "اردو",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Notification Bell / Profile Squircle
                    Surface(
                        onClick = onProfileClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("profile_top_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SoftWhite,
    borderColor: Color = BorderLight,
    elevation: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape = shape, spotColor = Color(0x0F1B3022))
            .border(1.dp, borderColor, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VeryLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 13.sp
                ),
                color = Color(0xFF334155)
            )
        }

        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.testTag("section_action_${title.take(6)}")
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldGreen
                    )
                )
            }
        }
    }
}

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    prefix: String = "Rs.",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = TextPrimary
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val formatted = formatter.format(amount.toLong())
    Text(
        text = "$prefix $formatted",
        style = style,
        color = color,
        modifier = modifier
    )
}

@Composable
fun WeatherMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EmeraldGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun OfflineStatusPill(
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isOnline) BadgeGreenBg else BadgeRedBg,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) SuccessGreen else ErrorRed)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (isOnline) "Live Sync" else "Offline Khata",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOnline) SuccessGreen else ErrorRed
            )
        }
    }
}

/**
 * Scalable Vector Image of Farmer Profile with optional Green Verified Checkmark
 */
@Composable
fun FarmerUserVectorAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    showTickMark: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Avatar Vector Drawing
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            val w = this.size.width
            val h = this.size.height

            // 1. Background Gradient (Lush Emerald to Light Sage)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7)),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.4f),
                    radius = w * 0.65f
                )
            )

            // 2. Outer Ring Accent
            drawCircle(
                color = Color(0xFF2E7D32),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.035f)
            )

            // 3. Torso / Kurta Shirt (Shoulders)
            val torsoPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.10f, h)
                cubicTo(
                    w * 0.12f, h * 0.70f,
                    w * 0.35f, h * 0.64f,
                    w * 0.5f, h * 0.66f
                )
                cubicTo(
                    w * 0.65f, h * 0.64f,
                    w * 0.88f, h * 0.70f,
                    w * 0.90f, h
                )
                close()
            }
            drawPath(
                path = torsoPath,
                color = Color(0xFF1B5E20) // Deep Forest Green Kurta
            )

            // Kurta Collar & Placket
            val collarPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.44f, h * 0.66f)
                lineTo(w * 0.5f, h * 0.76f)
                lineTo(w * 0.56f, h * 0.66f)
                lineTo(w * 0.5f, h * 0.69f)
                close()
            }
            drawPath(collarPath, color = Color(0xFFC8E6C9))

            // 4. Neck
            drawRect(
                color = Color(0xFFE5A87C),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.52f),
                size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.16f)
            )

            // 5. Head / Face Oval
            drawOval(
                color = Color(0xFFF3BF9B),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.31f, h * 0.28f),
                size = androidx.compose.ui.geometry.Size(w * 0.38f, h * 0.36f)
            )

            // 6. Farmer Traditional Pagri / Turban (Amber-Gold with folds)
            val turbanBase = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.24f, h * 0.34f)
                cubicTo(w * 0.20f, h * 0.15f, w * 0.40f, h * 0.08f, w * 0.54f, h * 0.09f)
                cubicTo(w * 0.72f, h * 0.10f, w * 0.82f, h * 0.20f, w * 0.76f, h * 0.35f)
                cubicTo(w * 0.65f, h * 0.30f, w * 0.35f, h * 0.30f, w * 0.24f, h * 0.34f)
                close()
            }
            drawPath(
                path = turbanBase,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFB300), Color(0xFFF57F17), Color(0xFFFF8F00))
                )
            )

            // Turban front crest / fold peak (Shamla)
            val turbanPeak = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.46f, h * 0.10f)
                cubicTo(w * 0.48f, h * 0.02f, w * 0.58f, h * 0.03f, w * 0.56f, h * 0.10f)
                close()
            }
            drawPath(path = turbanPeak, color = Color(0xFFFFD54F))

            // Turban wrap band line
            val turbanBand = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.26f, h * 0.32f)
                cubicTo(w * 0.40f, h * 0.27f, w * 0.60f, h * 0.27f, w * 0.74f, h * 0.32f)
                lineTo(w * 0.72f, h * 0.36f)
                cubicTo(w * 0.58f, h * 0.31f, w * 0.42f, h * 0.31f, w * 0.28f, h * 0.36f)
                close()
            }
            drawPath(path = turbanBand, color = Color(0xFFE65100))

            // 7. Facial Features: Eyes
            drawCircle(
                color = Color(0xFF263238),
                radius = w * 0.025f,
                center = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.42f)
            )
            drawCircle(
                color = Color(0xFF263238),
                radius = w * 0.025f,
                center = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.42f)
            )

            // Mustache & Smile (Friendly farmer mustache)
            val mustache = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.38f, h * 0.53f)
                cubicTo(w * 0.44f, h * 0.50f, w * 0.48f, h * 0.52f, w * 0.50f, h * 0.51f)
                cubicTo(w * 0.52f, h * 0.52f, w * 0.56f, h * 0.50f, w * 0.62f, h * 0.53f)
                cubicTo(w * 0.57f, h * 0.55f, w * 0.43f, h * 0.55f, w * 0.38f, h * 0.53f)
                close()
            }
            drawPath(path = mustache, color = Color(0xFF37474F))

            // Smile
            drawArc(
                color = Color(0xFFC26E4A),
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.53f),
                size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.07f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.025f)
            )
        }

        // Green Tick Mark Badge if enabled
        if (showTickMark) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(size * 0.38f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Verified",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.24f)
                )
            }
        }
    }
}



