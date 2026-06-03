package com.socreate.app.core.model

/**
 * SoCreate Brand Identity
 *
 * Legal: Steven Michael Allen Owens
 * Developer: @SoQuarky
 * Umbrella: AdventuresInDrawing
 * Brands: Soquarky, FunFYP, FPY.Lovely, Soquarky.Click
 */
object SoCreateBranding {
    const val LEGAL_NAME = "Steven Michael Allen Owens"
    const val DEVELOPER_HANDLE = "SoQuarky"
    const val UMBRELLA_COMPANY = "AdventuresInDrawing"
    const val PRIMARY_BRAND = "Soquarky"

    val BRANDS = listOf(
        Brand("Soquarky", "Main business brand — creative tools & content", isActive = true),
        Brand("FunFYP", "Fun viral content & social media", isActive = true),
        Brand("FPY.Lovely", "Lifestyle & art community", isActive = true),
        Brand("Soquarky.Click", "Web platform & link hub", isActive = true)
    )

    const val APP_NAME = "SoCreate"
    const val APP_TAGLINE = "Animate Your Imagination"
    const val APP_VERSION_NAME = "1.1.0"
    const val APP_VERSION_CODE = 2

    // Social / Web
    const val WEBSITE = "soquarky.click"
    const val SUPPORT_EMAIL = "support@soquarky.click"
    const val PLAYSTORE_DEV_NAME = "Soquarky / AdventuresInDrawing"

    // Learning Platform
    const val ARTISTSO_URL = "artistso.com"
    const val ARTISTSO_FULL_URL = "https://artistso.com"
    const val CRASH_REPORT_EMAIL = "soquarky@artistso.com"

    // Copyright
    const val COPYRIGHT = "© 2026 Steven Michael Allen Owens. All rights reserved."
    const val COPYRIGHT_SHORT = "© 2026 Soquarky"

    // Build metadata
    const val BUILD_FLAVOR = "production"
    const val TARGET_DEVICE = "Samsung Galaxy Tab S10+"

    // Privacy
    const val PRIVACY_URL = "https://soquarky.click/privacy"
    const val TERMS_URL = "https://soquarky.click/terms"

    /**
     * Crash data ownership notice.
     * All crash data is owned by the end user, not the developer or company.
     */
    const val CRASH_DATA_OWNERSHIP = "All crash data is stored on YOUR device and belongs to YOU. " +
        "Neither Steven Michael Allen Owens, AdventuresInDrawing, nor any associated brand " +
        "collects, stores, or processes your crash data automatically. Sharing is opt-in only."
}

data class Brand(
    val name: String,
    val description: String,
    val isActive: Boolean = false
)
