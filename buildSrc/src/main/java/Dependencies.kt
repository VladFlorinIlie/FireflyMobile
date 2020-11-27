object Dependencies{

    const val minSdk = 23
    const val targetSdk = 30
    const val compileSdk = 30
    const val kotlinVersion = "1.4.10"
    private const val retrofitVersion = "2.9.0"
    private const val lifecycleVersion = "2.2.0"
    private const val roomVersion = "2.2.5"
    private const val coroutineVersion = "1.3.7"
    private const val appCompatVersion = "1.3.0-alpha02"
    private const val androidxFragmentVersion = "1.2.5"
    private const val androidxAnnotationVersion = "1.1.0"
    private const val androidxRecyclerViewVersion = "1.2.0-alpha05"
    private const val materialDesignVersion = "1.3.0-alpha02"
    private const val swipeRefreshVersion = "1.1.0"
    private const val androidxCoreVersion = "1.5.0-alpha05"
    private const val androidxConstraintLayoutVersion = "2.0.3"
    private const val androidxPrefVersion = "1.1.1"
    private const val androidxWorkVersion = "2.4.0"
    private const val materialDrawerVersion = "8.2.0"
    // Do not use 5.1.0 of iconics https://github.com/mikepenz/Android-Iconics/issues/524
    private const val iconicsVersion = "5.0.3"
    private const val toastyVersion = "7be5e09082"
    private const val chartVersion = "3.1.0"
    private const val aboutLibVersion = "3.2.0-rc01"
    private const val glideVersion = "4.11.0"
    private const val nachosVersion = "1.2.0"
    private const val acraVersion = "5.7.0"
    private const val osmdroidVersion = "6.1.8"
    private const val jUnitVersion = "5.7.0"
    private const val espressoVersion = "3.1.1"
    private const val androidTestVersion = "1.1.1"
    private const val androidTestCoreVersion = "1.0.0"
    private const val accordionViewVersion = "1.2.4"
    private const val fancyshowcaseviewVersion = "1.3.1"
    private const val markdownVersion = "0.13.0"
    private const val biometricVersion = "1.1.0-beta01"
    private const val testVersion = "1.3.0-alpha03"
    private const val taskerPluginVersion = "0.3.3"
    private const val moshiVersion = "1.11.0"
    private const val calendarViewVersion = "1.0.0"
    private const val pagingLibVersion = "3.0.0-alpha09"

    val lifecycleLibs = "androidx.lifecycle:lifecycle-extensions:$lifecycleVersion"
    val lifecyclerLiveDataCore = "androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycleVersion"
    val lifeCycleExtension = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion"
    val retrofitLibs = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    val retrofitMoshi = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
    val retrofitScalar = "com.squareup.retrofit2:converter-scalars:$retrofitVersion"
    val mockWebServer = "org.mock-server:mockserver-netty:5.11.2"
    val roomLibs = "androidx.room:room-runtime:$roomVersion"
    val roomExtension = "androidx.room:room-ktx:$roomVersion"
    val roomCompiler = "androidx.room:room-compiler:$roomVersion"
    val coroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion"
    val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
    val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:$swipeRefreshVersion"
    val androidxFragment = "androidx.fragment:fragment-ktx:$androidxFragmentVersion"
    val androidxAnnotation = "androidx.annotation:annotation:$androidxAnnotationVersion"
    val androidxRecyclerView = "androidx.recyclerview:recyclerview:$androidxRecyclerViewVersion"
    val materialDesign = "com.google.android.material:material:$materialDesignVersion"
    val androidxCore = "androidx.core:core-ktx:$androidxCoreVersion"
    val androidxConstraintLayout = "androidx.constraintlayout:constraintlayout:$androidxConstraintLayoutVersion"
    val androidxPref = "androidx.preference:preference:$androidxPrefVersion"
    val androidxWork = "androidx.work:work-runtime-ktx:$androidxWorkVersion"
    val materialDrawer = "com.mikepenz:materialdrawer:$materialDrawerVersion"
    val iconics = "com.mikepenz:iconics-core:$iconicsVersion"
    val materialDrawerIconics = "com.mikepenz:materialdrawer-iconics:$materialDrawerVersion"
    // Do not upadte them yet!
    val googleMaterialIcons  = "com.mikepenz:google-material-typeface:3.0.1.4.original-kotlin@aar"
    val fontAwesome = "com.mikepenz:fontawesome-typeface:5.9.0.0-kotlin@aar"
    val toasty = "com.github.GrenderG:Toasty:$toastyVersion"
    val chart = "com.github.PhilJay:MPAndroidChart:$chartVersion"
    val aboutLib = "com.github.daniel-stoneuk:material-about-library:$aboutLibVersion"
    val kotlinLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    val glideLib = "com.github.bumptech.glide:glide:$glideVersion"
    val glideCompiler = "com.github.bumptech.glide:compiler:$glideVersion"
    val glideOkHttpExtension = "com.github.bumptech.glide:okhttp3-integration:$glideVersion"
    val nachos = "com.hootsuite.android:nachos:$nachosVersion"
    val acraMail = "ch.acra:acra-mail:$acraVersion"
    val osmdroid = "org.osmdroid:osmdroid-android:$osmdroidVersion"
    val junitEngine = "org.junit.jupiter:junit-jupiter-engine:$jUnitVersion"
    val jUnitApi = "org.junit.jupiter:junit-jupiter-api:$jUnitVersion"
    val jUnitParameter = "org.junit.jupiter:junit-jupiter-params:$jUnitVersion"
    val espresso = "androidx.test.espresso:espresso-core:$espressoVersion"
    val androidTest = "androidx.test:runner:$androidTestVersion"
    val androidTestCore = "androidx.test:core:$androidTestCoreVersion"
    val androidTestExt = "androidx.test.ext:junit:$androidTestCoreVersion"
    val accordionView = "com.github.florent37:expansionpanel:$accordionViewVersion"
    val fancyshowcaseview = "me.toptas.fancyshowcase:fancyshowcaseview:$fancyshowcaseviewVersion"
    val markdownLib = "com.atlassian.commonmark:commonmark:$markdownVersion"
    val markdownStrikeThroughExtension = "com.atlassian.commonmark:commonmark-ext-gfm-strikethrough:$markdownVersion"
    val markdownAutoLink = "com.atlassian.commonmark:commonmark-ext-autolink:$markdownVersion"
    val biometricLib = "androidx.biometric:biometric:$biometricVersion"
    val testRunner = "androidx.test:runner:$testVersion"
    val testOrchestrator = "androidx.test:orchestrator:$testVersion"
    val taskerPluginLib = "com.joaomgcd:taskerpluginlibrary:$taskerPluginVersion"
    val notificationLib = "io.karn:notify:1.3.0"
    val moshiLib = "com.squareup.moshi:moshi:$moshiVersion"
    val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
    val calendarView = "com.github.kizitonwose:CalendarView:$calendarViewVersion"
    val pagingLib = "androidx.paging:paging-runtime-ktx:$pagingLibVersion"
}