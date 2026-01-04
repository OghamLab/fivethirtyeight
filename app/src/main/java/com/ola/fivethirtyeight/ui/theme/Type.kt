package com.ola.fivethirtyeight.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ola.fivethirtyeight.R
val TimesNewRoman = FontFamily(
    Font(R.font.times_new_roman, FontWeight.Normal),
    Font(R.font.times_new_roman_bold, FontWeight.Bold),
    Font(R.font.times_new_roman_bold, FontWeight.ExtraBold),
    Font(R.font.times_new_roman_bold, FontWeight.SemiBold)


)



val Typography = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman_bold)
        ),
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman_bold)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),


    titleSmall = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman_bold)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),


    labelLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    labelMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    displayLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    displayMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    displaySmall = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.times_new_roman)
        ),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ))



/*al AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        color = androidx.compose.ui.graphics.Color.Gray
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)












*/






/* Other default text styles to override
titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
),
labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
*/
