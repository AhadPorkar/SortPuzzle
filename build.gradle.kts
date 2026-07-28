// ماژول ریشه — فقط پلاگین‌ها را برای زیرماژول‌ها اعلام می‌کند.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
